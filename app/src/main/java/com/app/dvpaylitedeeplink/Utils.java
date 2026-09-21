package com.app.dvpaylitedeeplink;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class Utils {
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
