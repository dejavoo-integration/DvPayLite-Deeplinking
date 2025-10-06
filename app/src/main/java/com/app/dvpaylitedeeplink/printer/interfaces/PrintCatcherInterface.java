package com.app.dvpaylitedeeplink.printer.interfaces;

import com.denovo.app.invokekozen.printer.models.PrintErrorResult;
import com.denovo.app.invokekozen.printer.models.PrinterData;

public interface PrintCatcherInterface {
    void onIntentCaught(PrinterData printerData);
    void onIntentCaughtError(PrintErrorResult errorResult);
}
