package gg.playit.api.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PlayitPop {
    Any("Any"),
    USLosAngeles("USLosAngeles"),
    USSeattle("USSeattle"),
    USDallas("USDallas"),
    USMiami("USMiami"),
    USChicago("USChicago"),
    USNewJersey("USNewJersey"),
    CanadaToronto("CanadaToronto"),
    Mexico("Mexico"),
    BrazilSaoPaulo("BrazilSaoPaulo"),
    Spain("Spain"),
    London("London"),
    Germany("Germany"),
    Poland("Poland"),
    Sweden("Sweden"),
    IndiaDelhi("IndiaDelhi"),
    IndiaMumbai("IndiaMumbai"),
    IndiaBangalore("IndiaBangalore"),
    Singapore("Singapore"),
    Tokyo("Tokyo"),
    Sydney("Sydney"),
    SantiagoChile("SantiagoChile"),
    Israel("Israel"),
    Romania("Romania"),
    USNewYork("USNewYork"),
    USDenver("USDenver"),
    Staging("Staging");

    private final String value;

    PlayitPop(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
