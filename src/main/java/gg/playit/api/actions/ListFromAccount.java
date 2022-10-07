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
            if (this == AccountTunnels) {
                return "list-account-tunnels";
            }
            throw new IllegalStateException();
        }
    }
}
