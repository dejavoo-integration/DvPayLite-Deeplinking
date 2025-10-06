package com.app.dvpaylitedeeplink.scanner;

public interface IScannerResult {
    void onSuccess(String result);
    void onFailure(String errorMessage);
}
