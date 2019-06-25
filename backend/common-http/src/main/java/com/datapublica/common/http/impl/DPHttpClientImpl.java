package com.datapublica.common.http.impl;

/*
 * Copyright (C) by Data Publica, All Rights Reserved.
 */

import com.datapublica.common.http.DPHttpResponse;
import org.apache.commons.io.IOUtils;
import org.apache.http.*;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.client.RedirectLocations;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.List;

/**
 * Core implementation of DPHTTPClient This is based on Apache HttpClient, supports http and https with any
 * BasicAuthHttpProxy.
 * <p>
 * This service supports two optional parameters
 * <ul>
 * <li>proxy: BasicAuthHttpProxy. Corresponds to the proxy provider which will be used</li>
 * <li>userAgent: String. Default User-Agent that will be used if it is not set in the request.</li>
 * </ul>
 */
public class DPHttpClientImpl extends AbstractHttpClientImpl {
    private static final Logger log = LoggerFactory.getLogger(DPHttpClientImpl.class);

    private DefaultHttpClient client;
    private BasicHttpContext localcontext;

    public DPHttpClientImpl(String cookiePolicy, boolean followRedirect) {


        TrustManager[] tm = new TrustManager[]{new TrustManagerManipulator()};
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tm, new SecureRandom());
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            // init fail for JVM reasons
            throw new RuntimeException(e);
        }

        SSLSocketFactory sslSocketFactory = new SSLSocketFactory(sslContext,
                SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        // Declare registry
        final SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
        registry.register(new Scheme("https", 443, sslSocketFactory));
        // Get pooled client, and set route planner
        this.client = new DefaultHttpClient(new PoolingClientConnectionManager(registry));
        if (followRedirect) {
            // redirect and send headers again
            this.client.setRedirectStrategy(new LaxRedirectStrategy() {
                @Override
                public HttpUriRequest getRedirect(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {
                    HttpUriRequest redirect = super.getRedirect(request, response, context);
                    redirect.setHeaders(request.getAllHeaders());
                    return redirect;
                }
            });
        }
        this.localcontext = new BasicHttpContext();
        // Set cookie policy
        this.client.getParams().setParameter(ClientPNames.COOKIE_POLICY, cookiePolicy);
    }

    public DPHttpClientImpl(String cookiePolicy) {
        this(cookiePolicy, true);
    }

    public DPHttpClientImpl() {
        this(CookiePolicy.IGNORE_COOKIES, true);
    }

    public DPHttpClientImpl(boolean followRedirect) {
        this(CookiePolicy.IGNORE_COOKIES, followRedirect);
    }

    @Override
    public void setProxy(BasicAuthHttpProxy proxy) {
        super.setProxy(proxy);
        this.localcontext = (this.proxy != null) ? this.proxy.initClient(this.client) : new BasicHttpContext();
    }

    @Override
    protected DPHttpResponse process(HttpUriRequest request, int... expectedStatues) throws IOException {
        return internalProcess(request, null);
    }

    @Override
    protected DPHttpResponse processAndStore(HttpUriRequest request, File targetFile, int... expectedStatuses) throws IOException {
        return internalProcess(request, targetFile);
    }

    protected DPHttpResponse internalProcess(HttpUriRequest request, File targetFile) throws IOException {
        HttpResponse response = null;
        log.debug("Executing HTTP request: " + request.getURI());
        try {
            // Execute query
            final HttpContext context = new BasicHttpContext(this.localcontext);
            final Date date = new Date();
            response = this.client.execute(request, context);

            // Track redirections
            URI target = request.getURI();
            RedirectLocations locations = (RedirectLocations) context.getAttribute(DefaultRedirectStrategy.REDIRECT_LOCATIONS);
            if (locations != null) {
                List<URI> all = locations.getAll();
                target = all.get(all.size() - 1);
                log.debug("Request [{}] has been redirected to [{}]", request.getURI().toString(), target.toString());
            }

            HttpEntity entity = response.getEntity();

            // Build response
            final DPHttpResponse result = new DPHttpResponse(date, response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase(), target.toString());
            parseHeader(response, result, request);

            try {
                // May not use get(HttpEntity) anymore : fucking Microsoft IIS server is sending an invalid content type (between double quotes)
                ContentType contentType = null;
                try {
                    contentType = ContentType.get(entity);
                } catch (IllegalArgumentException e) {
                    final Header[] headers = response.getHeaders("Content-Type");
                    if (headers != null && headers.length > 0) {
                        final String value = headers[0].getValue();
                        if (value.matches("^\".+\"$")) {
                            final String parsed = value.substring(1, value.length() - 1);
                            log.info("Parsing content type [" + parsed + "] from origin [" + value + "]");
                            contentType = ContentType.parse(parsed);
                        }
                    }
                    if (contentType == null) {
                        throw e;
                    }
                }
                //
                if (contentType != null) {
                    result.setContentType(contentType.getMimeType());
                    Charset charset = contentType.getCharset();
                    if (charset != null)
                        result.setCharset(charset.name());
                }
            } catch (UnsupportedCharsetException ex) {
                log.warn("Unknown charset: " + ex.getMessage());
            }
            // Null entity exception: we adopt the "empty body" strategy to make things easier for everyone
            if (targetFile != null) {
                IOUtils.copy(entity.getContent(), new FileOutputStream(targetFile));
            } else {
                result.setContent(entity == null ? new byte[]{} : EntityUtils.toByteArray(entity));
            }
            return result;
        } finally {
            // Consume response
            if (response != null) {
                EntityUtils.consume(response.getEntity());
            }
        }
    }
}
