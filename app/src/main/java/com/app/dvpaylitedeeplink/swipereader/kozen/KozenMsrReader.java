package com.app.dvpaylitedeeplink.swipereader.kozen;


import android.content.Context;

import com.denovo.app.invokekozen.swipereader.common.ReaderResponse;
import com.denovo.app.invokekozen.swipereader.config.AppExecutors;
import com.denovo.app.invokekozen.swipereader.helpers.Constants;
import com.denovo.app.invokekozen.swipereader.helpers.Utils;
import com.denovo.app.invokekozen.swipereader.listeners.CardReader;
import com.denovo.app.invokekozen.swipereader.listeners.SwipeResult;
import com.google.gson.JsonObject;
import com.pos.sdk.cardreader.POICardManager;
import com.pos.sdk.cardreader.PosMagCardReader;
import com.pos.sdk.utils.PosUtils;

public class KozenMsrReader implements CardReader {
    private PosMagCardReader magCardReader = null;
    private Context context;
    private int timeOutInSec;
    private SwipeResult swipeResult;

    public KozenMsrReader(Context context, int timeOutInSec, SwipeResult swipeResult) {
        this.context = context;
        this.timeOutInSec = timeOutInSec;
        this.swipeResult = swipeResult;
    }

    private boolean isClosed = false;

    @Override
    public void read() {
        if (magCardReader == null) {
            magCardReader = POICardManager.getDefault(context).getMagCardReader();
        }
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Utils.logPrint('D', "AppExecutors executed");
                    int ret = magCardReader.open();
                    Utils.logPrint('D', "Reader open: " + (ret == 0 ? "ok" : "fail"));
                    if (ret != 0) {
                        Utils.logPrint('E', "open fail");
                        ReaderResponse response = new ReaderResponse("06", Constants.FAILURE, "Failed to open card reader");
                        swipeResult.onFailure(response.toJson());
                        return;
                    }

                    boolean detected = false;

                    int iterations = (Integer) ((timeOutInSec * 1000) / 300);
                    Utils.logPrint('D', "total iteration: " + iterations);

                    for (int index = 0; index < iterations; index++) {
                        Utils.logPrint('D', "repeat iteration: " + index);

                        if (magCardReader.detect() == 0) {
                            Utils.logPrint('D', "Card detected at iteration: " + index);
                            detected = true;
                            break;
                        }
                        PosUtils.delayms(300);

                        if (index == iterations - 1 || isClosed) {
                            Utils.logPrint('D', "card reader closed before detection");
                            break;
                        }

                    }

                    if (detected) {
                        JsonObject data = readCardData();
                        if (data != null) {
                            Utils.logPrint('D', "Card detected success");
                            close();
                            ReaderResponse response = new ReaderResponse("00", Constants.SUCCESS, data.toString());
                            swipeResult.onSuccess(response.toJson());
                        } else {
                            Utils.logPrint('D', "Card detected failure");
                            close();
                            ReaderResponse response = new ReaderResponse("07", Constants.FAILURE, "Failed to read card data");
                            swipeResult.onFailure(response.toJson());
                        }
                    } else {
                        Utils.logPrint('E', "Card detected timeout");
                        close();
                        ReaderResponse response = new ReaderResponse("08", Constants.TIMEOUT, "Card detection timeout");
                        swipeResult.onTimeOut(response.toJson());
                    }
                } catch (Exception e) {
                    Utils.logPrint('E', "Exception in card detection: "+e);
                    close();
                    ReaderResponse response = new ReaderResponse("09", Constants.FAILURE, e.toString());
                    swipeResult.onFailure(response.toJson());
                }
            }
        });
    }

    private JsonObject readCardData() {
        JsonObject jsonObject = new JsonObject();
        boolean hasData = false;

        for (int i = PosMagCardReader.CARDREADER_TRACE_INDEX_1; i <= PosMagCardReader.CARDREADER_TRACE_INDEX_3; i++) {
        byte[] traceData = magCardReader.getTraceData(i);
        if (traceData != null && traceData.length > 0) {
            String trackData = new String(traceData);
            Utils.logPrint('D', "Trace data: " + trackData);
            jsonObject.addProperty("track" + i, trackData);
            hasData = true;
        }
     }
        return hasData ? jsonObject : null;
        }


    @Override
    public void close() {
        try {
            int result = magCardReader.close();
            Utils.logPrint('D', "Reader closed: " + (result == 0 ? "ok" : "fail"));
        } catch (Exception e) {
            Utils.logPrint('D', "Card close exception: "+e);
        }
        isClosed =true;
    }
}



