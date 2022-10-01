package gg.playit.api.actions;

public class ListFromAccount implements Action {
    private final Type type;

    public ListFromAccount(Type type) {
        this.type = type;
    }

    @Override
    public String getPath() {
        return "/account";
    }

    @Override
    public String getType() {
        return type.toString();
    }

    public enum Type {
        AccountTunnels;

        @Override
        public String toString() {
            return switch (this) {
                case AccountTunnels -> "list-account-tunnels";
            };
        }
    }
}
