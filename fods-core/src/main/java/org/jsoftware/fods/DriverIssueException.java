package org.jsoftware.fods;

public class DriverIssueException extends RuntimeException {

    public DriverIssueException(String message, Exception init) {
        super(message, init);
    }
}
