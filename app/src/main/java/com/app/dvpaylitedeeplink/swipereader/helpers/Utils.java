package com.app.dvpaylitedeeplink.swipereader.helpers;

import android.util.Log;

import com.denovo.app.invokekozen.swipereader.helpers.Constants;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Utils {
    static String buildMode = com.denovo.app.invokekozen.swipereader.helpers.Constants.DEV;


    public static boolean isKozenPOSModel(String deviceModel) {
        if (checkStringValue(deviceModel)) {
            return deviceModel.equalsIgnoreCase(com.denovo.app.invokekozen.swipereader.helpers.Constants.KOZEN_POS_MODEL_P3) ||
                    deviceModel.equalsIgnoreCase(com.denovo.app.invokekozen.swipereader.helpers.Constants.KOZEN_POS_MODEL_P1) ||
                    deviceModel.equalsIgnoreCase(com.denovo.app.invokekozen.swipereader.helpers.Constants.KOZEN_POS_MODEL_P5) ||
                    deviceModel.equalsIgnoreCase(com.denovo.app.invokekozen.swipereader.helpers.Constants.KOZEN_POS_MODEL_L200) ||
                    deviceModel.equalsIgnoreCase(com.denovo.app.invokekozen.swipereader.helpers.Constants.KOZEN_POS_MODEL_P8) /*||
                    deviceModel.equalsIgnoreCase(com.denovo.app.invokekozen.swipereader.helpers.Constants.KOZEN_POS_MODEL_P18)*/
                    ;
        } else {
            return false;
        }
    }

    public static boolean isWizorPOSModel(String deviceModel) {
        return deviceModel.equalsIgnoreCase(com.denovo.app.invokekozen.swipereader.helpers.Constants.WIZAR_POS_MODEL_Q2) ||
                deviceModel.equalsIgnoreCase(com.denovo.app.invokekozen.swipereader.helpers.Constants.WIZAR_POS_MODEL_Q2_Q2) ||
                deviceModel.equalsIgnoreCase(com.denovo.app.invokekozen.swipereader.helpers.Constants.WIZAR_POS_MODEL_QD1) ||
                deviceModel.equalsIgnoreCase(com.denovo.app.invokekozen.swipereader.helpers.Constants.WIZAR_POS_MODEL_QD2) ||
                deviceModel.equalsIgnoreCase(com.denovo.app.invokekozen.swipereader.helpers.Constants.WIZAR_POS_MODEL_QD5) ||
                deviceModel.equalsIgnoreCase(com.denovo.app.invokekozen.swipereader.helpers.Constants.WIZAR_POS_MODEL_QD4) ||
                deviceModel.equalsIgnoreCase(com.denovo.app.invokekozen.swipereader.helpers.Constants.WIZAR_POS_MODEL_QD3) ||
                deviceModel.equalsIgnoreCase(com.denovo.app.invokekozen.swipereader.helpers.Constants.WIZAR_POS_MODEL_Q3) ||
                deviceModel.equalsIgnoreCase(com.denovo.app.invokekozen.swipereader.helpers.Constants.TYPICAL_ANDROID_SHORT) ||
                deviceModel.contains(com.denovo.app.invokekozen.swipereader.helpers.Constants.WIZAR_POS_MANUFACTURER);
    }

    public static boolean checkStringValue(String data) {
        return data != null && !data.trim().isEmpty() && !data.equalsIgnoreCase("null");
    }

    public static void logPrint(char tag, String message) {

        if (checkStringValue(buildMode) && (buildMode.equalsIgnoreCase(com.denovo.app.invokekozen.swipereader.helpers.Constants.DEV) || buildMode.equalsIgnoreCase(com.denovo.app.invokekozen.swipereader.helpers.Constants.DEBUG) || buildMode.equalsIgnoreCase(com.denovo.app.invokekozen.swipereader.helpers.Constants.UAT) || buildMode.equalsIgnoreCase(com.denovo.app.invokekozen.swipereader.helpers.Constants.PRE_PROD))) {
            switch (tag) {
                case 'I':
                    int maxLogSize = 1000;
                    for (int i = 0; i <= message.length() / maxLogSize; i++) {
                        int start = i * maxLogSize;
                        int end = (i + 1) * maxLogSize;
                        end = end > message.length() ? message.length() : end;
                        Log.i(com.denovo.app.invokekozen.swipereader.helpers.Constants.TAG, message.substring(start, end));
                    }
                    break;
                case 'E':
                    Log.e(com.denovo.app.invokekozen.swipereader.helpers.Constants.TAG, message);
                    break;
                case 'V':
                    Log.v(com.denovo.app.invokekozen.swipereader.helpers.Constants.TAG, message);
                    break;
                case 'D':
                    Log.d(com.denovo.app.invokekozen.swipereader.helpers.Constants.TAG, message);
                    break;
                case 'W':
                    Log.w(com.denovo.app.invokekozen.swipereader.helpers.Constants.TAG, message);
                    break;
                default:
                    break;
            }
        }
    }

    public static String stackTraceToString(Throwable e) {
        if (checkStringValue(buildMode) && (buildMode.equalsIgnoreCase(com.denovo.app.invokekozen.swipereader.helpers.Constants.DEV) || buildMode.equalsIgnoreCase(com.denovo.app.invokekozen.swipereader.helpers.Constants.DEBUG) ||
                buildMode.equalsIgnoreCase(com.denovo.app.invokekozen.swipereader.helpers.Constants.UAT) || buildMode.equalsIgnoreCase(Constants.PRE_PROD))){
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            return sw.toString();
        } else {
            return "";
        }
    }


}
