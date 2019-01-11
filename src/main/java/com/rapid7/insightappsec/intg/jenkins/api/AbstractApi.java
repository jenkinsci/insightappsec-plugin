package com.rapid7.insightappsec.intg.jenkins.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.rapid7.insightappsec.intg.jenkins.exception.APIException;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.AbstractHttpMessage;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.rapid7.insightappsec.intg.jenkins.MappingConfiguration.OBJECT_MAPPER_INSTANCE;
import static java.lang.String.format;

public abstract class AbstractApi {

    // HEADERS

    private static final String X_API_KEY_HEADER = "x-api-key";

    // FIELDS

    private final HttpClient client;
    private final String host;
    private final String apiKey;

    protected AbstractApi(HttpClient client,
                          String host,
                          String apiKey) {
        this.client = client;
        this.host = host;
        this.apiKey = apiKey;
    }

    /**
     * POST a resource.
     * @param path The path to post to.
     * @param body The body to send in the post.
     * @return The last segment from the 'location' header in response. Indicative of the created ID.
     */
    protected String post(String path,
                          Object body) {
        try {
            URI uri = buildUri(path);
            HttpPost post = createPost(uri, body);

            HttpResponse response = client.execute(post);

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
                String locationHeader = response.getHeaders(HttpHeaders.LOCATION)[0].getValue();
                return locationHeader.substring(locationHeader.lastIndexOf('/') + 1);
            } else {
                throw new APIException(format("Error occurred during POST of [%s]. Expected status code [%s]. Response was: %n %s",
                                              body.getClass().getSimpleName(),
                                              HttpStatus.SC_CREATED,
                                              response),
                                       response);
            }
        } catch (APIException e) {
            throw e; // re-throw
        } catch (Exception e) {
            throw new APIException(format("Error occurred during POST of [%s]",
                                          body.getClass().getName()),
                                   e);
        }
    }

    /**
     * PUT a resource.
     * @param path The path to put to.
     * @param body The body to send in the put.
     */
    protected void put(String path,
                       Object body) {
        try {
            URI uri = buildUri(path);
            HttpPut put = createPut(uri, body);

            HttpResponse response = client.execute(put);

            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new APIException(format("Error occurred during PUT of [%s]. Expected status code [%s]. Response was: %n %s",
                                              body.getClass().getSimpleName(),
                                              HttpStatus.SC_OK,
                                              response),
                                       response);
            }
        } catch (APIException e) {
            throw e; // re-throw
        } catch (Exception e) {
            throw new APIException(format("Error occurred during PUT of [%s]",
                                          body.getClass().getName()),
                                   e);
        }
    }

    /**
     * GET a single resource by it's ID.
     * @param path  The path to the resource, including it's ID.
     * @param id    The ID of the resource.
     * @param clazz The class to map response content to.
     * @return the mapped resource.
     */
    protected <T> T getById(String path,
                            String id,
                            Class<T> clazz) {
        try {
            URI uri = buildUri(path);
            HttpGet get = createGet(uri);

            HttpResponse response = client.execute(get);

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String content = IOUtils.toString(response.getEntity().getContent());

                return OBJECT_MAPPER_INSTANCE.readValue(content, clazz);
            } else {
                throw new APIException(format("Error occurred during GET for [%s] with id [%s]. Expected status code [%s]. Response was: %n %s",
                                              clazz.getSimpleName(),
                                              id,
                                              HttpStatus.SC_OK,
                                              response),
                                       response);
            }
        } catch (APIException e) {
            throw e; // re-throw
        } catch (Exception e) {
            throw new APIException(format("Error occurred during GET for [%s] with id [%s]",
                                           clazz.getSimpleName(),
                                           id),
                                   e);
        }
    }

    /**
     * Collect all pages of a particular resources using a GET.
     * @param path  The path to the resources.
     * @param clazz The class to map the pages data to.
     * @return The list of all resources.
     */
    protected <T> List<T> getForAll(String path,
                                    Class<T> clazz) {
        return retrieveAll((index) -> {
            URI uri = buildUriWithIndex(path, index);

            HttpGet get = createGet(uri);

            return retrievePage(clazz, get);
        });
    }

    /**
     * Collect all pages of a particular resources using a POST with body.
     * @param path  The path to the resources.
     * @param clazz The class to map the pages data to.
     * @param body  The body to send in each request
     * @return The list of all resources.
     */
    protected <T> List<T> postForAll(String path,
                                     Class<T> clazz,
                                     Object body) {
        return retrieveAll((index) -> {
            URI uri = buildUriWithIndex(path, index);

            HttpPost post = createPost(uri, body);

            return retrievePage(clazz, post);
        });
    }

    // HELPERS

    private URI buildUri(String path) {
        return buildUri(path, new HashMap<>());
    }

    private URI buildUriWithIndex(String path,
                                  int index) {
        Map<String, String> params = new HashMap<>();

        params.put("index", String.valueOf(index));
        params.put("size", "1000"); // use max size for faster data retrieval

        return buildUri(path, params);
    }

    private URI buildUri(String path,
                         Map<String, String> params) {
        try {
            URIBuilder builder = new URIBuilder(format("https://%s/ias/v1%s", host, path));
            params.forEach(builder::addParameter);

            return builder.build();
        } catch (URISyntaxException e) {
            throw new APIException("Error occurred building URI", e);
        }
    }

    private HttpPost createPost(URI uri,
                                Object body) {
        HttpPost post = new HttpPost(uri);

        addApiKey(post);
        addBody(post, body);

        return post;
    }

    private HttpPut createPut(URI uri,
                              Object body) {
        HttpPut put = new HttpPut(uri);

        addApiKey(put);
        addBody(put, body);

        return put;
    }

    private HttpGet createGet(URI uri) {
        HttpGet get = new HttpGet(uri);

        addApiKey(get);

        return get;
    }

    private void addApiKey(AbstractHttpMessage request) {
        request.addHeader(X_API_KEY_HEADER, apiKey);
    }

    private void addBody(HttpEntityEnclosingRequestBase request,
                         Object body) {
        try {
            String json = OBJECT_MAPPER_INSTANCE.writeValueAsString(body);
            request.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
        } catch (JsonProcessingException e) {
            throw new APIException("Error occurred writing body as json", e);
        }
    }

    /**
     * Retrieve all pages of particular resource.
     * @param getPageByIndex The function to provide a page
     * @return all pages of mapped resources, as a list.
     */
    private <T> List<T> retrieveAll(Function<Integer, Page<T>> getPageByIndex) {
        int index = 0;
        Page<T> page = getPageByIndex.apply(index);
        int totalPages = page.getMetadata().getTotalPages();

        // handle eager return
        if (totalPages == 0) {
            return new ArrayList<>();
        }

        // iterate and collect
        List<T> all = new ArrayList<>(page.getData());
        index++;
        while(index < totalPages) {
            all.addAll(getPageByIndex.apply(index).getData());
            index++;
        }

        return all;
    }

    /**
     * Retrieve a page of a particular resource.
     * @param clazz     The class to map the page data to.
     * @param request   The request to invoke that will return a page.
     * @return the page of mapped resources.
     */
    private <T> Page<T> retrievePage(Class<T> clazz,
                                     HttpUriRequest request) {
        try {
            HttpResponse response = client.execute(request);

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String content = IOUtils.toString(response.getEntity().getContent());

                JavaType type = OBJECT_MAPPER_INSTANCE.getTypeFactory().constructParametricType(Page.class, clazz);

                return OBJECT_MAPPER_INSTANCE.readValue(content, type);
            } else {
                throw new APIException(format("Error occurred during retrieval of page of [%s]. Expected status code [%s]. Response was: %n %s",
                                               clazz.getSimpleName(),
                                               HttpStatus.SC_OK,
                                               response),
                                       response);
            }
        } catch (APIException e) {
            throw e; // re-throw
        } catch (Exception e) {
            throw new APIException(format("Error occurred during retrieval of page of [%s]",
                                          clazz.getSimpleName()),
                                   e);
        }
    }
}
