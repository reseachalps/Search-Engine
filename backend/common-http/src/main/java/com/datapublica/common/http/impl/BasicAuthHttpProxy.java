package com.datapublica.common.http.impl;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.ChallengeState;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import javax.annotation.PostConstruct;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Description bean of any http proxy provider based on Basic authentification
 */
public class BasicAuthHttpProxy {
    private static final String COOKIES_HEADER = "Set-Cookie";
    private static final Logger log = LoggerFactory.getLogger(BasicAuthHttpProxy.class);
    private String host;
    private int port;
    private String username;
    private String password;
    /**
     * If true, it indicates that the proxyIp can be found in response cookies. If false, the proxyIp can be found in headers
     * Default: false
     */
    private boolean proxyIpInCookie = false;
    /**
     * Name of the proxyIp header
     * Default: null
     */
    private String proxyIpHeader;
    private Pattern regexIp;

    @PostConstruct
    private void init() {
    }

    /**
     * Modify the request to inject the proxy selection. This method issues a warning if a https scheme is selected
     *
     * @param request the request
     * @param ip the ip to select, can be null
     */
    public void selectProxy(HttpUriRequest request, String ip) {
        if (ip == null)
            return;
        if ("https".equals(request.getURI().getScheme())) {
            log.warn("Selecting a specific proxy with https is currently not supported... "
                    + "no header or cookie will be injected in the request");
            return;
        }
        if (!proxyIpInCookie) {
            request.addHeader(proxyIpHeader, ip);
        } else {
            Header cookie = request.getFirstHeader("Cookie");
            String cookieString = proxyIpHeader + "=" + ip + ";";
            if (cookie == null) {
                request.addHeader("Cookie", cookieString);
            } else {
                String value = cookie.getValue();
                if(!value.matches(";\\s*$"))
                    value += ";";
                request.setHeader("Cookie", value + " " + cookieString);
            }
        }
    }

    /**
     * Extract the proxy ip from the response. If it is not found (because DP proxy does not refresh the cookie for
     * instance), it will try to look inside the request for a trace of the selected proxy.
     *
     * @param response the response
     * @param request the request that was used
     * @return the proxy ip, null if not found
     */
    public String extractProxyIp(HttpResponse response, HttpUriRequest request) {
        String proxyIp = extractProxyIpFromHeader(response);

        if (proxyIp == null && proxyIpInCookie) {
            // proxy in cookie check in the cookie jar!
            for (Header cookie : request.getHeaders("Cookie")) {
                final Matcher matcher = regexIp.matcher(cookie.getValue());

                if (matcher.find()) {
                    proxyIp = matcher.group(1);
                    return proxyIp;
                }
            }
        }
        return proxyIp;
    }

    /**
     * Extract proxy ip from header list, if it is cookie-base it will search through cookies
     *
     * @param response the response
     * @return the ip used by the proxy, null if not found
     */
    private String extractProxyIpFromHeader(HttpResponse response) {
        for (Header header : response.getAllHeaders()) {
            if (proxyIpInCookie) {
                if (COOKIES_HEADER.equals(header.getName())) {
                    final Matcher matcher = regexIp.matcher(header.getValue());
                    if (matcher.find()) {
                        return matcher.group(1);
                    }
                }
            } else if (header.getName().equals(proxyIpHeader)) {
                return header.getValue();
            }
        }
        return null;
    }

    /**
     * Initialize the client to go through this proxy and provide the context to use if credentials are provided
     *
     * @param client the apache http client
     * @return null if no username is provided
     */
    public BasicHttpContext initClient(DefaultHttpClient client) {
        if(host == null)
            return null;
        BasicHttpContext context = null;
        HttpHost httpHost = new HttpHost(host, port);
        if (username != null) {
            client.getCredentialsProvider().setCredentials(new AuthScope(httpHost),
                    new UsernamePasswordCredentials(username, password));
            AuthCache authCache = new BasicAuthCache();
            BasicScheme basicAuth = new BasicScheme(ChallengeState.PROXY);
            authCache.put(httpHost, basicAuth);

            context = new BasicHttpContext();
            context.setAttribute(ClientContext.AUTH_CACHE, authCache);
        }

        client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, httpHost);
        return context;
    }

    @Required
    public void setHost(String host) {
        this.host = host;
    }
    @Required
    public void setPort(int port) {
        this.port = port;
    }
    @Required
    public void setUsername(String username) {
        this.username = username;
    }
    @Required
    public void setPassword(String password) {
        this.password = password;
    }

    public void setProxyIpInCookie(boolean proxyIpInCookie) {
        this.proxyIpInCookie = proxyIpInCookie;
        if (regexIp == null && proxyIpInCookie && proxyIpHeader != null) {
            regexIp = Pattern.compile(proxyIpHeader + "=([0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+:[0-9]+);");
        }
    }

    public void setProxyIpHeader(String proxyIpHeader) {
        this.proxyIpHeader = proxyIpHeader;
        if (regexIp == null && proxyIpInCookie && proxyIpHeader != null) {
            regexIp = Pattern.compile(proxyIpHeader + "=([0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+:[0-9]+);");
        }
    }

    @Override
    public String toString() {
        return "BasicAuthHttpProxy{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", username='" + username + '\'' +
                ", proxyIpInCookie=" + proxyIpInCookie +
                ", proxyIpHeader='" + proxyIpHeader + '\'' +
                '}';
    }
}
