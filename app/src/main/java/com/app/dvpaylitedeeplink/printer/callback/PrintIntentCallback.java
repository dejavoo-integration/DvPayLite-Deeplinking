package com.app.dvpaylitedeeplink.printer.callback;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.denovo.app.invokekozen.printer.models.PrintErrorResult;
import com.denovo.app.invokekozen.printer.models.PrintResult;
import com.denovo.app.invokekozen.printer.utils.IntentPrinterConstants;
import com.google.gson.Gson;

public class PrintIntentCallback {

    private Activity activity = null;
    private final IntentPrinterConstants intentConstants = new IntentPrinterConstants();

    public PrintIntentCallback(Activity activity) {
        this.activity = activity;
    }

    public void sendCallBackData(PrintResult printResult) {
        Gson gson = new Gson();
        String printResultJson = gson.toJson(printResult);
        Log.e(intentConstants.TAG, "printResultJson:" + printResultJson);

        Intent intentWithResult = new Intent(intentConstants.ACTION_INTENT_CALLBACK);
        intentWithResult.putExtra(intentConstants.PRINT_RESULT, printResultJson);
        activity.setResult(intentConstants.RESULT_CLOSE, intentWithResult);
        activity.finish();
    }

    public void sendErrorData(PrintErrorResult errorResult){
        Gson gson = new Gson();
        String errorResultJson = gson.toJson(errorResult);
        Log.e(intentConstants.TAG, "errorResultJson:" + errorResultJson);

        Intent intentWithResult = new Intent(intentConstants.ACTION_INTENT_CALLBACK);
        intentWithResult.putExtra(intentConstants.ERROR_RESULT, errorResult);
        activity.setResult(intentConstants.RESULT_CLOSE, intentWithResult);
        activity.finish();
    }



}
