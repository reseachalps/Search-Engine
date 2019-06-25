package com.datapublica.common.http.impl;

import com.datapublica.common.http.DPHttpClient;
import com.datapublica.common.http.DPHttpResponse;
import com.datapublica.common.http.exception.UnexpectedHttpStatusException;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;

import java.io.File;
import java.io.IOException;

/**
 * Provides a basic implementation of an http client that manages proxies and user agents
 */
public abstract class AbstractHttpClientImpl implements DPHttpClient {
    protected BasicAuthHttpProxy proxy;
    protected String userAgent = "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/6.0)";

    public void setProxy(BasicAuthHttpProxy proxy) {
        this.proxy = proxy;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    @Override
    public DPHttpResponse executeAndStore(HttpUriRequest request, File destFile) throws UnexpectedHttpStatusException, IOException {
        return this.executeAndStore(request, destFile, HttpStatus.SC_OK);
    }

    @Override
    public DPHttpResponse executeAndStore(HttpUriRequest request, File destFile, int... expectedStatuses) throws UnexpectedHttpStatusException, IOException {
    // Execute request
        prepareRequest(request);
        final DPHttpResponse response = processAndStore(request, destFile, expectedStatuses);

        int expectedStatus = 0;
        for (int i = 0; i < expectedStatuses.length; i++) {
            expectedStatus = expectedStatuses[i];
            if (response.getStatus() == expectedStatus) {
                return response;
            }
        }
        //
        throw new UnexpectedHttpStatusException(expectedStatus, response);
    }

    @Override
    public DPHttpResponse execute(HttpUriRequest request, int... expectedStatuses) throws IOException {
        // Execute request
        prepareRequest(request);
        final DPHttpResponse response = process(request, expectedStatuses);

        int expectedStatus = 0;
        for (int i = 0; i < expectedStatuses.length; i++) {
            expectedStatus = expectedStatuses[i];
            if (response.getStatus() == expectedStatus) {
                return response;
            }
        }
        //
        throw new UnexpectedHttpStatusException(expectedStatus, response);
    }

    @Override
    public DPHttpResponse execute(HttpUriRequest request) throws IOException {
        return this.execute(request, HttpStatus.SC_OK);
    }

    /**
     * Can parse headers inside the results. It will set the proxies if necessary and if found
     *
     * @param response the response
     * @param result   the result object to fill
     * @param request  the request
     */
    protected void parseHeader(HttpResponse response, DPHttpResponse result, HttpUriRequest request) {
        for (Header header : response.getAllHeaders()) {
            result.addHeader(header.getName(), header.getValue());
        }
        if (this.proxy != null) {
            result.setProxyIp(this.proxy.extractProxyIp(response, request));
        }
    }

    public void selectProxy(HttpUriRequest request, String ip) {
        if (proxy != null) {
            this.proxy.selectProxy(request, ip);
        }
    }

    protected void prepareRequest(HttpUriRequest request) {
        if (request.getFirstHeader("User-Agent") == null) {
            request.addHeader("User-Agent", this.userAgent);
        }
    }

    /**
     * This method must be implemented to execute the request. This request is ready to be executed. Don't forget to
     * call #parseHeader after the response has been fetched.
     *
     * @param request          the request ready to be set
     * @param expectedStatuses The expected HTTP status (allow to disable caching if the status is not the expected one)
     * @return A fully filled response object
     * @throws IOException
     */
    protected abstract DPHttpResponse process(HttpUriRequest request, int... expectedStatuses) throws IOException;


    /**
     * This method must be implemented to execute the request. This request is ready to be executed. Don't forget to
     * call #parseHeader after the response has been fetched.
     *
     * @param request          the request ready to be set
     * @param expectedStatuses The expected HTTP status (allow to disable caching if the status is not the expected one)
     * @return A fully filled response object
     * @throws IOException
     */
    protected abstract DPHttpResponse processAndStore(HttpUriRequest request, File targetFile, int... expectedStatuses) throws IOException;
}
