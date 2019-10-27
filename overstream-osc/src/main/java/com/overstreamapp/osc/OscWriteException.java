package com.overstreamapp.osc;


public class OscWriteException extends Exception {

    public OscWriteException() {
    }

    public OscWriteException(String msg) {
        super(msg);
    }

    public OscWriteException(Throwable cause) {
        super(cause);
    }

    public OscWriteException(String message, Throwable cause) {
        super(message, cause);
    }

}
