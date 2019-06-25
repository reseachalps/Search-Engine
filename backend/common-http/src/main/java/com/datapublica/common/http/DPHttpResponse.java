package com.datapublica.common.http;

/*
 * Copyright (C) by Data Publica, All Rights Reserved.
 */

import com.datapublica.common.http.util.JsonUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.mozilla.universalchardet.UniversalDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Response of DPHttpClient, this class is straightly serializable and implements gory details about encoding detection.
 * For the charset, the HttpClient gives the one that was inside the headers (with the content-type). If no charset is
 * given, the charset will be loaded during methods text()
 */
public class DPHttpResponse implements Serializable {
    private final transient Logger log = LoggerFactory.getLogger(getClass());
    private static final long serialVersionUID = 927626310221454519L;
    /**
     * Status of the response
     */
    private int status;
    /**
     * Reason linked to the status
     */
    private String reason;
    /**
     * URI of the resource managed by this response.
     */
    private String baseUri;
    /**
     * Http Headers as a Multimap
     */
    private final Multimap<String, String> headers = HashMultimap.create();
    /**
     * Binary content
     */
    private byte[] content;
    /**
     * If available, the content-type
     */
    private String contentType;
    /**
     * Charset of the document
     */
    private String charset;
    /**
     * IP of the proxy used to complete this request, if available
     */
    private String proxyIp;
    /**
     * Date of fetch.
     */
    private Date date;
    //
    private transient boolean fromCache = false;

    public DPHttpResponse(Date date, int status, String reason, String baseUri) {
        this.status = status;
        this.reason = reason;
        this.baseUri = baseUri;
        this.content = null;
        this.proxyIp = null;
        this.date = date;
    }

    public DPHttpResponse() {
    }

    public DPHttpResponse(DPHttpResponse toClone) {
        this.status = toClone.status;
        this.reason = toClone.reason;
        this.baseUri = toClone.baseUri;
        this.fromCache = toClone.fromCache;
        this.content = toClone.content;
        this.proxyIp = toClone.proxyIp;
        this.date = toClone.date;
        this.charset = toClone.charset;
        this.contentType = toClone.contentType;
        this.headers.putAll(toClone.headers);
    }

    public String getCharset() {
        return charset;
    }

    @JsonIgnore
    public byte[] getContent() {
        return this.content;
    }

    public int getStatus() {
        return this.status;
    }

    public String getReason() {
        return this.reason;
    }

    public String getBaseUri() {
        return this.baseUri;
    }

    public Collection<String> getHeaders(String name) {
        return this.headers.get(name);
    }

    public String getProxyIp() {
        return this.proxyIp;
    }

    public Date getDate() {
        return this.date;
    }

    public Map<String, Collection<String>> getAllHeaders() {
        return headers.asMap();
    }

    @JsonIgnore
    public boolean isFromCache() {
        return fromCache;
    }

    public String getContentType() {
        return contentType;
    }

    private static final transient Pattern COOKIE_PATTERN = Pattern.compile("([^=]+)=([^;]*);?\\s?");

    @JsonIgnore
    public Map<String, String> getCookies() {
        Map<String, String> result = Maps.newHashMap();

        for (String cookies : getHeaders("Set-Cookie")) {
            Matcher matcher = COOKIE_PATTERN.matcher(cookies);
            while (matcher.find())
                result.put(matcher.group(1), matcher.group(2));
        }

        return result;
    }


    /**
     * Returns a correctly encoded String representing the document.
     * <p>
     * The detection chain is:
     * <ul>
     * <li>ContentType charset</li>
     * <li>If ContentType is xml or html, Then detect via the &lf;?xml header</li>
     * <li>If ContentType is html, Then detect via the meta[content-type]</li>
     * <li>Mozilla's UniversalDetector</li>
     * <li>Choose utf-8</li>
     * </ul>
     *
     * @return the document
     */
    public String text() {
        detectEncoding();
        return text(charset);
    }

    private void detectEncoding() {
        if (charset != null)
            return;
        if (contentType != null && (contentType.contains("html") || contentType.contains("xml")))
            detectCharsetViaXMLHeader();
        if (charset != null)
            return;
        if (charset != null)
            return;
        detectCharsetViaUniversalDetector();
        if (charset == null) {
            log.warn("Charset not found... use utf8");
            // fuck it... it's utf8
            charset = "UTF-8";
        }
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public void addHeader(String name, String value) {
        this.headers.put(name, value);
    }

    public void setProxyIp(String proxyIp) {
        this.proxyIp = proxyIp;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setFromCache(boolean fromCache) {
        this.fromCache = fromCache;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    private String text(String charset) {
        return new String(content, Charset.forName(charset));
    }

    @SuppressWarnings("unused")
    private void setAllHeaders(Map<String, Collection<String>> allHeaders) {
        headers.clear();
        for (Map.Entry<String, Collection<String>> entries : allHeaders.entrySet()) {
            headers.putAll(entries.getKey(), entries.getValue());
        }
    }

    private transient static final Pattern charsetPattern = Pattern.compile("(?i)\\bcharset=\\s*\"?([^\\s;\"]*)");

    private static String getCharsetFromContentType(String contentType) {
        if (contentType == null)
            return null;
        Matcher m = charsetPattern.matcher(contentType);
        if (m.find()) {
            String charset = m.group(1).trim();
            if (Charset.isSupported(charset))
                return charset;
            charset = charset.toUpperCase(Locale.ENGLISH);
            if (Charset.isSupported(charset))
                return charset;
        }
        return null;
    }

    private void detectCharsetViaUniversalDetector() {
        log.debug("Detecting charset via Universal Detector");
        // charset detector
        UniversalDetector detector = new UniversalDetector(null);
        for (int i = 0; i < content.length && !detector.isDone(); i += 1024) {
            detector.handleData(content, i, Math.min(content.length - i, 1024));
        }
        detector.dataEnd();
        charset = detector.getDetectedCharset();
    }

    public JsonNode json() {
        String asText = text();
        return JsonUtil.read(asText);
    }

    public <E> E json(Class<E> clazz) {
        String asText = text();
        return JsonUtil.read(asText, clazz);
    }

    public JsonNode json(String encoding) {
        String asText = text(encoding);
        return JsonUtil.read(asText);
    }

    public <E> E json(String encoding, Class<E> clazz) {
        String asText = text(encoding);
        return JsonUtil.read(asText, clazz);
    }

    private void detectCharsetViaXMLHeader() {
        String text = text("UTF-8");
        if (text.startsWith("<?xml")) {
            String head = text.substring(0, text.indexOf('<', 1));
            Matcher m = Pattern.compile("encoding\\s*=\\s*\"([^\"]*)\"").matcher(head);
            if (m.find()) {
                charset = m.group(1);
            }
        }
    }
}
