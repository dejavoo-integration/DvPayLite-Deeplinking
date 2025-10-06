package com.app.dvpaylitedeeplink.printer.launcher;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.denovo.app.invokekozen.printer.interfaces.IPrinter;
import com.denovo.app.invokekozen.printer.interfaces.PrintLauncherInterface;
import com.denovo.app.invokekozen.printer.interfaces.PrinterDataException;
import com.denovo.app.invokekozen.printer.interfaces.PrinterException;
import com.denovo.app.invokekozen.printer.interfaces.PrinterRollException;
import com.denovo.app.invokekozen.printer.models.PrintErrorResult;
import com.denovo.app.invokekozen.printer.models.PrintResult;
import com.denovo.app.invokekozen.printer.print.PrintDataParserClass;
import com.denovo.app.invokekozen.printer.print.PrintReceipt;
import com.denovo.app.invokekozen.printer.utils.IntentPrinterConstants;

public class IntentPrintApplication {

    private Context context = null;
    private PrintLauncherInterface launchInterface = null;

    public final String KOZEN_POS_MODEL_P3 = "P3";
    public final String KOZEN_POS_MODEL_P1 = "P1";
    public final String KOZEN_POS_MODEL_P8 = "P8";
    public final String KOZEN_POS_MODEL_P10 = "P10";
    public final String WIZOR_POS_MODEL_Q2 = "Q2";
    private final IntentPrinterConstants intentConstants = new IntentPrinterConstants();

    public IntentPrintApplication(Context context) {
        this.context = context;
    }

    public void setLaunchInterface(PrintLauncherInterface launchInterface) {
        this.launchInterface = launchInterface;
    }

    public void launchPrinter(String printerReq) {
        try {
            if (printerReq != null) {
                if(isPrinterHardwareAvailable()){
                    PrintDataParserClass printDataParser = new PrintDataParserClass();
                    if (isKozenModel()) {
                        printExtData(printDataParser.parsePrinterData(printerReq).toString(), context);
                    } else if (isWizorModel()) {
                        printExtDataWizor(printDataParser.parsePrinterData(printerReq).toString(), context);
                    }
                } else {
                    onPrintFailed(intentConstants.PRINTER_NA, intentConstants.PRINTER_NA_DES);
                }
            } else {
                onPrintFailed(intentConstants.INPUT_OBJ_NULL, intentConstants.INPUT_OBJ_NULL_DES);
            }
        } catch (Exception e) {
            Log.e(intentConstants.TAG, "Exception:" + e.getMessage());
            onPrintFailed(intentConstants.APP_NOT_LAUNCHED, intentConstants.APP_NOT_LAUNCHED_DES);
        }
    }

    private boolean isPrinterHardwareAvailable() {
        String model = Build.MODEL;
        switch (model) {
            case KOZEN_POS_MODEL_P3:
            case KOZEN_POS_MODEL_P1:
            case KOZEN_POS_MODEL_P8:
            case KOZEN_POS_MODEL_P10:
            case WIZOR_POS_MODEL_Q2:
                return true;
            default:
                return false;
        }
    }
    private boolean isWizorModel() {
        String model = Build.MODEL;
        switch (model) {
            case WIZOR_POS_MODEL_Q2:
                return true;
            default:
                return false;
        }
    }

    private boolean isKozenModel() {
        String model = Build.MODEL;
        switch (model) {
            case KOZEN_POS_MODEL_P3:
            case KOZEN_POS_MODEL_P1:
            case KOZEN_POS_MODEL_P8:
            case KOZEN_POS_MODEL_P10:
                return true;
            default:
                return false;
        }
    }

