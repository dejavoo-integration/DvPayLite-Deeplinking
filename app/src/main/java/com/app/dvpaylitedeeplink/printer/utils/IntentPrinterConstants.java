package com.app.dvpaylitedeeplink.printer.utils;

public class IntentPrinterConstants {

    public final String TAG = "INVOKE";
    public final String ACTION_INTENT_DATA = "ACTION_INTENT_DATA";
    public final String ACTION_INTENT_CALLBACK = "ACTION_INTENT_CALLBACK";
    public final String TRANS_DATA = "TRANS_DATA";
    public final String PRINT_DATA = "PRINT_DATA";
    public final String TRANS_RESULT = "TRANS_RESULT";
    public final String PRINT_RESULT = "PRINT_RESULT";
    public final String PRINT_SUCCESS = "SUCCESS";
    public final String PRINT_SUCCESS_MSG = "Print successful";
    public final String ERROR_RESULT = "ERROR_RESULT";
    public final String DVPAYLITE_PACKAGE_NAME = "com.denovo.app.denovopay";
    public final String IPOSGO_PACKAGE_NAME = "com.denovo.app.top";
    /*public final String LAUNCHER_ACTIVITY = ".uilayer.splash.UISplashScreen";*/
    public final String LAUNCHER_ACTIVITY = ".uilayer.intent_catcher.UIIntentCatcherActivity";


    public final int RESULT_OK = 0;
    public final int RESULT_BACK = 1;
    public final int RESULT_CLOSE = 2;

    //----INTENT APPLICATION ERROR CODES------

    public final int APP_NOT_INSTALLED = 101;
    public final String APP_NOT_INSTALLED_DES = "IPosGo App is not Installed";

    public final int APP_NOT_ENABLED = 102;
    public final String APP_NOT_ENABLED_DES = "IPosGo App is not Enabled";

    public final int APP_NOT_LAUNCHED = 103;
    public final String APP_NOT_LAUNCHED_DES = "Unable to Launch IPosGo";

    public final int CATCHER_ACTIVITY_NOT_AVAILABLE = 104;
    public final String CATCHER_ACTIVITY_NOT_AVAILABLE_DES = "IPosGo app is not Supported,Get latest version";

    public final int INPUT_OBJ_NULL = 105;
    public final String INPUT_OBJ_NULL_DES = "Null data object";


    //----HANDLE INTENT CALLBACKS ERROR CODES------

    public final int NULL_LAUNCHER_CALLBACK = 201;
    public final String NULL_LAUNCHER_CALLBACK_DES = "Null launcher callback";

    /*public final int NULL_TRANS_RESULT_FROM_CLIENT = 202;
    public final String NULL_TRANS_RESULT_FROM_CLIENT_DES = "Null Transaction Result from the Client App";

    public final int TRANS_RESULT_KEY_EMPTY = 203;
    public final String TRANS_RESULT_KEY_EMPTY_DES = "Transaction Result KEY Mismatch";

    public final int TRANS_RESULT_CODE_MISMATCH = 204;
    public final String TRANS_RESULT_CODE_MISMATCH_DES = "Transaction Result Code Mismatch";*/

    public final int CALLBACK_ACTION_MISMATCH = 205;
    public final String CALLBACK_ACTION_MISMATCH_DES = "Transaction Callback Mismatch";

    public final int CALLBACK_DATA_EMPTY = 206;
    public final String CALLBACK_DATA_EMPTY_DES = "Null Transaction Callback Data";

    public final int UNABLE_TO_HANDLE_CALLBACK = 207;
    public final String UNABLE_TO_HANDLE_CALLBACK_DES = "Unable to handle Callback data";


    //----INTENT CATCHER ERROR CODES------

    public final int NULL_CATCHER_INTERFACE = 301;
    public final String NULL_CATCHER_INTERFACE_DES = "Null Catcher Interface";

    public final int NULL_TRANS_DATA = 302;
    public final String NULL_TRANS_DATA_DES = "Null Transaction Data";

    public final int TRANS_DATA_KEY_EMPTY = 303;
    public final String TRANS_DATA_KEY_EMPTY_DES = "Transaction Data KEY Mismatch";

    public final int CATCHER_ACTION_MISMATCH = 304;
    public final String CATCHER_ACTION_MISMATCH_DES = "Catcher Action Mismatch";

    public final int CATCHER_NULL_INTENT = 305;
    public final String CATCHER_NULL_INTENT_DES = "Catcher Null Intent";

    public final int UNABLE_TO_CATCH_INTENT = 306;
    public final String UNABLE_TO_CATCH_INTENT_DES = "Unable to Catch Intent data";

    public final int NULL_PRINT_DATA = 307;
    public final String NULL_PRINT_DATA_DES = "Null Printer Data";

    public final int PRINT_DATA_KEY_EMPTY = 308;
    public final String PRINT_DATA_KEY_EMPTY_DES = "Print Data KEY Mismatch";

    public final int NULL_PRINT_RESULT_FROM_CLIENT = 309;
    public final String NULL_PRINT_RESULT_FROM_CLIENT_DES = "Null Print Result from the Client App";

    public final int PRINT_RESULT_KEY_EMPTY = 310;
    public final String PRINT_RESULT_KEY_EMPTY_DES = "Print Result KEY Mismatch";

    public final int PRINT_RESULT_CODE_MISMATCH = 311;
    public final String PRINT_RESULT_CODE_MISMATCH_DES = "Print Result Code Mismatch";

    /*public static final String USER_PROFILE = "USER_PROFILE";
    public static final String TRANS_RESPONSE = "TRANS_RESPONSE";
    public static final int CLIENT_APP_REQUEST_CODE = 2001;*/

    public final int PRINTER_NA = 312;
    public final String PRINTER_NA_DES = "Hardware not supported";

    public final int PRINTER_FAILURE = 313;
    public final String PRINTER_FAILURE_DES = "Printer hardware failure";
}
