package com.rapid7.insightappsec.mock;

import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.impl.EnglishReasonPhraseCatalog;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;

public class MockHttpResponse extends BasicHttpResponse {

    MockHttpResponse(StatusLine statusline) {
        super(statusline);
    }

    public static MockHttpResponse create(int statusCode) {
        String reasonPhrase = EnglishReasonPhraseCatalog.INSTANCE.getReason(statusCode, null);
        StatusLine statusLine = new BasicStatusLine(HttpVersion.HTTP_1_1, statusCode, reasonPhrase);

        return new MockHttpResponse(statusLine);
    }

}
