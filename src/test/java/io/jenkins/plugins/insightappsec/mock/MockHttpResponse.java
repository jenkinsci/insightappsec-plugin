package io.jenkins.plugins.insightappsec.mock;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.jenkins.plugins.insightappsec.MappingConfiguration;
import org.apache.http.Header;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.EnglishReasonPhraseCatalog;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;

import java.io.ByteArrayInputStream;

public class MockHttpResponse extends BasicHttpResponse {

    MockHttpResponse(StatusLine statusline) {
        super(statusline);
    }

    public static MockHttpResponse create(int statusCode) {
        String reasonPhrase = EnglishReasonPhraseCatalog.INSTANCE.getReason(statusCode, null);
        StatusLine statusLine = new BasicStatusLine(HttpVersion.HTTP_1_1, statusCode, reasonPhrase);

        return new MockHttpResponse(statusLine);
    }

    public static MockHttpResponse create(int statusCode,
                                          Header[] headers) {
        MockHttpResponse response = create(statusCode);

        response.setHeaders(headers);

        return response;
    }

    public static MockHttpResponse create(int statusCode,
                                          Object body) throws JsonProcessingException {
        MockHttpResponse response = create(statusCode, new Header[0]);

        String str = MappingConfiguration.OBJECT_MAPPER_INSTANCE.writeValueAsString(body);

        BasicHttpEntity entity = new BasicHttpEntity();
        entity.setContent(new ByteArrayInputStream(str.getBytes()));

        response.setEntity(entity);

        return response;
    }

}
