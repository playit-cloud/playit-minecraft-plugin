package gg.playit.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import gg.playit.api.actions.Action;
import gg.playit.api.actions.CreateTunnel;
import gg.playit.api.actions.ListFromAccount;
import gg.playit.api.actions.SignAgentRegister;
import gg.playit.api.models.AccountTunnels;
import gg.playit.api.models.Created;
import gg.playit.api.models.SignedData;
import org.apache.commons.codec.DecoderException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ApiClient {
    //    private static final String API_URL = "https://api.playit.cloud";
    private static final String API_URL = "http://localhost:8080";

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

    private <T> T execute(Action action, Class<T> responseType) throws IOException {
        var requestBody = mapper.writeValueAsString(action);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + action.getPath()))
                .header("Authorization", String.format("agent-key %s", this.secret))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            var responseBody = response.body();

            if (response.statusCode() != 200) {
                throw new IOException("Got API error, request: "
                        + action.getPath() + ", "
                        + action.getType() + ": "
                        + response.statusCode()
                        + ", req body: " + requestBody
                        + ", res body: " + responseBody
                );
            }

            return mapper.readValue(responseBody, responseType);
        } catch (InterruptedException e) {
            throw new IOException("client send / read interrupted", e);
        }
    }
}
