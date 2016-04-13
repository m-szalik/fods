package org.jsoftware.fods;

public class MBeanRegistrationException extends RuntimeException {

    public MBeanRegistrationException(String message, Exception init) {
        super(message, init);
    }
}
