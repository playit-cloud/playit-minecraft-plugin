package gg.playit.minecraft;

import gg.playit.api.ApiClient;
import gg.playit.api.ApiClientException;
import gg.playit.api.model.ApiFail;
import gg.playit.api.model.ApiResultError;
import gg.playit.api.model.ApiSuccess;
import gg.playit.api.model.ApiSuccessNoFail;
import gg.playit.api.model.enums.AccountStatus;
import gg.playit.api.model.enums.ClaimAgentType;
import gg.playit.api.model.enums.ClaimExchangeError;
import gg.playit.api.model.enums.ClaimSetupError;
import gg.playit.api.model.enums.ClaimSetupResponse;
import gg.playit.api.model.request.ReqClaimExchange;
import gg.playit.api.model.request.ReqClaimSetup;
import gg.playit.api.model.response.AgentNotice;
import gg.playit.api.model.response.AgentRunDataV1;
import gg.playit.api.model.response.AgentSecretKey;
import gg.playit.minecraft.utils.Hex;

import java.io.IOException;
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

    public PlayitKeysSetup(String secretKey, AtomicInteger state, String version) {
        keys.secretKey = secretKey;
        this.state = state;
        this.version = version != null && version.length() > 64 ? version.substring(0, 64) : (version != null ? version : "0.0.0");
    }

    private final PlayitKeys keys = new PlayitKeys();
    private final String version;
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

    public PlayitKeys getKeys() {
        return keys;
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
                    log.info("please visit: https://playit.gg/claim/" + claimCode);
                }

                try {
                    var setupResult = openClient.claimSetup(new ReqClaimSetup(claimCode, ClaimAgentType.SelfManaged, version));
                    if (setupResult instanceof ApiSuccess<ClaimSetupResponse, ?> success) {
                        var response = success.data();
                        switch (response) {
                            case UserAccepted -> {
                                log.info("claim accepted, exchanging for secret");
                                var exchangeResult = openClient.claimExchange(new ReqClaimExchange(claimCode));
                                if (exchangeResult instanceof ApiSuccess<AgentSecretKey, ?> exchSuccess) {
                                    keys.secretKey = exchSuccess.data().secret_key();
                                    state.compareAndSet(STATE_MISSING_SECRET, STATE_CHECKING_SECRET);
                                } else if (exchangeResult instanceof ApiFail<?, ClaimExchangeError> fail) {
                                    keys.secretKey = null;
                                    log.warning("claim exchange failed: " + fail.data() + " - to claim visit: https://playit.gg/claim/" + claimCode);
                                } else if (exchangeResult instanceof ApiResultError<?, ?> err) {
                                    keys.secretKey = null;
                                    log.warning("claim exchange API error: " + err.data() + " - to claim visit: https://playit.gg/claim/" + claimCode);
                                } else {
                                    keys.secretKey = null;
                                    log.warning("claim exchange failed: unexpected result - to claim visit: https://playit.gg/claim/" + claimCode);
                                }
                            }
                            case WaitingForUserVisit, WaitingForUser -> {
                                keys.secretKey = null;
                            }
                            case UserRejected -> {
                                keys.secretKey = null;
                                state.compareAndSet(STATE_MISSING_SECRET, STATE_ERROR);
                                log.warning("claim rejected by user - to claim visit: https://playit.gg/claim/" + claimCode);
                            }
                        }
                    } else if (setupResult instanceof ApiFail<?, ClaimSetupError> fail) {
                        keys.secretKey = null;
                        var err = fail.data();
                        if (err == ClaimSetupError.CodeExpired || err == ClaimSetupError.InvalidCode) {
                            claimCode = null;
                            log.warning("claim setup failed: " + err + ", regenerating claim code");
                        } else {
                            log.warning("claim setup failed: " + err + " - to claim visit: https://playit.gg/claim/" + claimCode);
                        }
                    } else if (setupResult instanceof ApiResultError<?, ?> err) {
                        keys.secretKey = null;
                        log.warning("claim setup API error: " + err.data() + " - to claim visit: https://playit.gg/claim/" + claimCode);
                    } else {
                        keys.secretKey = null;
                        log.warning("claim setup failed: unexpected result - to claim visit: https://playit.gg/claim/" + claimCode);
                    }
                } catch (ApiClientException e) {
                    keys.secretKey = null;
                    String reason = e.getStatusCode() >= 0
                            ? "HTTP " + e.getStatusCode() + (e.getResponseBody() != null ? ": " + e.getResponseBody() : "")
                            : e.getMessage();
                    log.warning("claim setup request failed: " + reason + " - to claim visit: https://playit.gg/claim/" + claimCode);
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

                    log.info("secret verified, ready to connect");
                    return keys;
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

                    throw new IOException("API error: " + e.getMessage(), e);
                }
            }
            default -> {
                return null;
            }
        }
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
