package com.app.dvpaylitedeeplink.swipereader.listeners;

public interface SwipeResult {
    void onSuccess(String response);
    void onFailure(String response);
    void onTimeOut(String response);
}