package sampleurideeplinkapp;

import android.net.Uri;
import android.util.Log;
import android.webkit.JavascriptInterface;

public class WebAppInterface {

    SampleUriActivity mcontext;

    public WebAppInterface(SampleUriActivity context) {
        mcontext = context;
    }

    @JavascriptInterface
    public void receiveUrl(String url) {
        Log.d("WebAppInterface", "Received URL1: " + url);
        Uri uri = Uri.parse(url);
        mcontext.openApp(uri);
    }

    @JavascriptInterface
    public void validateType(String type, String tpn, String selectedType) {
        Log.d("WebAppInterface", "Received type: " + type);
        mcontext.tpn = tpn;
        mcontext.selectedType = selectedType;
        mcontext.validateType(type);
    }

    @JavascriptInterface
    public void selectedType(String hostType, String tpn, String type, String amount, String tip, String refId, String receiptType, String paymentType, String approvalType, String isvID, String merchantId) {
        Log.d("WebAppInterface", "Received selected type & item: " + hostType + type + amount + receiptType);
        mcontext.tpn = tpn;
        mcontext.amount = amount;
        mcontext.type = type;
        mcontext.tip = tip;
        mcontext.refId = refId;
        mcontext.receiptType = receiptType;
        mcontext.paymentType = paymentType;
        mcontext.approvalType = approvalType;
        mcontext.isvID = isvID;
        mcontext.merchantId = merchantId;
        mcontext.validateType(hostType);
    }

    @JavascriptInterface
    public void selectedType(String hostType, String tpn, String type, String amount, String tip, String refId, String receiptType,
                             String paymentType, String approvalType, String isvID, String merchantId, String primaryColor, String secondaryColor,
                             String negativeColor,String font, String AVS, String loader) {
        Log.d("WebAppInterface", "Received selected type & item: " + hostType + type + amount + receiptType);
        mcontext.tpn = tpn;
        mcontext.amount = amount;
        mcontext.type = type;
        mcontext.tip = tip;
        mcontext.refId = refId;
        mcontext.receiptType = receiptType;
        mcontext.paymentType = paymentType;
        mcontext.approvalType = approvalType;
        mcontext.isvID = isvID;
        mcontext.merchantId = merchantId;
        mcontext.primaryColor = primaryColor;
        mcontext.secondaryColor = secondaryColor;
        mcontext.negativeColor = negativeColor;
        mcontext.fontType = font;
        mcontext.avs = AVS;
        mcontext.loader = loader;
        mcontext.validateType(hostType);
    }

}
