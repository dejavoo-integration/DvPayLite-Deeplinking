package com.app.dvpaylitedeeplink.printer.catcher;

import android.content.Intent;
import android.util.Log;

import com.denovo.app.invokekozen.printer.interfaces.PrintCatcherInterface;
import com.denovo.app.invokekozen.printer.models.PrintErrorResult;
import com.denovo.app.invokekozen.printer.models.PrinterData;
import com.denovo.app.invokekozen.printer.utils.IntentPrinterConstants;
import com.denovo.app.invokekozen.printer.utils.PrinterUtility;
import com.google.gson.Gson;

public class PrintIntentCatcher {

    private PrintCatcherInterface catcherInterface = null;
    private final IntentPrinterConstants intentConstants = new IntentPrinterConstants();

    public void setPrintCatcherInterface(PrintCatcherInterface catcherInterface) {
        this.catcherInterface = catcherInterface;
    }

    public void initPrintIntent(Intent intent) {
        /*String type = intent.getType();*/
        try {
            if (intent != null) {
                String action = intent.getAction();
                Log.e(intentConstants.TAG + "action:", action);
                if (PrinterUtility.checkStringValue(action) && action.equalsIgnoreCase(intentConstants.ACTION_INTENT_DATA)) {
                    if (intent.hasExtra(intentConstants.PRINT_DATA)) {
                        Gson gson = new Gson();
                        String printResultString = intent.getExtras().getString(intentConstants.PRINT_DATA);
                        Log.e(intentConstants.TAG , "printResultString:"+printResultString);
                        PrinterData printerData = gson.fromJson(printResultString, PrinterData.class);
                        /*TransactionData transactionData = (TransactionData) intent.getSerializableExtra(intentConstants.TRANS_DATA);*/
                        if (printerData != null) {
                            if (catcherInterface != null) {
                                catcherInterface.onIntentCaught(printerData);
                            }else {
                                onIntentCaughtError(intentConstants.NULL_CATCHER_INTERFACE,intentConstants.NULL_CATCHER_INTERFACE_DES);
                            }
                        } else {
                            onIntentCaughtError(intentConstants.NULL_PRINT_DATA,intentConstants.NULL_PRINT_DATA_DES);
                        }
                    } else {
                        onIntentCaughtError(intentConstants.PRINT_DATA_KEY_EMPTY,intentConstants.PRINT_DATA_KEY_EMPTY_DES);
                    }
                } else {
                    onIntentCaughtError(intentConstants.CATCHER_ACTION_MISMATCH,intentConstants.CATCHER_ACTION_MISMATCH_DES);
                }
            } else {
                onIntentCaughtError(intentConstants.CATCHER_NULL_INTENT,intentConstants.CATCHER_NULL_INTENT_DES);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(intentConstants.TAG, "Exception:" + e.getMessage());
            onIntentCaughtError(intentConstants.UNABLE_TO_CATCH_INTENT,intentConstants.UNABLE_TO_CATCH_INTENT_DES);
        }
    }

    private void onIntentCaughtError(int errorCode, String errorMessage) {
        if (catcherInterface != null) {
            PrintErrorResult printErrorResult = new PrintErrorResult();
            printErrorResult.setErrorCode(errorCode);
            printErrorResult.setErrorMessage(errorMessage);
            catcherInterface.onIntentCaughtError(printErrorResult);
        }
    }

}
