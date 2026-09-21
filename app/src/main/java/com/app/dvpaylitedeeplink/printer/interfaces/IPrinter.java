package com.app.dvpaylitedeeplink.printer.interfaces;

public interface IPrinter {
    void onPrintDone();
    void onPrintFailed(int errorCode);
}
