package com.mabl.integration.jenkins;

/**
 * Non test related failure
 *
 */
class MablSystemException extends Exception {

    public MablSystemException(String message) {
        super(message);
    }

    public MablSystemException(String format, Object ... args) {
        super(String.format(format, args));
    }

    public MablSystemException(String message, Throwable cause) {
        super(message, cause);
    }
}