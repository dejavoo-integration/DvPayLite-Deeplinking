package com.app.dvpaylitedeeplink.printer.models;

import java.io.Serializable;

public class PrintResult implements Serializable {

    String printStatus;
    String printMessage;

    public String getPrintStatus() {
        return printStatus;
    }

    public void setPrintStatus(String printStatus) {
        this.printStatus = printStatus;
    }

    public String getPrintMessage() {
        return printMessage;
    }

    public void setPrintMessage(String printMessage) {
        this.printMessage = printMessage;
    }
}
