package com.app.dvpaylitedeeplink.swipereader.common;

import android.content.Context;
import android.os.Build;

import com.denovo.app.invokekozen.swipereader.common.ReaderResponse;
import com.denovo.app.invokekozen.swipereader.helpers.Constants;
import com.denovo.app.invokekozen.swipereader.helpers.Utils;
import com.denovo.app.invokekozen.swipereader.kozen.KozenMsrReader;
import com.denovo.app.invokekozen.swipereader.listeners.CardReader;
import com.denovo.app.invokekozen.swipereader.listeners.SwipeResult;


public class MsrReader {

    private Context context;
    private int timeOutInSec;
    private String deviceModel = Build.MODEL;
    private CardReader cardReader;


    public MsrReader(Context context, int timeOutInSec) {
        this.context = context;
        this.timeOutInSec = timeOutInSec;
    }

    private void initializeCardReader(SwipeResult swipeResult) {
        if (Utils.isKozenPOSModel(deviceModel)) {
            this.cardReader = new KozenMsrReader(context, timeOutInSec, swipeResult);
        } else {
            ReaderResponse response = new ReaderResponse("02", Constants.FAILURE, "Unable to support this device model");
            swipeResult.onFailure(response.toJson());
        }
    }

    public void read(SwipeResult swipeResult) {

        if (Utils.checkStringValue(deviceModel) && (Utils.isKozenPOSModel(deviceModel) || Utils.isWizorPOSModel(deviceModel))) {
            if (timeOutInSec < 10 || timeOutInSec > 90) {
                ReaderResponse response = new ReaderResponse("01", Constants.FAILURE, "Please update timeOut in the range of 10s - 90s");
                swipeResult.onFailure(response.toJson());
            } else {
                if (context != null) {

                    initializeCardReader(swipeResult);

                    if (cardReader != null) {
                        cardReader.read();
                    } else {
                        ReaderResponse response = new ReaderResponse("03", Constants.FAILURE, "Unable to init Card Reader");
                        swipeResult.onFailure(response.toJson());
                    }
                } else {
                    ReaderResponse response = new ReaderResponse("04", Constants.FAILURE, "Unable to init Card Reader,Context is null");
                    swipeResult.onFailure(response.toJson());
                }
            }
        } else {
            ReaderResponse response = new ReaderResponse("05", Constants.FAILURE, "Unable to support this device model");
            swipeResult.onFailure(response.toJson());
        }
    }

    public void close() {
        try {
            if (cardReader != null) {
                cardReader.close();
            }
        } catch (Exception exception) {
            Utils.logPrint('D', "card close exception: " + exception);
        }

    }

}


