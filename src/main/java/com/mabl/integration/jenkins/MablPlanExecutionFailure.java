package com.mabl.integration.jenkins;

/**
 * mabl Plan failed to complete due to test failure
 *
 */
class MablPlanExecutionFailure extends Exception {

    public MablPlanExecutionFailure(String message) {
        super(message);
    }

    public MablPlanExecutionFailure(String message, Throwable cause) {
        super(message, cause);
    }
}