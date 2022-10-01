package gg.playit.api;

import java.io.IOException;

public class ApiError extends IOException {
    public final String requestBody;
    public final String responseBody;
    public final int statusCode;

    public ApiError(int statusCode, String requestBody, String responseBody) {
        super("Api error code: " + statusCode + ", req: " + requestBody + ", res: " + responseBody);
        this.requestBody = requestBody;
        this.responseBody = responseBody;
        this.statusCode = statusCode;
    }
}
