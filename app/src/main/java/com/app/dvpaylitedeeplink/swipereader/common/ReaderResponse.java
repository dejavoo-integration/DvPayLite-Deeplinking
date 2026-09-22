package com.app.dvpaylitedeeplink.swipereader.common;

import com.google.gson.Gson;


public class ReaderResponse {
    private String responseCode;
    private String responseMessage;
    private String responseData;

    public ReaderResponse(String responseCode, String responseMessage, String responseData) {
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.responseData = responseData;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public String getResponseData() {
        return responseData;
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
