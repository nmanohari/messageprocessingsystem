package com.echoworx.exceptions;

public class MessageException extends RuntimeException {
    public MessageException(String msg, Throwable t) {
        super(msg, t);
    }
}