    private void printExtData(String printFormatResult, Context context) {
        PrintReceipt printReceipt = new PrintReceipt();
        printReceipt.kozenPrintData(printFormatResult,context, new IPrinter(){

            @Override
            public void onPrintDone() {
                Log.d("Printer","onPrintDone");
                PrintResult printResult = new PrintResult();
                printResult.setPrintStatus(intentConstants.PRINT_SUCCESS);
                printResult.setPrintMessage(intentConstants.PRINT_SUCCESS_MSG);
                launchInterface.onPrintSuccess(printResult);
            }

            @Override
            public void onPrintFailed(int errorCode) {
                Log.d("Printer","onPrintFailed::"+errorCode);
                PrintErrorResult printErrorResult = new PrintErrorResult();
                printErrorResult.setErrorCode(errorCode);
                switch (errorCode){
                    case -6:
                        printErrorResult.setErrorMessage("Printer is in state 4");
                        printErrorResult.setErrorException(new PrinterException());
                        break;
                    case -1:
                        printErrorResult.setErrorMessage("Invalid printer data");
                        printErrorResult.setErrorException(new PrinterDataException());
                        break;
                    case -2:
                        printErrorResult.setErrorMessage("Unknown exception while printing");
                        printErrorResult.setErrorException(new PrinterException());
                        break;
                    case -3:
                        printErrorResult.setErrorMessage("Invalid printer data body");
                        printErrorResult.setErrorException(new PrinterDataException());
                        break;
                    case -4:
                        printErrorResult.setErrorMessage("No paper roll");
                        printErrorResult.setErrorException(new PrinterRollException());
                        break;
                    case -5:
                        printErrorResult.setErrorMessage("Printer data limit exceeded.");
                        printErrorResult.setErrorException(new PrinterDataException());
                        break;
                    default:
                        printErrorResult.setErrorMessage("Printing failed");
                        printErrorResult.setErrorException(new PrinterException());
                        break;
                }
                launchInterface.onPrintFailed(printErrorResult);
            }
        });
    }

    private void printExtDataWizor(String printFormatResult, Context context) {
        PrintReceipt printReceipt = new PrintReceipt();
        printReceipt.kozenPrintData(printFormatResult,context, new IPrinter(){

            @Override
            public void onPrintDone() {
                Log.d("Printer","onPrintDone");
                PrintResult printResult = new PrintResult();
                printResult.setPrintStatus(intentConstants.PRINT_SUCCESS);
                printResult.setPrintMessage(intentConstants.PRINT_SUCCESS_MSG);
                launchInterface.onPrintSuccess(printResult);
            }

            @Override
            public void onPrintFailed(int errorCode) {
                Log.d("Printer","onPrintFailed::"+errorCode);
                PrintErrorResult printErrorResult = new PrintErrorResult();
                printErrorResult.setErrorCode(errorCode);
                switch (errorCode){
                    case -6:
                        printErrorResult.setErrorMessage("Printer is in state 4");
                        printErrorResult.setErrorException(new PrinterException());
                        break;
                    case -1:
                        printErrorResult.setErrorMessage("Invalid printer data");
                        printErrorResult.setErrorException(new PrinterDataException());
                        break;
                    case -2:
                        printErrorResult.setErrorMessage("Unknown exception while printing");
                        printErrorResult.setErrorException(new PrinterException());
                        break;
                    case -3:
                        printErrorResult.setErrorMessage("Invalid printer data body");
                        printErrorResult.setErrorException(new PrinterDataException());
                        break;
                    case -4:
                        printErrorResult.setErrorMessage("No paper roll");
                        printErrorResult.setErrorException(new PrinterRollException());
                        break;
                    case -5:
                        printErrorResult.setErrorMessage("Printer data limit exceeded.");
                        printErrorResult.setErrorException(new PrinterDataException());
                        break;
                    default:
                        printErrorResult.setErrorMessage("Printing failed");
                        printErrorResult.setErrorException(new PrinterException());
                        break;
                }
                launchInterface.onPrintFailed(printErrorResult);
            }
        });
    }

    private void onPrintFailed(int errorCode, String errorMessage) {
        if (launchInterface != null) {
            PrintErrorResult printErrorResult = new PrintErrorResult();
            printErrorResult.setErrorCode(errorCode);
            printErrorResult.setErrorMessage(errorMessage);
            printErrorResult.setErrorException(new PrinterException());
            launchInterface.onPrintFailed(printErrorResult);
        }
    }
}
