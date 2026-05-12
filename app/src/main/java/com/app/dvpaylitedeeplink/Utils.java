package com.app.dvpaylitedeeplink;

import android.annotation.SuppressLint;
import android.os.Build;
import android.util.Log;

import com.pos.sdk.accessory.POIGeneralAPI;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class Utils {

    public static boolean isUnattendedDevice() {
        String deviceModel = getModel();
        String deviceSerialNumber = getSerialNumber();
        return deviceModel.equalsIgnoreCase(Constants.WIZAR_POS_MODEL_QD3) && getWizarPosSubModelName().equalsIgnoreCase(Constants.WIZAR_Q3V_SHORT)
                || (deviceModel.equalsIgnoreCase(Constants.WIZAR_POS_MODEL_QD3Mini) && getWizarPosSubModelName().equalsIgnoreCase(Constants.WIZAR_QD3Mini_SHORT) && isUnattendedQ3MiniV(deviceSerialNumber));
    }

    @SuppressLint("HardwareIds")
    public static String getSerialNumber() {
        return Build.SERIAL;
    }


    public static boolean isUnattendedQ3MiniV(String serialNumber) {
        if (serialNumber == null || serialNumber.length() < 9) {
            return false;
        }
        String variantCode = serialNumber.substring(5, 9);
        return Constants.UNATTENDED_DEVICE_IDENTIFIER.equalsIgnoreCase(variantCode);
    }


    public static String getWizarPosSubModelName() {
        String key = "ro.wp.product.submodel";
        Object strVersion = null;
        try {
            Class<?> systemProperties = Class.forName("android.os.SystemProperties");
            Log.i("systemProperties", systemProperties.toString());
            strVersion = systemProperties.getMethod("get", new Class[] { String.class, String.class }).invoke(systemProperties, new Object[] { key, "unknown" });
            Log.i("strVersion", strVersion.getClass().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strVersion.toString();
    }

    public static String getModel() {
        return Build.MODEL;
    }
    public static long generateRandom(int length) {
        Random random = new Random();
        char[] digits = new char[length];
        digits[0] = (char) (random.nextInt(9) + '1');
        for (int i = 1; i < length; i++) {
            digits[i] = (char) (random.nextInt(10) + '0');
        }
        return Long.parseLong(new String(digits));
    }

    public static String getCurrentDateYYMMDD() {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd", Locale.getDefault());
        return formatter.format(date);
    }
    public static int removeDouble(String input) {
        input = input.trim();

        if (input.contains(".")) {
            double value = Double.parseDouble(input);
            return (int) (value * 100);
        } else {
            return Integer.parseInt(input);
        }
    }

    // For double input
    public static int removeDouble(double input) {
        if (input % 1 == 0) {
            return (int) input;
        } else {
            return (int) (input * 100);
        }
    }
}
