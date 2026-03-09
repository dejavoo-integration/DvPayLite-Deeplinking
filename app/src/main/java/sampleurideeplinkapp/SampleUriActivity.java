package sampleurideeplinkapp;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.app.dvpaylitedeeplink.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
public class SampleUriActivity extends AppCompatActivity {

    private WebView webView;
    String amount;
    String tip;
    String type;
    String refId;
    String tpn;
    String receiptType;
    String paymentType;
    String selectedType;
    String approvalType;
    String isvID;
    String merchantId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);
        loadWeb();
    }

    public void loadWeb() {
        webView = findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webView.addJavascriptInterface(new WebAppInterface(this), "Android");
        webView.loadUrl("file:///android_asset/index.html");

    }

    private Uri getpaymentDeepLink(String selectedItem) {
        String scheme = "denovopay";
        String host = "pay";
        String encodedData = null;
        String uri = null;
        Log.d("Request", "selectedItem: " + selectedItem);
        switch (selectedItem) {
            case Constants.SALE:
            case Constants.REFUND:
            case Constants.ACTIVATE:
            case Constants.REDEEM:
            case Constants.RELOAD:
            case Constants.REISSUE:
            case Constants.ADDPOINTS:
                if (!amount.isEmpty()) {
                    encodedData = getSaleJsonData(selectedItem);
                } else {
                    updateInJS("Unable to make Transaction");
                    showToast("Unable to make Transaction");
                }
                break;
            case Constants.VOID:
                encodedData = getVoidJsonData(selectedItem);
                break;
            case Constants.PRE_AUTH:
                encodedData = getPreAuthJsonData();
                break;
            case Constants.TIP_ADJUST:
                encodedData = getTipAdjustJsonData();
                break;
            case Constants.INC_AUTH:
                encodedData = getIncAuthJsonData();
                break;
            case Constants.TICKET:
                encodedData = getTicketJsonData();
                break;
            case Constants.STATUS:
                if (!refId.isEmpty()) {
                    encodedData = getStatusCheckJsonData();
                } else {
                    updateInJS("Unable to Check txn Status");
                    showToast("Unable to Check txn Status");
                }
                break;
            case Constants.ADMINISTRATIVE_TXN:
                encodedData = getAdminTxnJsonData();
                break;
            case Constants.INQUIRE:
            case Constants.DEACTIVATE:
            case Constants.BALANCE:
                encodedData = getprocessInquiryOrDeactivateTxnJsonData();
            break;
        }
        if (encodedData != null) {
            uri = scheme + "://" + host + "?data=" + encodedData;
            Log.e("Request", "Request-uri: " + uri);
        }
        return Uri.parse(uri);
    }

    private String getAdminTxnJsonData() {
        try {
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("type", type);
            jsonRequest.put("refId", "DL" + Utils.generateRandom(12));
            jsonRequest.put("applicationType", "DVPAYLITE");
            jsonRequest.put("MerchantId", merchantId);
            jsonRequest.put("TPN", tpn);

            Log.e("Request", "Request: " + jsonRequest.toString());
            return URLEncoder.encode(jsonRequest.toString(), "UTF-8");
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getIncAuthJsonData() {
        try {
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("type", type);
            jsonRequest.put("amount", amount);
            jsonRequest.put("applicationType", "DVPAYLITE");
            jsonRequest.put("refId", refId);
            jsonRequest.put("receiptType", receiptType);
            jsonRequest.put("MerchantId", merchantId);
            jsonRequest.put("TPN", tpn);
            if (!approvalType.equals("No Tag")) {
                jsonRequest.put("isTxnStatusScreenRequired", approvalType);
            }
            jsonRequest.put("IsvId", isvID);

            Log.e("Request", "Request: " + jsonRequest.toString());
            return URLEncoder.encode(jsonRequest.toString(), "UTF-8");
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getStatusCheckJsonData() {
        try {
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("type", Constants.STATUS);
            jsonRequest.put("applicationType", "DVPAYLITE");
            jsonRequest.put("refId", refId);
            jsonRequest.put("MerchantId", merchantId);
            jsonRequest.put("TPN", tpn);
            if (!approvalType.equals("No Tag")) {
                jsonRequest.put("isTxnStatusScreenRequired", approvalType);
            }
            jsonRequest.put("IsvId", isvID);

            Log.e("Request", "Request: " + jsonRequest.toString());
            return URLEncoder.encode(jsonRequest.toString(), "UTF-8");
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getTicketJsonData() {
        try {
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("type", type);
            jsonRequest.put("amount", amount);
            jsonRequest.put("tip", tip);
            jsonRequest.put("applicationType", "DVPAYLITE");
            jsonRequest.put("refId", refId);
            jsonRequest.put("receiptType", receiptType);
            jsonRequest.put("MerchantId", merchantId);
            jsonRequest.put("TPN", tpn);
            if (!approvalType.equals("No Tag")) {
                jsonRequest.put("isTxnStatusScreenRequired", approvalType);
            }
            jsonRequest.put("IsvId", isvID);

            Log.e("Request", "Request: " + jsonRequest.toString());
            return URLEncoder.encode(jsonRequest.toString(), "UTF-8");
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getTipAdjustJsonData() {
        try {
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("type", type);
            jsonRequest.put("amount", amount);
            jsonRequest.put("tip", tip);
            jsonRequest.put("applicationType", "DVPAYLITE");
            jsonRequest.put("refId", refId);
            jsonRequest.put("MerchantId", merchantId);
            jsonRequest.put("TPN", tpn);
            if (!approvalType.equals("No Tag")) {
                jsonRequest.put("isTxnStatusScreenRequired", approvalType);
            }
            jsonRequest.put("IsvId", isvID);

            Log.e("Request", "Request: " + jsonRequest.toString());
            return URLEncoder.encode(jsonRequest.toString(), "UTF-8");
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getPreAuthJsonData() {
        try {
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("type", type);
            jsonRequest.put("amount", amount);
            jsonRequest.put("applicationType", "DVPAYLITE");
            jsonRequest.put("refId", "DL" + Utils.generateRandom(12));
            jsonRequest.put("receiptType", receiptType);
            jsonRequest.put("MerchantId", merchantId);
            jsonRequest.put("TPN", tpn);
            if (!approvalType.equals("No Tag")) {
                jsonRequest.put("isTxnStatusScreenRequired", approvalType);
            }
            jsonRequest.put("IsvId", isvID);

            Log.e("Request", "Request: " + jsonRequest.toString());
            return URLEncoder.encode(jsonRequest.toString(), "UTF-8");
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getVoidJsonData(String type) {
        try {
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("type", type);
            jsonRequest.put("applicationType", "DVPAYLITE");
            jsonRequest.put("refId", refId);
            jsonRequest.put("receiptType", receiptType);
            jsonRequest.put("MerchantId", merchantId);
            jsonRequest.put("TPN", tpn);
            if (!approvalType.equals("No Tag")) {
                jsonRequest.put("isTxnStatusScreenRequired", approvalType);
            }
            jsonRequest.put("IsvId", isvID);

            Log.e("Request", "Request: " + jsonRequest.toString());
            return URLEncoder.encode(jsonRequest.toString(), "UTF-8");
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getSaleJsonData(String type) {
        try {
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("type", type);
            jsonRequest.put("paymentType", paymentType);
            jsonRequest.put("amount", amount);
            jsonRequest.put("tip", tip);
            jsonRequest.put("applicationType", "DVPAYLITE");
            jsonRequest.put("refId", "DL" + Utils.generateRandom(12));
            jsonRequest.put("receiptType", receiptType);
            jsonRequest.put("MerchantId", merchantId);
            jsonRequest.put("TPN", tpn);
            if (!approvalType.equals("No Tag")) {
                jsonRequest.put("isTxnStatusScreenRequired", approvalType);
            }
            jsonRequest.put("IsvId", isvID);

            Log.e("Request", "Request: " + jsonRequest.toString());
            return URLEncoder.encode(jsonRequest.toString(), "UTF-8");
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void openApp(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        List<ResolveInfo> activities = getPackageManager().queryIntentActivities(intent, 0);
        boolean isIntentSafe = activities.size() > 0;

        if (isIntentSafe) {
            startActivityForResult(intent, 1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("onTransaction::requestCode", String.valueOf(requestCode));
        Log.d("onTransaction::resultCode", String.valueOf(resultCode));
        if (resultCode == Constants.URI_ACTIVITY_CODE) {
            if (data != null) {
                String resultData = data.getStringExtra("transactionResult");
                Log.d("onTransaction::resultData", String.valueOf(resultData));

                if (resultData != null) {
                    String responseData = data.getSerializableExtra("transactionResultJson").toString();

                    try {
                        switch (resultData) {
                            case Constants.SUCCESS:
                                Log.d("onTransaction::SuccessResponse", String.valueOf(new JSONObject(responseData)));
                                updateInJS(responseData);
                                break;
                            case Constants.FAILURE:
                                Log.d("onTransaction::FailureResponse", String.valueOf(new JSONObject(responseData)));
                                updateInJS(responseData);
                                break;
                        }
                        showToast(responseData);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
        }

    }

    private void updateInJS(String responseData) {
        webView.post(() -> webView.evaluateJavascript("javascript:receiveResponse('" + responseData + "');", null));
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void validateType(String hostType) {
        Log.d("WebAppInterface", "Print received values :" + tpn + amount + type + refId + tip + receiptType + paymentType);
        Uri finalUri = null;
        switch (hostType) {
            case Constants.HOST_REGISTER:
                if (tpn != null && !tpn.isEmpty()) {
                    finalUri = getRegisterApp();
                } else {
                    updateInJS("Unable to Register");
                    showToast("Unable to Register ");
                }
                break;
            case Constants.HOST_GET:
                finalUri = getTPNOrDeviceData(selectedType);
                /*finalUri = getTPN();
                break;
            case Constants.HOST_GET_DEVICE:
                finalUri = getDeviceRequest();*/
                break;
            case Constants.HOST_PAY:
                finalUri = getpaymentDeepLink(type);
                break;
            case Constants.HOST_SETTLE:
                finalUri = getSettleJsonData(type);
                break;

        }
        if (finalUri != null) {
            openApp(finalUri);
            Log.d("Uri", "Print URI :" + finalUri);
        }
    }

    private Uri getTPNOrDeviceData(String selectedType) {
        String scheme = "denovopay";
        String host = Constants.HOST_GET;
        String applicationType = "DVPAYLITE";
        String hostGetTPN = "";
        try {
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("applicationType", applicationType);
            if (selectedType.equals(Constants.GET_TPN)){
                jsonRequest.put("type", Constants.GET_TPN);
            } else if (selectedType.equals(Constants.GET_DEVICE)) {
                jsonRequest.put("type", Constants.GET_DEVICE);
            }
            hostGetTPN = URLEncoder.encode(jsonRequest.toString(), "UTF-8");
            Log.e("getTpn", "getTpn Request: " + jsonRequest.toString());
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return Uri.parse(scheme + "://" + host + "?data=" + hostGetTPN);
    }

    private Uri getDeviceRequest() {
        String scheme = "denovopay";
        String host = Constants.HOST_GET;
        String applicationType = "DVPAYLITE";
        boolean isDeviceModel = true;
        String hostGetTPN = "";
        try {
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("applicationType", applicationType);
            hostGetTPN = URLEncoder.encode(jsonRequest.toString(), "UTF-8");
            Log.e("getTpn", "getTpn Request: " + jsonRequest.toString());
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return Uri.parse(scheme + "://" + host + "?data=" + hostGetTPN + "&deviceModel=" + isDeviceModel);
    }

    private Uri getTPN() {
        String scheme = "denovopay";
        String host = Constants.HOST_GET;
        String applicationType = "DVPAYLITE";
        String hostGetTPN = "";
        try {
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("applicationType", applicationType);
            hostGetTPN = URLEncoder.encode(jsonRequest.toString(), "UTF-8");
            Log.e("getTpn", "getTpn Request: " + jsonRequest.toString());
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return Uri.parse(scheme + "://" + host + "?data=" + hostGetTPN);
    }

    private Uri getRegisterApp() {
        String scheme = "denovopay";
        String host = Constants.HOST_REGISTER;
        String applicationType = "DVPAYLITE";
        String host_Register = "";
        try {
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("tpn", tpn);
            jsonRequest.put("applicationType", applicationType);
            host_Register = URLEncoder.encode(jsonRequest.toString(), "UTF-8");
            Log.e("getRegisterApp", "getRegisterApp Request: " + jsonRequest.toString());
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return Uri.parse(scheme + "://" + host + "?data=" + host_Register);
    }

    private Uri getSettleJsonData(String type) {
        String scheme = "denovopay";
        String host = Constants.HOST_SETTLE;
        String host_settle = "";
        try {
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("type", type);
            jsonRequest.put("applicationType", "DVPAYLITE");

            Log.e("Request", "Request: " + jsonRequest.toString());
            host_settle = URLEncoder.encode(jsonRequest.toString(), "UTF-8");
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return Uri.parse(scheme + "://" + host + "?data=" + host_settle);
    }

    private String getprocessInquiryOrDeactivateTxnJsonData() {
        try {
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("type", type);
            jsonRequest.put("paymentType", paymentType);
            jsonRequest.put("applicationType", "DVPAYLITE");
            jsonRequest.put("refId", "DL" + Utils.generateRandom(12));
            jsonRequest.put("receiptType", receiptType);
            jsonRequest.put("MerchantId", merchantId);
            jsonRequest.put("TPN", tpn);
            if (!approvalType.equals("No Tag")) {
                jsonRequest.put("isTxnStatusScreenRequired", approvalType);
            }
            jsonRequest.put("IsvId", isvID);

            Log.e("Request", "Request: " + jsonRequest.toString());
            return URLEncoder.encode(jsonRequest.toString(), "UTF-8");
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
