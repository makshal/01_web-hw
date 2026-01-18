package reference.solution;

import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URLEncodedUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class Request {
    private final String method;
    private final String path;
    private final Map<String, String> headers;
    private final String body;
    private final List<NameValuePair> queryParams;
    private final List<NameValuePair> postParams;

    public Request(String method, String path, Map<String, String> headers, String body, List<NameValuePair> queryParams, List<NameValuePair> postParams) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.body = body;
        this.queryParams = queryParams;
        this.postParams = postParams;
    }

    public String method() {
        return method;
    }

    public String path() {
        return path;
    }

    public Map<String, String> headers() {
        return headers;
    }

    public String body() {
        return body;
    }

    public String getQueryParam(String name) {

        return queryParams.stream()
                .filter(p -> name.equals(p.getName()))
                .map(NameValuePair::getValue)
                .map(v -> v != null ? v : "")
                .findFirst()
                .orElse(null);

    }

    public String getQueryParams() {

        return URLEncodedUtils.format(queryParams, StandardCharsets.UTF_8);

    }

    public String getPostParam(String name) {

        return postParams.stream()
                .filter(p -> name.equals(p.getName()))
                .map(NameValuePair::getValue)
                .map(v -> v != null ? v : "")
                .findFirst()
                .orElse(null);

    }

    public String getPostParams() {

        return URLEncodedUtils.format(postParams, StandardCharsets.UTF_8);

    }

}

