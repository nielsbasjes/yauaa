package nl.basjes.parse.useragent.utils;

import nl.basjes.parse.useragent.utils.publicsuffix.PublicSuffixMatcher;
import nl.basjes.parse.useragent.utils.publicsuffix.PublicSuffixMatcherLoader;
import org.apache.http.conn.util.DomainType;
import org.junit.jupiter.api.Test;

import static nl.basjes.parse.useragent.utils.HostnameExtracter.extractHostname;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TestHostnameExtracter {

    @Test
    public void testHostnameExtract(){
        assertEquals("www.basjes.nl", extractHostname("https://www.basjes.nl/test"));
        assertEquals("www.basjes.nl", extractHostname("//www.basjes.nl/test"));
        assertEquals("www.basjes.nl", extractHostname("www.basjes.nl/test"));
        assertEquals("www.basjes.nl", extractHostname("//www.basjes.nl"));
        assertEquals("www.basjes.nl", extractHostname("www.basjes.nl"));
        assertNull(extractHostname("/index.html"));

        // Unable to determine if is is a hostname or a path name, so assume hostname
        assertEquals("index.html", extractHostname("index.html"));
    }

    @Test
    public void testHostnameExtractQueryParams() {
        assertEquals("www.basjes.nl", extractHostname("https://www.basjes.nl/test?foo=foo"));
        assertEquals("www.basjes.nl", extractHostname("https://www.basjes.nl/test&foo=foo"));
        assertEquals("www.basjes.nl", extractHostname("https://www.basjes.nl/test?foo=foo&bar=bar"));
        assertEquals("www.basjes.nl", extractHostname("https://www.basjes.nl/test&foo=foo?bar=bar"));
        assertEquals("www.basjes.nl", extractHostname("https://www.basjes.nl/test?foo=foo?bar=bar"));
        assertEquals("www.basjes.nl", extractHostname("https://www.basjes.nl/test&foo=foo&bar=bar"));
        assertEquals("www.basjes.nl", extractHostname("https://www.basjes.nl/test????foo=bar"));
        assertEquals("www.basjes.nl", extractHostname("https://www.basjes.nl/test&&&&foo=bar"));
        assertEquals("www.basjes.nl", extractHostname("https://www.basjes.nl/test?&?&foo=bar"));

        assertEquals("www.basjes.nl", extractHostname("https://www.basjes.nl/test?foo=foo#Foo#Bar"));
        assertEquals("www.basjes.nl", extractHostname("https://www.basjes.nl/test&foo=foo#Foo#Bar"));
        assertEquals("www.basjes.nl", extractHostname("https://www.basjes.nl/test?foo=foo&bar=bar#Foo#Bar"));
        assertEquals("www.basjes.nl", extractHostname("https://www.basjes.nl/test&foo=foo?bar=bar#Foo#Bar"));
        assertEquals("www.basjes.nl", extractHostname("https://www.basjes.nl/test?foo=foo?bar=bar#Foo#Bar"));
        assertEquals("www.basjes.nl", extractHostname("https://www.basjes.nl/test&foo=foo&bar=bar#Foo#Bar"));
        assertEquals("www.basjes.nl", extractHostname("https://www.basjes.nl/test????foo=bar#Foo#Bar"));
        assertEquals("www.basjes.nl", extractHostname("https://www.basjes.nl/test&&&&foo=bar#Foo#Bar"));
        assertEquals("www.basjes.nl", extractHostname("https://www.basjes.nl/test?&?&foo=bar#Foo#Bar"));
    }

    @Test
    public void verifyHTTPCLIENT_2030(){
        // See https://issues.apache.org/jira/browse/HTTPCLIENT-2030
        PublicSuffixMatcher matcher = PublicSuffixMatcherLoader.getDefault();
        assertEquals("unknown", matcher.getDomainRoot("www.example.unknown"));
        assertNull(matcher.getDomainRoot("www.example.unknown", DomainType.ICANN));
    }

}
