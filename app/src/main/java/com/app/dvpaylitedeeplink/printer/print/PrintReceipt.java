package com.app.dvpaylitedeeplink.printer.print;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.denovo.app.invokekozen.printer.interfaces.IPrinter;
import com.denovo.app.invokekozen.printer.utils.PrinterUtility;
import com.pos.sdk.printer.POIPrinterManager;
import com.pos.sdk.printer.models.BitmapPrintLine;
import com.pos.sdk.printer.models.PrintLine;
import com.pos.sdk.printer.models.TextPrintLine;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PrintReceipt {
    private final int PRINT_MAX_ROWS = 1000;

    public void kozenPrintData(String printData, Context mContext, IPrinter printerInterface){
        if(printData != null){
            String column_1 = "";
            String column_2 = "";
            String column_3 = "";
            int state;
            POIPrinterManager printerManager = new POIPrinterManager(mContext);
            try {
                printerManager.open();

                state = printerManager.getPrinterState();
                Log.v("Printer","printer state:::"+state);

                try {
                    printerManager.setPrintFont("/system/fonts/DroidSansMono.ttf");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                printerManager.setPrintGray(2000);
                JSONObject printObj = new JSONObject(printData);
                if (printObj.has("body")) {
                    int column = 1;
                    Log.d("Printer","print body");
                    JSONArray bodyArr = printObj.getJSONArray("body");
                    if (bodyArr != null && bodyArr.length() <= PRINT_MAX_ROWS) { //this is for temp purpose(for long printer issue complaint)
                        for (int i = 0; i < bodyArr.length(); i++) {
                            if (!bodyArr.getJSONObject(i).has("path") && !bodyArr.getJSONObject(i).has("image")) {
                                JSONArray format = bodyArr.getJSONObject(i).getJSONArray("format");
                                int fontSize = format.getInt(0);
                                String align = format.getString(5);
                                String style = format.getString(6);
                                int reverseMode = format.getInt(7);
                                int lineFeed = format.getInt(8);

                                if (lineFeed == 1 && align.equalsIgnoreCase("C")) {
                                    column_2 = bodyArr.getJSONObject(i).getString("data");
                                    if (column == 2) {
                                        TextPrintLine textPrintLine1 = setTextLine(PrintLine.CENTER,fontSize,style.equalsIgnoreCase("B"),column_2,reverseMode == 1);
                                        TextPrintLine textPrintLine2 = setTextLine(PrintLine.LEFT,fontSize,style.equalsIgnoreCase("B"),column_1,reverseMode == 1);

                                        List<TextPrintLine> list = new ArrayList<>();
                                        list.add(textPrintLine2);
                                        list.add(textPrintLine1);
                                        printerManager.addPrintLine(list);

                                        column_1 = "";
                                        column_2 = "";
                                        column_3 = "";
                                        list = null;
                                        textPrintLine1 = null;
                                        textPrintLine2 = null;
                                    } else {
                                        TextPrintLine textPrintLine = setTextLine(PrintLine.CENTER,fontSize,style.equalsIgnoreCase("B"),column_2,reverseMode == 1);
                                        printerManager.addPrintLine(textPrintLine);
                                        column = 1;
                                        column_1 = "";
                                        column_2 = "";
                                        column_3 = "";
                                        textPrintLine = null;
                                    }
                                } else if (lineFeed == 0 && align.equalsIgnoreCase("L")) {
                                    if (column == 2) {
                                        TextPrintLine textPrintLine = setTextLine(PrintLine.LEFT,fontSize,style.equalsIgnoreCase("B"),column_1,reverseMode == 1);
                                        printerManager.addPrintLine(textPrintLine);
                                        column = 1;
                                        column_1 = "";
                                        column_2 = "";
                                        column_3 = "";
                                        textPrintLine = null;
                                    }

                                    column = 2;
                                    column_1 = bodyArr.getJSONObject(i).getString("data");

                                } else if (lineFeed == 0 && align.equalsIgnoreCase("C")) {
                                    if (column == 2) {
                                        column = 3;
                                        column_2 = bodyArr.getJSONObject(i).getString("data");

                                    } else {
                                        column = 1;
                                        column_2 = bodyArr.getJSONObject(i).getString("data");

                                        TextPrintLine textPrintLine = setTextLine(PrintLine.CENTER,fontSize,style.equalsIgnoreCase("B"),column_2,reverseMode == 1);
                                        printerManager.addPrintLine(textPrintLine);
                                        column_2 = "";
                                        column_1 = "";
                                        column_3 = "";
                                        textPrintLine = null;

                                    }

                                } else if (lineFeed == 1 && align.equalsIgnoreCase("R")) {

                                    if (column == 3) {
                                        column = 1;
                                        column_3 = bodyArr.getJSONObject(i).getString("data");
                                        if (reverseMode == 1) {
                                            String column_2_spaced = addSpace(column_2);
                                            String invertText = column_1 + column_2_spaced + column_3;

                                            TextPrintLine textPrintLine = setTextLine(PrintLine.LEFT,fontSize,style.equalsIgnoreCase("B"),invertText,true);
                                            printerManager.addPrintLine(textPrintLine);
                                            textPrintLine = null;

                                        } else {
                                            TextPrintLine textPrintLine1 = setTextLine(PrintLine.CENTER,fontSize,style.equalsIgnoreCase("B"),column_2,false);
                                            TextPrintLine textPrintLine2 = setTextLine(PrintLine.LEFT,fontSize,style.equalsIgnoreCase("B"),column_1,false);
                                            TextPrintLine textPrintLine3 = setTextLine(PrintLine.RIGHT,fontSize,style.equalsIgnoreCase("B"),column_3,false);

                                            List<TextPrintLine> list = new ArrayList<>();
                                            list.add(textPrintLine2);
                                            list.add(textPrintLine1);
                                            list.add(textPrintLine3);
                                            printerManager.addPrintLine(list);

                                            textPrintLine1 = null;
                                            textPrintLine2 = null;
                                            textPrintLine3 = null;
                                            list = null;
                                        }

                                        column_1 = "";
                                        column_2 = "";
                                        column_3 = "";
                                    } else {
                                        column = 1;
                                        column_3 = bodyArr.getJSONObject(i).getString("data");

                                        if (reverseMode == 1) {
                                            String invertText = column_1 + addSpace(fontSize - (column_1.length() + column_3.length())) + column_3;


                                            TextPrintLine textPrintLine1 = setTextLine(PrintLine.LEFT,fontSize,style.equalsIgnoreCase("B"),invertText,true);
                                            printerManager.addPrintLine(textPrintLine1);

                                            textPrintLine1 = null;

                                        } else {
                                            TextPrintLine textPrintLine1 = setTextLine(PrintLine.LEFT,fontSize,style.equalsIgnoreCase("B"),column_1,false);
                                            TextPrintLine textPrintLine2 = setTextLine(PrintLine.RIGHT,fontSize,style.equalsIgnoreCase("B"),column_3,false);

                                            List<TextPrintLine> list = new ArrayList<>();
                                            list.add(textPrintLine1);
                                            list.add(textPrintLine2);
                                            printerManager.addPrintLine(list);

                                            column_3 = "";
                                            column_1 = "";
                                            column_2 = "";
                                            textPrintLine1 = null;
                                            textPrintLine2 = null;
                                            list = null;
                                        }

                                    }

                                } else if (lineFeed == 1 && align.equalsIgnoreCase("L")) {

                                    if (column == 2) {
                                        column = 1;
                                        column_2 = bodyArr.getJSONObject(i).getString("data");

                                        TextPrintLine textPrintLine1 = setTextLine(PrintLine.CENTER,fontSize,style.equalsIgnoreCase("B"),column_2,reverseMode == 1);
                                        TextPrintLine textPrintLine2 = setTextLine(PrintLine.LEFT,fontSize,style.equalsIgnoreCase("B"),column_1,reverseMode == 1);

                                        List<TextPrintLine> list = new ArrayList<>();
                                        list.add(textPrintLine1);
                                        list.add(textPrintLine2);
                                        printerManager.addPrintLine(list);

                                        column_1 = "";
                                        column_2 = "";
                                        column_3 = "";
                                        textPrintLine1 = null;
                                        textPrintLine2 = null;
                                        list = null;
                                    } else {

                                        column_2 = bodyArr.getJSONObject(i).getString("data");
                                        if (column_2.isEmpty()) {
                                            column = 1;
                                            column_1 = "";
                                            column_2 = "";
                                            column_3 = "";
                                        } else {

                                            TextPrintLine textPrintLine = setTextLine(PrintLine.LEFT,fontSize,style.equalsIgnoreCase("B"),column_2,reverseMode == 1);
                                            printerManager.addPrintLine(textPrintLine);

                                            column = 1;
                                            column_1 = "";
                                            column_2 = "";
                                            column_3 = "";
                                            textPrintLine = null;
                                        }
                                    }
                                } else if (lineFeed == 1 && align.equalsIgnoreCase("N")) {
                                    column = 1;
                                    column_2 = bodyArr.getJSONObject(i).getString("data");

                                    TextPrintLine textPrintLine = setTextLine(PrintLine.CENTER,fontSize,style.equalsIgnoreCase("B"),column_2,reverseMode == 1);
                                    printerManager.addPrintLine(textPrintLine);

                                    column_2 = "";
                                    column_1 = "";
                                    column_3 = "";
                                    textPrintLine = null;
                                } else if (lineFeed == 4 && align.equalsIgnoreCase("C")) {
                                    column = 1;
                                    column_2 = bodyArr.getJSONObject(i).getString("data");

                                    TextPrintLine textPrintLine = setTextLine(PrintLine.CENTER,fontSize,style.equalsIgnoreCase("B"),column_2,reverseMode == 1);
                                    printerManager.addPrintLine(textPrintLine);

                                    column_2 = "";
                                    column_1 = "";
                                    column_3 = "";
                                    textPrintLine = null;
                                } else if (lineFeed == 3 && align.equalsIgnoreCase("C")) {
                                    column = 1;
                                    column_2 = bodyArr.getJSONObject(i).getString("data");

                                    TextPrintLine textPrintLine = setTextLine(PrintLine.CENTER,fontSize,style.equalsIgnoreCase("B"),column_2,reverseMode == 1);
                                    printerManager.addPrintLine(textPrintLine);

                                    column = 1;
                                    column_2 = "";
                                    column_1 = "";
                                    column_3 = "";
                                    textPrintLine = null;

                                }
                                if (i == bodyArr.length() - 1) {

                                    TextPrintLine textPrintLine = setTextLine(PrintLine.LEFT,fontSize,false,"                               \n\n\n",false);
                                    printerManager.addPrintLine(textPrintLine);

                                    textPrintLine = null;

                                }
                            } else if (bodyArr.getJSONObject(i).has("image")) {
                                Bitmap img = PrinterUtility.decode(bodyArr.getJSONObject(i).getString("image"));
                                Log.d("Printer","img-----------"+img);
                                BitmapPrintLine bitmapPrintLine = new BitmapPrintLine();
                                bitmapPrintLine.setType(PrintLine.BITMAP);
                                bitmapPrintLine.setPosition(PrintLine.CENTER);
                                bitmapPrintLine.setBitmap(img);
                                printerManager.addPrintLine(bitmapPrintLine);
                                img = null;
                            }
                        }
                        POIPrinterManager.IPrinterListener listener = new POIPrinterManager.IPrinterListener() {
                            @Override
                            public void onStart() {
                                Log.d("Kozen","Printing started");
                            }

                            @Override
                            public void onFinish() {
                                if(printerManager != null){
                                    printerManager.close();
                                }
                                onPrintDone(printerInterface);
                            }

                            @Override
                            public void onError(int code, String msg) {
                                Log.e("POIPrinterManager", "code: " + code + "msg: " + msg);
                                if(printerManager != null){
                                    printerManager.close();
                                }
                                onPrintFailed(code,printerInterface);
                            }
                        };
                        if (state == 4) {
                            printerManager.close();
                            Log.e("POIPrinterManager", "code: 0  state : 4");
                            onPrintFailed(-6,printerInterface);
                        }

                        printerManager.beginPrint(listener);
                    } else {
                        printerManager.close();
                        Log.d("Printer","onPrintFailed--MAX-LIMIT-REACHED");
                        onPrintFailed(-5,printerInterface);
                    }
                } else {
                    printerManager.close();
                    Log.d("Printer","onPrintFailed--NO JSON BODY");
                    onPrintFailed(-3,printerInterface);
                }

            } catch (Exception e){
                e.printStackTrace();
                onPrintFailed(-2,printerInterface);
            }
        } else {
            Log.d("Printer","onPrintFailed--NO print data");
            onPrintFailed(-1,printerInterface);
        }
    }

    private void onPrintDone(IPrinter printerInterface) {
        if (printerInterface != null) {
            printerInterface.onPrintDone();
        }
    }

    private void onPrintFailed(int errorCode, IPrinter printerInterface) {
        if (printerInterface != null) {
            printerInterface.onPrintFailed(errorCode);
        }
    }

    private TextPrintLine setTextLine(int position, int fontSize, boolean isBold, String content, boolean isInvert) {
        TextPrintLine textPrintLine = new TextPrintLine();
        textPrintLine.setType(PrintLine.TEXT);
        textPrintLine.setPosition(position);
        textPrintLine.setSize(fontSize);
        textPrintLine.setBold(isBold);
        textPrintLine.setInvert(isInvert);
        textPrintLine.setContent(content);
        return textPrintLine;
    }

    private String addSpace(String content) {
        if (PrinterUtility.checkStringValue(content)) {
            int stringLength = content.length();
            StringBuilder stringBuilder = new StringBuilder();
            while (stringLength + stringBuilder.length() <= 18) {
                stringBuilder.append(" ");
            }
            content = content + stringBuilder;
            return content;
        } else {
            return "DECLINE         ";
        }
    }

    private String addSpace(int strLength) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < strLength; i++) {
            stringBuilder.append(" ");
        }
        return stringBuilder.toString();
    }
}
