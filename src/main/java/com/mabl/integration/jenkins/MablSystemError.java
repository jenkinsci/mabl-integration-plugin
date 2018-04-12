package com.mabl.integration.jenkins;

/**
 * Non test related failure
 *
 */
class MablSystemError extends Exception {

    public MablSystemError(String message) {
        super(message);
    }

    public MablSystemError(String message, Throwable cause) {
        super(message, cause);
    }
}