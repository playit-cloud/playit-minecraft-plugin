package gg.playit.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import gg.playit.api.model.*;
import gg.playit.api.model.enums.*;
import gg.playit.api.model.request.*;
import gg.playit.api.model.response.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ApiClient {
    private static final String API_URL = "https://api.playit.gg";

    private final HttpClient client;
    private final String agentSecret;
    private final ObjectMapper mapper;

    public ApiClient(String agentSecret) {
        this.client = HttpClient.newHttpClient();
        this.agentSecret = agentSecret;
        this.mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public ApiResultNoFail<AccountTunnelsV1> v1TunnelsList() throws ApiClientException {
        return _callNoFail("/v1/tunnels/list", "{}", new TypeReference<>() {});
    }

    public ApiResult<ObjectId, TunnelCreateErrorV1> v1TunnelsCreate(ReqTunnelsCreateV1 req) throws ApiClientException {
        return _call("/v1/tunnels/create", req, new TypeReference<>() {});
    }

    public ApiResult<Empty, TunnelConfigError> v1TunnelsConfig(ReqTunnelsConfigV1 req) throws ApiClientException {
        return _call("/v1/tunnels/config", req, new TypeReference<>() {});
    }

    public ApiResult<Empty, TunnelPropSetError> v1TunnelsPropset(ReqTunnelsPropset req) throws ApiClientException {
        return _call("/v1/tunnels/propset", req, new TypeReference<>() {});
    }

    public ApiResult<Empty, TunnelTypeSetError> v1TunnelsTypeset(ReqTunnelsTypeset req) throws ApiClientException {
        return _call("/v1/tunnels/typeset", req, new TypeReference<>() {});
    }

    public ApiResultNoFail<AgentRunDataV1> v1AgentsRundata() throws ApiClientException {
        return _callNoFail("/v1/agents/rundata", "{}", new TypeReference<>() {});
    }

    public ApiResult<SchemaData, SchemaGetError> v1SchemasGet(ReqSchemasGetV1 req) throws ApiClientException {
        return _call("/v1/schemas/get", req, new TypeReference<>() {});
    }

    public ApiResultNoFail<PlayitPops> infoPops() throws ApiClientException {
        return _callNoFail("/info/pops", "{}", new TypeReference<>() {});
    }

    public ApiResult<WebSession, SigninFail> loginSignin(ReqLoginSignin req) throws ApiClientException {
        return _call("/login/signin", req, new TypeReference<>() {});
    }

    public ApiResultNoFail<ClearWebSession> loginClearcookie() throws ApiClientException {
        return _callNoFail("/login/clearcookie", "{}", new TypeReference<>() {});
    }

    public ApiResult<WebSession, LoginCreateGuestError> loginCreateGuest() throws ApiClientException {
        return _call("/login/create/guest", "{}", new TypeReference<>() {});
    }

    public ApiResult<WebSession, GuestLoginError> loginGuest() throws ApiClientException {
        return _call("/login/guest", "{}", new TypeReference<>() {});
    }

    public ApiResult<WebSession, PasswordResetError> loginResetPassword(ReqLoginResetPassword req) throws ApiClientException {
        return _call("/login/reset/password", req, new TypeReference<>() {});
    }

    public ApiResultNoFail<Empty> loginResetSend(ReqLoginResetSend req) throws ApiClientException {
        return _callNoFail("/login/reset/send", req, new TypeReference<>() {});
    }

    public ApiResult<ClaimSetupResponse, ClaimSetupError> claimSetup(ReqClaimSetup req) throws ApiClientException {
        return _call("/claim/setup", req, new TypeReference<>() {});
    }

    public ApiResult<AgentSecretKey, ClaimExchangeError> claimExchange(ReqClaimExchange req) throws ApiClientException {
        return _call("/claim/exchange", req, new TypeReference<>() {});
    }

    public ApiResult<QueryRegion, QueryRegionError> queryRegion(ReqQueryRegion req) throws ApiClientException {
        return _call("/query/region", req, new TypeReference<>() {});
    }

    public ApiResult<ObjectId, TunnelCreateError> tunnelsCreate(ReqTunnelsCreate req) throws ApiClientException {
        return _call("/tunnels/create", req, new TypeReference<>() {});
    }

    public ApiResultNoFail<AccountTunnels> tunnelsList(ReqTunnelsList req) throws ApiClientException {
        return _callNoFail("/tunnels/list", req, new TypeReference<>() {});
    }

    public ApiResult<Empty, UpdateError> tunnelsUpdate(ReqTunnelsUpdate req) throws ApiClientException {
        return _call("/tunnels/update", req, new TypeReference<>() {});
    }

    public ApiResult<Empty, DeleteError> tunnelsDelete(ReqTunnelsDelete req) throws ApiClientException {
        return _call("/tunnels/delete", req, new TypeReference<>() {});
    }

    public ApiResult<Empty, TunnelRenameError> tunnelsRename(ReqTunnelsRename req) throws ApiClientException {
        return _call("/tunnels/rename", req, new TypeReference<>() {});
    }

    public ApiResult<Empty, TunnelsFirewallAssignError> tunnelsFirewallAssign(ReqTunnelsFirewallAssign req) throws ApiClientException {
        return _call("/tunnels/firewall/assign", req, new TypeReference<>() {});
    }

    public ApiResult<Empty, TunnelRatelimitError> tunnelsRatelimit(ReqTunnelsRatelimit req) throws ApiClientException {
        return _call("/tunnels/ratelimit", req, new TypeReference<>() {});
    }

    public ApiResult<Empty, TunnelEnableError> tunnelsEnable(ReqTunnelsEnable req) throws ApiClientException {
        return _call("/tunnels/enable", req, new TypeReference<>() {});
    }

    public ApiResult<Empty, TunnelProxySetError> tunnelsProxySet(ReqTunnelsProxySet req) throws ApiClientException {
        return _call("/tunnels/proxy/set", req, new TypeReference<>() {});
    }

    public ApiResult<Empty, AgentRenameError> agentsRename(ReqAgentsRename req) throws ApiClientException {
        return _call("/agents/rename", req, new TypeReference<>() {});
    }

    public ApiResult<Empty, AgentRoutingSetError> agentsRoutingSet(ReqAgentsRoutingSet req) throws ApiClientException {
        return _call("/agents/routing/set", req, new TypeReference<>() {});
    }

    public ApiResult<AgentRouting, AgentRoutingGetError> agentsRoutingGet(ReqAgentsRoutingGet req) throws ApiClientException {
        return _call("/agents/routing/get", req, new TypeReference<>() {});
    }

    public ApiResultNoFail<AgentRunData> agentsRundata() throws ApiClientException {
        return _callNoFail("/agents/rundata", "{}", new TypeReference<>() {});
    }

    public ApiResultNoFail<Domains> domainsList() throws ApiClientException {
        return _callNoFail("/domains/list", "{}", new TypeReference<>() {});
    }

    public ApiResult<SignedAgentKey, ProtoRegisterError> protoRegister(ReqProtoRegister req) throws ApiClientException {
        return _call("/proto/register", req, new TypeReference<>() {});
    }

    private <S, F> ApiResult<S, F> _call(String path, Object requestBody, TypeReference<ApiResult<S, F>> typeRef) throws ApiClientException {
        return _callInternal(path, requestBody, typeRef);
    }

    private <S> ApiResultNoFail<S> _callNoFail(String path, Object requestBody, TypeReference<ApiResultNoFail<S>> typeRef) throws ApiClientException {
        return _callInternal(path, requestBody, typeRef);
    }

    private <T> T _callInternal(String path, Object requestBody, TypeReference<T> typeRef) throws ApiClientException {
        String bodyStr;
        try {
            bodyStr = requestBody instanceof String ? (String) requestBody : mapper.writeValueAsString(requestBody);
        } catch (IOException e) {
            throw new ApiClientException(e);
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + path))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");

        if (agentSecret != null) {
            builder = builder.header("Authorization", "agent-key " + agentSecret);
        }

        HttpRequest request = builder
                .POST(HttpRequest.BodyPublishers.ofString(bodyStr))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();

            if (response.statusCode() != 200) {
                throw new ApiClientException(response.statusCode(), path, bodyStr, responseBody);
            }

            return mapper.readValue(responseBody, typeRef);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiClientException(e);
        } catch (IOException e) {
            throw new ApiClientException(e);
        }
    }
}
