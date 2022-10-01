package gg.playit.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import gg.playit.api.actions.*;
import gg.playit.api.models.*;
import org.apache.commons.codec.DecoderException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ApiClient {
        private static final String API_URL = "https://api.playit.cloud";
//    private static final String API_URL = "http://localhost:8080";

    private final HttpClient client;
    private final String secret;
    private final ObjectMapper mapper = new ObjectMapper();

    public ApiClient(String secret) {
        client = HttpClient.newHttpClient();
        this.secret = secret;
    }

    public byte[] getSignedAgentRegisterData(SignAgentRegister req) throws IOException, DecoderException {
        return execute(req, SignedData.class).decode();
    }

    public AccountTunnels listTunnels() throws IOException {
        return execute(new ListFromAccount(ListFromAccount.Type.AccountTunnels), AccountTunnels.class);
    }

    public Created createTunnel(CreateTunnel create) throws IOException {
        return execute(create, Created.class);
    }

    public String exchangeClaimForSecret(String claim) {
        try {
            var req = new ExchangeClaimForSecret();
            req.claimKey = claim;
            var res = execute(req, AgentSecret.class);
            return res.secretKey;
        } catch (IOException e) {
            return null;
        }
    }

    public SessionStatus getStatus() throws IOException {
        return execute(new GetStatus(), SessionStatus.class);
    }

    public String createGuestWebSessionKey() throws IOException {
        return execute(new CreateGuestSession(), WebSession.class).sessionKey;
    }

    private <T> T execute(Action action, Class<T> responseType) throws IOException {
        var requestBody = mapper.writeValueAsString(action);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + action.getPath()))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");

        if (secret != null) {
            builder = builder.header("Authorization", String.format("agent-key %s", this.secret));
        }

        HttpRequest request = builder
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            var responseBody = response.body();

            if (response.statusCode() != 200) {
                throw new ApiError(response.statusCode(), requestBody, responseBody);
            }

            return mapper.readValue(responseBody, responseType);
        } catch (InterruptedException e) {
            throw new IOException("client send / read interrupted", e);
        }
    }
}
