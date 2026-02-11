package gg.playit.minecraft;

import gg.playit.api.ApiClient;
import gg.playit.api.ApiClientException;
import gg.playit.api.model.ApiResult;
import gg.playit.api.model.ApiSuccess;
import gg.playit.api.model.ApiSuccessNoFail;
import gg.playit.api.model.enums.AccountStatus;
import gg.playit.api.model.enums.PlayitNetwork;
import gg.playit.api.model.enums.TunnelType;
import gg.playit.api.model.request.AccountTunnelOriginCreate;
import gg.playit.api.model.request.AgentOrigin;
import gg.playit.api.model.request.CreateTunnelEndpoint;
import gg.playit.api.model.request.ReqClaimExchange;
import gg.playit.api.model.request.ReqTunnelsCreateV1;
import gg.playit.api.model.request.TunnelProtocol;
import gg.playit.api.model.request.UseAllocRegion;
import gg.playit.api.model.response.AccountTunnelV1;
import gg.playit.api.model.response.AccountTunnelsV1;
import gg.playit.api.model.response.AgentNotice;
import gg.playit.api.model.response.AgentRunDataV1;
import gg.playit.api.model.response.AgentSecretKey;
import gg.playit.api.model.response.AgentTunnelConfig;
import gg.playit.api.model.response.ConnectAddress;
import gg.playit.api.model.response.ConnectAddr4;
import gg.playit.api.model.response.ConnectAddr6;
import gg.playit.api.model.response.ConnectAutoName;
import gg.playit.api.model.response.ConnectDomain;
import gg.playit.api.model.response.ConnectIp4;
import gg.playit.api.model.response.ConnectIp6;
import gg.playit.minecraft.utils.Hex;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class PlayitKeysSetup {
    private static final Logger log = Logger.getLogger(PlayitKeysSetup.class.getName());
    public final AtomicInteger state;
    public static final int STATE_INIT = 1;
    public static final int STATE_MISSING_SECRET = 2;
    public static final int STATE_CHECKING_SECRET = 3;
    public static final int STATE_CREATING_TUNNEL = 4;
    public static final int STATE_ERROR = 5;
    public static final int STATE_SHUTDOWN = 0;
    private final ApiClient openClient = new ApiClient(null);

    public PlayitKeysSetup(String secretKey, AtomicInteger state) {
        keys.secretKey = secretKey;
        this.state = state;
    }

    private final PlayitKeys keys = new PlayitKeys();
    private String claimCode;

    public int getState() {
        return state.get();
    }

    public void shutdown() {
        this.state.set(STATE_SHUTDOWN);
    }

    public String getClaimCode() {
        return claimCode;
    }

    public PlayitKeys progress() throws IOException {
        switch (state.get()) {
            case STATE_INIT -> {
                if (keys.secretKey == null) {
                    state.compareAndSet(STATE_INIT, STATE_MISSING_SECRET);
                    return null;
                }

                state.compareAndSet(STATE_INIT, STATE_CHECKING_SECRET);
                log.info("secret key found, checking");
                return null;
            }
            case STATE_MISSING_SECRET -> {
                if (claimCode == null) {
                    byte[] array = new byte[8];
                    new Random().nextBytes(array);
                    claimCode = Hex.encodeHexString(array);
                    log.info("secret key not set, generate claim code: " + claimCode);
                }

                log.info("trying to exchange claim code for secret");
                try {
                    var result = openClient.claimExchange(new ReqClaimExchange(claimCode));
                    if (result instanceof ApiSuccess<AgentSecretKey, ?> success) {
                        keys.secretKey = success.data().secret_key();
                        state.compareAndSet(STATE_MISSING_SECRET, STATE_CHECKING_SECRET);
                    } else {
                        keys.secretKey = null;
                    }
                } catch (ApiClientException e) {
                    keys.secretKey = null;
                }

                if (keys.secretKey == null) {
                    log.info("failed to exchange, to claim visit: https://playit.gg/mc/" + claimCode);
                }

                return null;
            }
            case STATE_CHECKING_SECRET -> {
                log.info("check secret");

                var api = new ApiClient(keys.secretKey);
                try {
                    var rundata = api.v1AgentsRundata();
                    if (!(rundata instanceof ApiSuccessNoFail<AgentRunDataV1> success)) {
                        throw new IOException("agents rundata failed: " + rundata);
                    }
                    var data = success.data();

                    keys.isGuest = data.permissions().account_status() == AccountStatus.Guest;
                    keys.isEmailVerified = data.permissions().account_status() != AccountStatus.EmailNotVerified;
                    keys.agentId = data.agent_id();
                    keys.notice = data.notices() != null && !data.notices().isEmpty() ? data.notices().get(0) : null;

                    state.compareAndSet(STATE_CHECKING_SECRET, STATE_CREATING_TUNNEL);
                    return null;
                } catch (ApiClientException e) {
                    if (e.getStatusCode() == 401 || e.getStatusCode() == 400) {
                        if (claimCode == null) {
                            log.info("secret key invalid, starting over");
                            state.compareAndSet(STATE_CHECKING_SECRET, STATE_MISSING_SECRET);
                        } else {
                            state.compareAndSet(STATE_CHECKING_SECRET, STATE_ERROR);
                            log.info("secret failed verification after creating, moving to error state");
                        }

                        return null;
                    }

                    throw new IOException("API error", e);
                }
            }
            case STATE_CREATING_TUNNEL -> {
                var api = new ApiClient(keys.secretKey);

                var tunnelsResult = api.v1TunnelsList();
                if (!(tunnelsResult instanceof ApiSuccessNoFail<AccountTunnelsV1> tunnelsSuccess)) {
                    throw new IOException("tunnels list failed: " + tunnelsResult);
                }
                var tunnels = tunnelsSuccess.data();
                keys.tunnelAddress = null;

                if (tunnels.tunnels() != null) {
                    for (AccountTunnelV1 tunnel : tunnels.tunnels()) {
                        if (tunnel.tunnel_type() == TunnelType.MinecraftJava) {
                            keys.tunnelAddress = extractDisplayAddress(tunnel);
                            if (keys.tunnelAddress != null) {
                                log.info("found minecraft java tunnel: " + keys.tunnelAddress);
                                return keys;
                            }
                        }
                    }
                }

                log.info("create new minecraft java tunnel");

                var create = new ReqTunnelsCreateV1(
                        null,
                        new TunnelProtocol.TunnelTypeDetail(TunnelType.MinecraftJava),
                        new AccountTunnelOriginCreate.Agent(new AgentOrigin(keys.agentId, new AgentTunnelConfig())),
                        new CreateTunnelEndpoint.Region(new UseAllocRegion(PlayitNetwork.Global, null)),
                        true,
                        null
                );

                api.v1TunnelsCreate(create);

                return null;
            }
            default -> {
                return null;
            }
        }
    }

    private static String extractDisplayAddress(AccountTunnelV1 tunnel) {
        List<ConnectAddress> addrs = tunnel.connect_addresses();
        if (addrs == null || addrs.isEmpty()) return null;
        var first = addrs.get(0);
        if (first instanceof ConnectAddress.Ip4 ip4) return ip4.value().address() + ":" + ip4.value().default_port();
        if (first instanceof ConnectAddress.Ip6 ip6) return ip6.value().address() + ":" + ip6.value().default_port();
        if (first instanceof ConnectAddress.Addr4 addr4) return addr4.value().address();
        if (first instanceof ConnectAddress.Addr6 addr6) return addr6.value().address();
        if (first instanceof ConnectAddress.Auto auto) return auto.value().address();
        if (first instanceof ConnectAddress.Domain domain) return domain.value().address();
        return null;
    }

    public static class PlayitKeys {
        public String secretKey;
        public String agentId;
        public String tunnelAddress;
        public boolean isGuest;
        public boolean isEmailVerified;
        public AgentNotice notice;
    }
}
