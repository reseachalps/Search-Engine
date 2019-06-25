/*
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
package com.datapublica.common.http.exception;

import com.datapublica.common.http.DPHttpResponse;

import java.io.IOException;

public class UnexpectedHttpStatusException extends IOException {

    private static final long serialVersionUID = 1L;

    private final int expectedStatus;
    private final int currentStatus;
    private final String reason;
    private final byte[] body;
    private final DPHttpResponse response;

    public UnexpectedHttpStatusException(int expectedStatus, DPHttpResponse response) {
        super("Expecting HTTP status [" + expectedStatus + "] but got [" + (response != null ? response.getStatus() : 1) + "]");
        this.expectedStatus = expectedStatus;
        this.response = response;
        if (this.response != null) {
            this.currentStatus = response.getStatus();
            this.reason = response.getReason();
            this.body = response.getContent();
        } else {
            this.currentStatus = -1;
            this.reason = null;
            this.body = null;
        }
    }

    public int getExpectedStatus() {
        return expectedStatus;
    }

    public int getCurrentStatus() {
        return currentStatus;
    }

    public String getReason() {
        return this.reason;
    }

    public byte[] getBody() {
        return this.body;
    }

    public DPHttpResponse getResponse() {
        return response;
    }
}
