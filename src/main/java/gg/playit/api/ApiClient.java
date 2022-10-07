package gg.playit.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import gg.playit.api.actions.*;
import gg.playit.api.models.*;
import gg.playit.minecraft.utils.DecoderException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;

public class ApiClient {
        private static final String API_URL = "https://api.playit.cloud";
//    private static final String API_URL = "http://localhost:8080";

    private final String secret;
    private final ObjectMapper mapper = new ObjectMapper();
    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    public ApiClient(String secret) {
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
            ExchangeClaimForSecret req = new ExchangeClaimForSecret();
            req.claimKey = claim;
            AgentSecret res = execute(req, AgentSecret.class);
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
        String requestBody = mapper.writeValueAsString(action);
        HttpPost post = new HttpPost(API_URL + action.getPath());
        post.addHeader("Content-Type", "application/json");
        post.addHeader("Accept", "application/json");

        if (secret != null) {
            post.addHeader("Authorization", String.format("agent-key %s", this.secret));
        }
        post.setEntity(new StringEntity(requestBody));

        HttpResponse response = httpClient.execute(post);
        String responseBody = EntityUtils.toString(response.getEntity());

        if (response.getStatusLine().getStatusCode() != 200) {
            throw new ApiError(response.getStatusLine().getStatusCode(), requestBody, responseBody);
        }

        return mapper.readValue(responseBody, responseType);
    }
}
