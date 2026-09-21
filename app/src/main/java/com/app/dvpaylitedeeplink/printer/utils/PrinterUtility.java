package com.app.dvpaylitedeeplink.printer.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.net.URLDecoder;

public class PrinterUtility {

    public static boolean checkStringValue(String data) {
        return data != null && !data.trim().equalsIgnoreCase("") && !data.equalsIgnoreCase("null") && !data.isEmpty();
    }

    public static Bitmap decode(String image) {
        try {
            byte[] imageBytes = Base64.decode(image, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
