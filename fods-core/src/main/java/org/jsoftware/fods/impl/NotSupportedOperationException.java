package org.jsoftware.fods.impl;

/**
 * Operation not supported
 * @author szalik
 */
public class NotSupportedOperationException extends RuntimeException {

    public NotSupportedOperationException() {
        super("Not supported by FoDS.");
    }
}
