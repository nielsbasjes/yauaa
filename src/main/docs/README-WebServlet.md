# The demonstration webservlet
Part of the distribution is a war file that is a servlet that has a webinterface and
some APIs that allow you to try things out.

This servlet can be downloaded via

<pre><code>&lt;dependency&gt;
  &lt;groupId&gt;nl.basjes.parse.useragent&lt;/groupId&gt;
  &lt;artifactId&gt;yauaa-webapp&lt;/artifactId&gt;
  &lt;version&gt;{{ book.YauaaVersion }}&lt;/version&gt;
  &lt;type&gt;war&lt;/type&gt;
&lt;/dependency&gt;
</code></pre>

NOTE that this is a **DEMONSTRATION** servlet!

It is simply the library in a servlet, no optimizations or smart memory settings have been done at all.

# Docker
Starting with version 5.14.1 the webservlet is also published to the central docker registry.

    https://hub.docker.com/r/nielsbasjes/yauaa

So with docker installed and running on your (Linux) desktop
you should be able to so something as simple as

    docker pull nielsbasjes/yauaa
    docker run -p8080:8080 nielsbasjes/yauaa

and then open

    http://localhost:8080/

in your browser to get the output of the servlet.

# Kubernetes

I've been playing around with Kubernetes and the code below "works on my cluster".

## Basic Service
First create a dedicated namespace and a very basic deployment to run this image 3 times and
exposes it as a ```Service``` that simply does ```http```.

```
apiVersion: v1
kind: Namespace
metadata:
  name: yauaa

---

apiVersion: apps/v1
kind: Deployment
metadata:
  name: yauaa
  namespace: yauaa
spec:
  selector:
    matchLabels:
      app: yauaa
  replicas: 3
  template:
    metadata:
      labels:
        app: yauaa
    spec:
      containers:
      - name: yauaa
        image: nielsbasjes/yauaa:latest
        ports:
        - containerPort: 8080
          protocol: TCP

---

apiVersion: v1
kind: Service
metadata:
  name: yauaa
  namespace: yauaa
spec:
  selector:
    app: yauaa
  ports:
  - name: default
    protocol: TCP
    port: 80
    targetPort: 8080
  type: ClusterIP
```


## Available outside the cluster (HTTP)
Depending on your Kubernetes cluster you may have the option to change
the ```.spec.type``` of the above ```Service``` to ```LoadBalancer```
or you may choose to expose it via an ```Ingress```.

I have been able to get it working with the helm chart for ```stable/nginx-ingress``` where I have enabled SSL.

Simply putting up an ```Ingress``` works something like this

```
apiVersion: extensions/v1beta1
kind: Ingress
metadata:
  name: yauaa
  namespace: yauaa
  annotations:
    kubernetes.io/ingress.class: nginx
spec:
  rules:
    - host: yauaa.example.nl
      http:
        paths:
          - backend:
              serviceName: yauaa
              servicePort: 80
            path: /
```

## Available outside the cluster (HTTPS)
Now I wanted to use ```https://``` instead of ```http://```.

Before you can start this you need the SSL (TLS actually) certificate and key for the hostname you have.

I created the required secret (in the correct namespace) with a command similar to this (I have letsencrypt):

```
kubectl -n yauaa create secret tls yauaa-cert --key=/etc/letsencrypt/live/example.nl/privkey.pem --cert=/etc/letsencrypt/live/example.nl/fullchain.pem
```

After the secret has been put into place the ```Ingress``` can be created with SSL.

```
apiVersion: extensions/v1beta1
kind: Ingress
metadata:
  name: yauaa
  namespace: yauaa
  annotations:
    kubernetes.io/ingress.class: nginx
    nginx.ingress.kubernetes.io/force-ssl-redirect: "true"
spec:
  rules:
    - host: yauaa.example.nl
      http:
        paths:
          - backend:
              serviceName: yauaa
              servicePort: 80
            path: /
  tls:
    - hosts:
        - yauaa.example.nl
      secretName: yauaa-cert
```

