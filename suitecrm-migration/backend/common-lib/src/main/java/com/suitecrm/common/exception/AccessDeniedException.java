package com.suitecrm.common.exception;

public class AccessDeniedException extends RuntimeException {
    private final String resource;
    private final String action;

    public AccessDeniedException(String resource, String action) {
        super(String.format("Access denied: cannot %s on %s", action, resource));
        this.resource = resource;
        this.action = action;
    }

    public String getResource() { return resource; }
    public String getAction() { return action; }
}
