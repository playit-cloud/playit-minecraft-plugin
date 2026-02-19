package gg.playit.api.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PlayitNetwork {
    Global("global"),
    NorthAmerica("north-america"),
    Europe("europe"),
    Asia("asia"),
    India("india"),
    SouthAmerica("south-america"),
    Chile("chile"),
    SeattleWashington("seattle-washington"),
    LosAngelesCalifornia("los-angeles-california"),
    DenverColorado("denver-colorado"),
    DallasTexas("dallas-texas"),
    ChicagoIllinois("chicago-illinois"),
    NewYork("new-york"),
    NaReserved1("_NaReserved1"),
    NaReserved2("_NaReserved2"),
    UnitedKingdom("united-kingdom"),
    Germany("germany"),
    Sweden("sweden"),
    Poland("poland"),
    Romania("romania"),
    Test("_Test"),
    Japan("japan"),
    Australia("australia");

    private final String value;

    PlayitNetwork(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
