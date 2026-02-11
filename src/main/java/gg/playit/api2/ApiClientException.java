package gg.playit.api2;

import java.io.IOException;

public class ApiClientException extends IOException {
    private final int statusCode;
    private final String requestBody;
    private final String responseBody;

    public ApiClientException(int statusCode, String requestBody, String responseBody) {
        super("API error code: " + statusCode + ", req: " + requestBody + ", res: " + responseBody);
        this.statusCode = statusCode;
        this.requestBody = requestBody;
        this.responseBody = responseBody;
    }

    public ApiClientException(Throwable cause) {
        super(cause);
        this.statusCode = -1;
        this.requestBody = null;
        this.responseBody = null;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
