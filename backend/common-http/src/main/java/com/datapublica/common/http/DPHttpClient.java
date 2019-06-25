package com.datapublica.common.http;

/*
 * Copyright (C) by Data Publica, All Rights Reserved.
 */

import com.datapublica.common.http.exception.UnexpectedHttpStatusException;
import org.apache.http.client.methods.HttpUriRequest;

import java.io.File;
import java.io.IOException;

/**
 * Simplified http client
 */
public interface DPHttpClient {

    /**
     * Execute an HTTP request through the proxy.
     *
     * @param request        The HTTP request to be executed
     * @param expectedStatus The expected HTTP statuses
     * @return The proxy response
     * @throws UnexpectedHttpStatusException If the HTTP response status is not the the expected one
     * @throws java.io.IOException           If an error occurs executing request
     */
    public DPHttpResponse execute(HttpUriRequest request, int... expectedStatus) throws UnexpectedHttpStatusException, IOException;

    /**
     * Execute an HTTP request through the proxy.
     *
     * @param request The HTTP request to be executed
     * @return The proxy response
     * @throws UnexpectedHttpStatusException If the HTTP response status is not a 200
     * @throws java.io.IOException           If an error occurs executing request
     */
    public DPHttpResponse execute(HttpUriRequest request) throws UnexpectedHttpStatusException, IOException;


    /**
     * Execute an HTTP request through the proxy.
     *
     * @param request        The HTTP request to be executed
     * @param expectedStatus The expected HTTP statuses
     * @return The proxy response
     * @throws UnexpectedHttpStatusException If the HTTP response status is not the the expected one
     * @throws java.io.IOException           If an error occurs executing request
     */
    public DPHttpResponse executeAndStore(HttpUriRequest request, File destFile, int... expectedStatus) throws UnexpectedHttpStatusException, IOException;

    /**
     * Execute an HTTP request through the proxy.
     *
     * @param request The HTTP request to be executed
     * @return The proxy response
     * @throws UnexpectedHttpStatusException If the HTTP response status is not a 200
     * @throws java.io.IOException           If an error occurs executing request
     */
    public DPHttpResponse executeAndStore(HttpUriRequest request, File destFile) throws UnexpectedHttpStatusException, IOException;

    /**
     * Inject the IP of the proxy the user would like to use for this request
     *
     * @param request The HTTP request to be executed
     * @param proxyIp The desired proxy
     */
    public void selectProxy(HttpUriRequest request, String proxyIp);
}
