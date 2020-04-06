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

<pre><code>docker pull nielsbasjes/yauaa:{{ book.YauaaVersion }}
docker run -p8080:8080 nielsbasjes/yauaa:{{ book.YauaaVersion }}
</code></pre>

and then open

    http://localhost:8080/

in your browser to get the output of the servlet.

## Custom rules

The servlet supports loading your own custom rules, which can be useful to classify internal monitoring systems.
It does this by looking in the folder that start with UserAgents for yaml files (i.e. `file:UserAgents*/*.yaml` ).

Based on the docker image this can be easily done with an additional layer where your entire `Dockerfile` looks like this

<pre><code>FROM nielsbasjes/yauaa:{{ book.YauaaVersion }}
ADD InternalTraffic.yaml UserAgents/
</code></pre>

When you build that docker image and run it the logging should contain something like this:

    Loading 1 rule files using expression: file:UserAgents*/*.yaml
    - Preparing InternalTraffic.yaml (9608 bytes)
    - Loaded  1 files in   11 msec using expression: file:UserAgents*/*.yaml

# Kubernetes

I've been playing around with Kubernetes and the code below "works on my cluster".

## Basic Service
First create a dedicated namespace and a very basic deployment to run this image 3 times and
exposes it as a ```Service``` that simply does ```http```.

<pre><code>apiVersion: v1
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
        image: nielsbasjes/yauaa:{{ book.YauaaVersion }}
        ports:
        - containerPort: 8080
          name: yauaa
          protocol: TCP
        readinessProbe:
          httpGet:
            path: /running
            port: yauaa
          initialDelaySeconds: 2
          periodSeconds: 3
        livenessProbe:
          httpGet:
            path: /running
            port: yauaa
          initialDelaySeconds: 10
          periodSeconds: 10

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
</code></pre>

## Custom rules in Kubernetes

In some cases you'll have internal systems with custom useragents.
You can write your own rules and include them in the deployment.

First define your rules and store them as a configmap in k8s.

You can do this by putting the config files in a folder 'foo' and doing something like: `kubectl create -n yauaa configmap niels-yauaa-rules --from-file=foo`

Or you can include them directly in the kubectl config file like this:

<pre><code>apiVersion: v1
kind: ConfigMap
metadata:
  name: niels-yauaa-rules
  namespace: yauaa
data:
  MyCustomRules.yaml: |
    config:
    - matcher:
        require:
        - 'agent.product.name="NielsBasjes"'
        extract:
        - 'CustomRuleDemonstrationName    : 42 :"A Simple demonstration of a custom rule"'
        - 'CustomRuleDemonstrationWebsite : 42 :"https://yauaa.basjes.nl"'
    - test:
        input:
          user_agent_string: 'NielsBasjes/42 (https://niels.basjes.nl)'
        expected:
          DeviceClass                           : 'Robot'
          DeviceName                            : 'Basjes Robot'
          DeviceBrand                           : 'Basjes'
          OperatingSystemClass                  : 'Cloud'
          OperatingSystemName                   : 'Cloud'
          OperatingSystemVersion                : '??'
          OperatingSystemVersionMajor           : '??'
          OperatingSystemNameVersion            : 'Cloud ??'
          OperatingSystemNameVersionMajor       : 'Cloud ??'
          LayoutEngineClass                     : 'Unknown'
          LayoutEngineName                      : 'Unknown'
          LayoutEngineVersion                   : '??'
          LayoutEngineVersionMajor              : '??'
          LayoutEngineNameVersion               : 'Unknown ??'
          LayoutEngineNameVersionMajor          : 'Unknown ??'
          AgentClass                            : 'Special'
          AgentName                             : 'NielsBasjes'
          AgentVersion                          : '42'
          AgentVersionMajor                     : '42'
          AgentNameVersion                      : 'NielsBasjes 42'
          AgentNameVersionMajor                 : 'NielsBasjes 42'
          AgentInformationUrl                   : 'https://niels.basjes.nl'
          CustomRuleDemonstrationName           : 'A Simple demonstration of a custom rule'
          CustomRuleDemonstrationWebsite        : 'https://yauaa.basjes.nl'
</code></pre>

Then the deployment must turn this config map into a volume and mount it in the pods.
Note that the folder under which is is mounted must be a the root level and the name must start with "UserAgent".

So the deployment becomes something like this

<pre><code>apiVersion: apps/v1
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
      volumes:
        - name: niels-yauaa-rules-volume
          configMap:
            name: niels-yauaa-rules
      containers:
      - name: yauaa
        image: nielsbasjes/yauaa:{{ book.YauaaVersion }}
        volumeMounts:
          # NOTE 1: The directory name MUST start with  "/UserAgents" !!
          # NOTE 2: You can have multiple as long as the mountPaths are all different.
          - name: niels-yauaa-rules-volume
            mountPath: /UserAgents-Niels
        ports:
        - containerPort: 8080
          name: yauaa
          protocol: TCP
        readinessProbe:
          httpGet:
            path: /running
            port: yauaa
          initialDelaySeconds: 2
          periodSeconds: 3
        livenessProbe:
          httpGet:
            path: /running
            port: yauaa
          initialDelaySeconds: 10
          periodSeconds: 10
</code></pre>


## Available outside the cluster (HTTP)
Depending on your Kubernetes cluster you may have the option to change
the ```.spec.type``` of the above ```Service``` to ```LoadBalancer```
or you may choose to expose it via an ```Ingress```.

I have been able to get it working with the helm chart for ```stable/nginx-ingress``` where I have enabled SSL.

Simply putting up an ```Ingress``` works something like this

<pre><code>
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
</code></pre>

## Available outside the cluster (HTTPS)
Now I wanted to use ```https://``` instead of ```http://```.

Before you can start this you need the SSL (TLS actually) certificate and key for the hostname you have.

I created the required secret (in the correct namespace) with a command similar to this (I have letsencrypt):

```shell script
kubectl -n yauaa create secret tls yauaa-cert --key=/etc/letsencrypt/live/example.nl/privkey.pem --cert=/etc/letsencrypt/live/example.nl/fullchain.pem
```

After the secret has been put into place the ```Ingress``` can be created with SSL.

<pre><code>apiVersion: extensions/v1beta1
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
</code></pre>

