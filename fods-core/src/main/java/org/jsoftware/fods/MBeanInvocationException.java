package org.jsoftware.fods;

public class MBeanInvocationException extends RuntimeException {

    public MBeanInvocationException(String message, Exception init) {
        super(message, init);
    }
}
