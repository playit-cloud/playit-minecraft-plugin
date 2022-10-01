package gg.playit.messages;

public class DecodeException extends RuntimeException {
    public final String message;

    public DecodeException(String message) {
        super(message);
        this.message = message;
    }
}
