package com.overstreamapp.osc;


public class OscReadException extends Exception {

    public OscReadException() {
    }

    public OscReadException(String msg) {
        super(msg);
    }

    public OscReadException(Throwable cause) {
        super(cause);
    }

    public OscReadException(String message, Throwable cause) {
        super(message, cause);
    }

}
