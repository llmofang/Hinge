package com.newrelic.agent.compile;

public class HaltBuildException extends RuntimeException {
    public HaltBuildException(String message) {
        super(message);
    }

    public HaltBuildException(Exception e) {
        super(e);
    }

    public HaltBuildException(String message, Exception e) {
        super(message, e);
    }
}

/* Location:           /home/cw/class-rewriter/class-rewriter-4.120.0.jar
 * Qualified Name:     com.newrelic.agent.compile.HaltBuildException
 * JD-Core Version:    0.6.2
 */