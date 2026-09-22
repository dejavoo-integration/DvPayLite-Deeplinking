package com.app.dvpaylitedeeplink.printer.models;

import java.io.Serializable;

public class PrintErrorResult implements Serializable {

    private int errorCode = 0;
    private String errorMessage;
    private Exception errorException;

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Exception getErrorException() {
        return errorException;
    }

    public void setErrorException(Exception errorException) {
        this.errorException = errorException;
    }
}
