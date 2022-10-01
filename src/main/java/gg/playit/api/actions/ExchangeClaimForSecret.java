package gg.playit.api.actions;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ExchangeClaimForSecret implements Action {
    @JsonProperty("claim_key")
    public String claimKey;

    @Override
    public String getPath() {
        return "/agent";
    }

    @Override
    public String getType() {
        return "exchange-claim-for-secret";
    }
}
