package org.jsoftware.fods;

public class ConnectionCreationException extends RuntimeException {

    public ConnectionCreationException(String message, Exception init) {
        super(message, init);
    }
}
