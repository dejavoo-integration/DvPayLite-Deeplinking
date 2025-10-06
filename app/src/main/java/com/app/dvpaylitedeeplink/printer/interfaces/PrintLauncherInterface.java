package com.app.dvpaylitedeeplink.printer.interfaces;

import com.denovo.app.invokekozen.printer.models.PrintErrorResult;
import com.denovo.app.invokekozen.printer.models.PrintResult;

public interface PrintLauncherInterface {
    void onPrintSuccess(PrintResult printResult);
    void onPrintFailed(PrintErrorResult errorResult);
}
