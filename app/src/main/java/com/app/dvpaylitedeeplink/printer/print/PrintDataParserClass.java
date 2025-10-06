package com.app.dvpaylitedeeplink.printer.print;

import android.util.Log;

import com.denovo.app.invokekozen.printer.utils.PrinterUtility;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PrintDataParserClass {

    public JSONObject parsePrinterData(String printData) {
        JSONObject printObj = new JSONObject();
        List<LineItem> itemList = new ArrayList();
        try {
            printData = printData.substring(printData.indexOf("<"));
            printData = printData.replaceAll(">[?\\s]*<","><");
//            printData = printData.replaceAll(">[?\\s]*",">");
            Log.v("Printer","printData after string removal:::"+printData);
            printData = printData.replaceAll("<BR/><BR/><BR/>", "<BRR>")
                    .replaceAll("<BR/>", "")
                    .replaceAll("<BRR>", "<BR/>");

            printData = printData.replaceAll("<lf/><lf/><lf/>", "<lff>")
                    .replaceAll("<lf/>", "")
                    .replaceAll("<lff>", "<lf/>");

            printData = printData.replaceAll("<LF/><LF/><LF/>", "<LFF>")
                    .replaceAll("<LF/>", "")
                    .replaceAll("<LFF>", "<LF/>");

            printData = printData.replaceAll("<br/><br/><br/>", "<brr>")
                    .replaceAll("<br/>", "")
                    .replaceAll("<brr>", "<br/>");

            Log.v("Printer","printData:::"+printData);
            XmlPullParserFactory xmlFactoryObject;
            xmlFactoryObject = XmlPullParserFactory.newInstance();
            XmlPullParser parser = xmlFactoryObject.newPullParser();
            InputStream is = new ByteArrayInputStream(printData.getBytes());
            parser.setInput(is, null);

            int event = parser.getEventType();
            HashMap<String, String> attrs = null;
            boolean large = false;
            boolean condensed = false;
            boolean inverted = false;
            boolean bold = false;
            String valueText = "";

            while (event != XmlPullParser.END_DOCUMENT) {
                String name = parser.getName();
                switch (event) {
                    case XmlPullParser.START_TAG: {
                        if (name.equalsIgnoreCase("B")) {
                            bold = true;
                        } else if (name.equalsIgnoreCase("LG")) {
                            large = true;
                        } else if (name.equalsIgnoreCase("CD")) {
                            condensed = true;
                        } else if (name.equalsIgnoreCase("INV")) {
                            inverted = true;
                        } else {
                            attrs = new HashMap<>();
                            for (int i = 0; i < parser.getAttributeCount(); i++) {
                                attrs.put(parser.getAttributeName(i), parser.getAttributeValue(i));
                            }
                        }
                    }
                    break;
                    case XmlPullParser.TEXT:
                        valueText = parser.getText()
                                .replace("×�", "ש");
                        break;
                    case XmlPullParser.END_TAG: {
                        if (name.equalsIgnoreCase("B")) {
                            bold = false;
                        } else if (name.equalsIgnoreCase("LG")) {
                            large = false;
                        } else if (name.equalsIgnoreCase("CD")) {
                            condensed = false;
                        } else if (name.equalsIgnoreCase("INV")) {
                            inverted = false;
                        } else if (name.equalsIgnoreCase("BR") || name.equalsIgnoreCase("LF")) {
                            itemList.add(new LineItem("", "C", false, false, false, false, false));
                        } else if (name.equalsIgnoreCase("R")) {
                            if (PrinterUtility.checkStringValue(valueText)) {
                                itemList.add(new LineItem(valueText, "R", large, inverted, condensed, bold, false));
                            }
                            valueText = "";
                        } else if (name.equalsIgnoreCase("L")) {
                            if (!PrinterUtility.checkStringValue(valueText)) {
                                itemList.add(new LineItem("", "L", large, inverted, condensed, bold, false));
                            } else {
                                itemList.add(new LineItem(valueText, "L", large, inverted, condensed, bold, false));
                            }
                            valueText = "";
                        } else if (name.equalsIgnoreCase("C")) {
                            itemList.add(new LineItem(valueText, "C", large, inverted, condensed, bold, false));
                            valueText = "";
                        } else if (name.equalsIgnoreCase("SEP")) {
                            itemList.add(new LineItem("", "C", false, false, false, false, false));
                        } else if (name.equalsIgnoreCase("T")) {
                            if (PrinterUtility.checkStringValue(valueText)) {
                                itemList.add(new LineItem(valueText, "C", large, inverted, condensed, bold, false));
                            }
                            valueText = "";
                        } else if (name.equalsIgnoreCase("IMG")) {
                            if (attrs != null && attrs.containsKey("src")) {
                                String src = attrs.get("src");

                                if (PrinterUtility.checkStringValue(src)) {
                                    itemList.add(new LineItem(src,"C",false,false,false,false,true));
                                }
                            } else if (PrinterUtility.checkStringValue(valueText)) {
                                itemList.add(new LineItem(valueText,"C",false,false,false,false, true));
                            }
                        }
                        break;
                    }
                }
                event = parser.next();
            }

            printObj.put("body", getPrintArr(itemList));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return printObj;
    }

    private static class LineItem {
        private final String text;
        private final String alignment;
        private final boolean large;
        private final boolean inverted;
        private final boolean condensed;
        private final boolean bold;
        private final boolean isImage;

        LineItem(
                String text,
                String alignment,
                boolean large,
                boolean inverted,
                boolean condensed,
                boolean bold,
                boolean isImage
        ) {
            this.text = text;
            this.alignment = alignment;
            this.large = large;
            this.inverted = inverted;
            this.condensed = condensed;
            this.bold = bold;
            this.isImage = isImage;
        }

        public String getText() {
            return text;
        }

        String getAlignment() {
            return alignment;
        }

        boolean isLarge() {
            return large;
        }

        boolean isInverted() {
            return inverted;
        }

        boolean isCondensed() {
            return condensed;
        }

        public boolean isBold() {
            return bold;
        }
        public boolean isImageBitmap() {
            return isImage;
        }
    }

    private JSONArray getPrintArr(List<LineItem> itemList) {
        JSONArray printDataArr = new JSONArray();

        for (int i = 0; i < itemList.size(); i++) {
            LineItem line = itemList.get(i);
            Log.v("Printer","alignment::"+line.getAlignment());

            switch (line.getAlignment()) {
                case "C":
                case "R":
                    if (line.isImageBitmap()) {
                        getImageObj(line, printDataArr);
                    } else {
                        getTextAlignedObj(line, printDataArr);
                    }
                    break;
                case "L":
                    if (i + 1 < itemList.size() && itemList.get(i + 1).getAlignment().equalsIgnoreCase("R")) {
                        getLTextAlignedObj(line, printDataArr);
                    } else {
                        getTextAlignedObj(line, printDataArr);
                    }
                    break;
            }
        }

        return printDataArr;
    }

    private void getImageObj(LineItem item, JSONArray objArr) {
        JSONObject textObj = new JSONObject();
        try {
            String image = item.getText();
            JSONArray formatData = new JSONArray();
            formatData.put(0);
            formatData.put(0);
            formatData.put(0);
            formatData.put(0);
            formatData.put(0);
            formatData.put("");
            formatData.put("");
            formatData.put(0);
            formatData.put(0);
            textObj.put("image", image);
            textObj.put("format", formatData);
            objArr.put(textObj);
            formatData = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getTextAlignedObj(LineItem item, JSONArray objArr) {
        JSONObject textObj = new JSONObject();
        try {
            String text = item.getText();
            JSONArray formatData = new JSONArray();
            formatData.put(24);
            formatData.put(0);
            formatData.put(0);
            formatData.put(0);
            formatData.put(0);
            formatData.put(item.getAlignment());
            if (item.isBold()) {
                formatData.put("B");
            } else {
                formatData.put("N");
            }
            if (item.isInverted()) {
                formatData.put(1);
            } else {
                formatData.put(0);
            }

            formatData.put(1);

            if (item.getAlignment().equalsIgnoreCase("R")) {
                if (text.length() > 15) {
                    textObj.put("data", text.substring(0, 15));
                    textObj.put("format", formatData);
                    objArr.put(textObj);

                    textObj = new JSONObject();
                    textObj.put("data", text.substring(14));
                    textObj.put("format", formatData);
                    objArr.put(textObj);
                } else {
                    textObj.put("data", text);
                    textObj.put("format", formatData);
                    objArr.put(textObj);
                }
            } else {
                textObj.put("data", text);
                textObj.put("format", formatData);
                objArr.put(textObj);
            }
            formatData = null;
        } catch (Exception e) {
           e.printStackTrace();
        }
    }

    public void getLTextAlignedObj(LineItem item, JSONArray objArr) {
        JSONObject textObj = new JSONObject();
        try {
            String text = item.getText();
            JSONArray formatData = new JSONArray();
            formatData.put(24);
            formatData.put(0);
            formatData.put(0);
            formatData.put(0);
            formatData.put(0);
            formatData.put(item.getAlignment());
            if (item.isBold()) {
                formatData.put("B");
            } else {
                formatData.put("N");
            }
            if (item.isInverted()) {
                formatData.put(1);
            } else {
                formatData.put(0);
            }
            formatData.put(0);

            textObj.put("data", text);
            textObj.put("format", formatData);
            objArr.put(textObj);

            formatData = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
