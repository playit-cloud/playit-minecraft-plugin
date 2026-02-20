package gg.playit.api;

import java.io.IOException;

public class ApiClientException extends IOException {
    private final int statusCode;
    private final String requestPath;
    private final String requestBody;
    private final String responseBody;

    public ApiClientException(int statusCode, String requestPath, String requestBody, String responseBody) {
        super("API error code: " + statusCode + ", req(" + requestPath + "): " + requestBody + ", res: " + responseBody);
        this.statusCode = statusCode;
        this.requestPath = requestPath;
        this.requestBody = requestBody;
        this.responseBody = responseBody;
    }

    public ApiClientException(Throwable cause) {
        super(cause);
        this.statusCode = -1;
        this.requestPath = null;
        this.requestBody = null;
        this.responseBody = null;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
