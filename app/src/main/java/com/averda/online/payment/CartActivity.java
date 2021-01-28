package com.averda.online.payment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.payumoney.core.PayUmoneyConstants;
import com.payumoney.core.PayUmoneySdkInitializer;
import com.payumoney.core.entity.TransactionResponse;
import com.payumoney.sdkui.ui.utils.PayUmoneyFlowManager;
import com.payumoney.sdkui.ui.utils.ResultModel;
import com.averda.online.BaseApplication;
import com.averda.online.R;
import com.averda.online.common.ZTAppCompatActivity;
import com.averda.online.mypackage.onlineTestSeries.TestUtils;
import com.averda.online.offers.OfferDialogActivity;
import com.averda.online.preferences.Preferences;
import com.averda.online.utils.CompleteListener;
import com.averda.online.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class CartActivity extends ZTAppCompatActivity implements CartAdapter.ClickListener, View.OnClickListener {
    public static final int PAYMENT_START = 1;
    public static final int PAYMENT_PAYMENT_GATEWAY = 2;
    public static final int PAYMENT_RETURN_BACK = 3;
    public static final int PAYMENT_DECLINE = 4;
    public static final int PAYMENT_FAILED = 5;
    public static final int PAYMENT_SUCCESS = 6;

    public static final double GST = 18;
    public static final String TAG = "CartActivity";
    public static final String EXTRA_PACKAGE_ID = "packageId";
    public static final String EXTRA_SUBJECT_ID = "subjectId";
    public static final String EXTRA_SCREEN_TYPE = "type";
    private RecyclerView cartList;
    private TextView totalAmountView;
    private TextView discountView;
    private TextView amountView;
    private TextView gstView;
    private TextView payAmountView;
    private double amount;
    private double totalamount;
    private long payAmount;
    private double discount;
    private JSONArray items;
    private String currency;
    private CartAdapter cartAdapter;
    private int packageId;
    private int subjectId;
    private String packageName = "";
    private PayUmoneySdkInitializer.PaymentParam mPaymentParams;
    private String transId;
    private String promoCodeValue = "";
    private double originalAmount;
    private double originalTotalamount;
    private long originalPayAmount;
    private long gstAmount;
    private String type = "other";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        cartList = findViewById(R.id.cartList);
        totalAmountView = findViewById(R.id.totalAmount);
        amountView = findViewById(R.id.amount);
        gstView = findViewById(R.id.gst);
        payAmountView = findViewById(R.id.payAmount);
        discountView = findViewById(R.id.discount);
        currency = getString(R.string.currency);
        findViewById(R.id.payButton).setOnClickListener(this);
        findViewById(R.id.progressBar).setOnClickListener(this);
        findViewById(R.id.removeCode).setOnClickListener(this);
        findViewById(R.id.applyPromoCode).setOnClickListener(this);
        findViewById(R.id.offer).setOnClickListener(this);
        ((EditText)findViewById(R.id.promoCode)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    applyPromoCode();
                    return true; // consume.
                }
                return false; // pass on to other listeners.
            }
        });
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            packageId = bundle.getInt(EXTRA_PACKAGE_ID);
            subjectId = bundle.getInt(EXTRA_SUBJECT_ID);
            type = bundle.getString(EXTRA_SCREEN_TYPE, type);
            addToCart();
        }else{
            getCart();
        }
    }

    private void addToCart(){
        PaymentUtils.addToCart(this, packageId, subjectId, new CompleteListener() {
            @Override
            public void success(JSONObject response) {
                if(Utils.isActivityDestroyed(CartActivity.this)){
                    return;
                }
                getCart();
            }

            @Override
            public void error(String error) {
                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getCart(){
        if(findViewById(R.id.appliedLayout).getVisibility() == View.VISIBLE){
            showPromoCode();
        }
        PaymentUtils.getCart(this, new CompleteListener() {
            @Override
            public void success(JSONObject response) {
                if(Utils.isActivityDestroyed(CartActivity.this)){
                    return;
                }
                JSONObject data = response.optJSONObject("Body");
                items = data.optJSONArray("Items");
                if(items != null && items.length() > 0) {
                    totalamount = data.optDouble("TotalAmont");
                    for (int i = 0 ; i < items.length() ; i++){
                        packageName = packageName + items.optJSONObject(i).optInt("PackageID");
                    }
                    Preferences.put(getApplicationContext(), Preferences.KEY_CART_COUNT, items.length());
                    setPriceView();
                    originalAmount = amount;
                    originalPayAmount = payAmount;
                    originalTotalamount = totalamount;
                    setCartList();
                    getPromoCoupon();
                }else{
                    findViewById(R.id.noItemLayout).setVisibility(View.VISIBLE);
                    Preferences.put(getApplicationContext(), Preferences.KEY_CART_COUNT, 0);
                }
            }

            @Override
            public void error(String error) {
                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                findViewById(R.id.progressBar).setVisibility(View.GONE);
            }
        });
    }

    private void setCartList(){
        findViewById(R.id.progressBar).setVisibility(View.GONE);
        if(cartAdapter != null){
            cartAdapter.refreshValues(items);
        }else{
            cartAdapter = new CartAdapter(this, items, this);
            cartList.setLayoutManager(new LinearLayoutManager(this));
            cartList.setAdapter(cartAdapter);
        }
        findViewById(R.id.footer).setVisibility(View.VISIBLE);
        findViewById(R.id.footerShadow).setVisibility(View.VISIBLE);
    }

    public void removeCart(int cartId){
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        PaymentUtils.removeCart(this, cartId, new CompleteListener() {
            @Override
            public void success(JSONObject response) {
                if(Utils.isActivityDestroyed(CartActivity.this)){
                    return;
                }
                findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                getCart();
            }

            @Override
            public void error(String error) {
                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                findViewById(R.id.progressBar).setVisibility(View.GONE);
            }
        });
    }

    private void setPriceView(){
        totalAmountView.setText(String.format(getString(R.string.total_amount), currency+Math.round(totalamount)));
        double perDiscount = discount/totalamount;
        discountView.setText(String.format(getString(R.string.discount), currency+Math.round(discount), Math.round(perDiscount*100)+"%"));
        amount = totalamount - discount;
        amountView.setText(String.format(getString(R.string.amount), currency+Math.round(amount)));
        double gst = (amount * GST)/100;
        gstAmount = Math.round(gst);
        gstView.setText(String.format(getString(R.string.gst), GST+"%", currency+gstAmount));
        payAmount = Math.round(amount) + gstAmount;
        payAmountView.setText(String.format(getString(R.string.pay_amount), currency+payAmount));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.offer:
                offer();
                break;
            case R.id.applyPromoCode:
                applyPromoCode();
                break;
            case R.id.removeCode:
                showPromoCode();
                break;
            case R.id.progressBar:
                return;
            case R.id.payButton:
                payButtonClicked();
                break;
        }
    }
    private void payButtonClicked(){
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        PaymentUtils.buyPackage(this, Math.round(payAmount), Math.round(discount), promoCodeValue, gstAmount, new CompleteListener() {
            @Override
            public void success(JSONObject response) {
                if(Utils.isActivityDestroyed(CartActivity.this)){
                    return;
                }
                response = response.optJSONObject("Body");
                transId = response.optString("Txnid");
                if(payAmount == 0){
                    sendPaymentStatus(PAYMENT_SUCCESS);
                }else {
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    launchPayUMoneyFlow();
                }
            }

            @Override
            public void error(String error) {
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void launchPayUMoneyFlow() {
        sendPaymentStatus(PAYMENT_START);
        PayUmoneySdkInitializer.PaymentParam.Builder builder = new PayUmoneySdkInitializer.PaymentParam.Builder();
        String phone = Utils.getPhone(this);
        String productName = packageName;
        String firstName = Utils.getName(this);
        String email = Utils.getEmail(this);
        String udf1 = "";
        String udf2 = "";
        String udf3 = "";
        String udf4 = "";
        String udf5 = "";
        String udf6 = "";
        String udf7 = "";
        String udf8 = "";
        String udf9 = "";
        String udf10 = "";

        AppEnvironment appEnvironment = ((BaseApplication) getApplication()).getAppEnvironment();
        builder.setAmount(payAmount+"")
                .setTxnId(transId)
                .setPhone(phone)
                .setProductName(productName)
                .setFirstName(firstName)
                .setEmail(email)
                .setsUrl(appEnvironment.surl())
                .setfUrl(appEnvironment.furl())
                .setUdf1(udf1)
                .setUdf2(udf2)
                .setUdf3(udf3)
                .setUdf4(udf4)
                .setUdf5(udf5)
                .setUdf6(udf6)
                .setUdf7(udf7)
                .setUdf8(udf8)
                .setUdf9(udf9)
                .setUdf10(udf10)
                .setIsDebug(appEnvironment.debug())
                .setKey(appEnvironment.merchant_Key())
                .setMerchantId(appEnvironment.merchant_ID());

        try {
            mPaymentParams = builder.build();
            mPaymentParams = calculateServerSideHashAndInitiatePayment1(mPaymentParams);
            PayUmoneyFlowManager.startPayUMoneyFlow(mPaymentParams, this, R.style.AppTheme_default, false);
            sendPaymentStatus(PAYMENT_PAYMENT_GATEWAY);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
    }
    private PayUmoneySdkInitializer.PaymentParam calculateServerSideHashAndInitiatePayment1(final PayUmoneySdkInitializer.PaymentParam paymentParam) {

        StringBuilder stringBuilder = new StringBuilder();
        HashMap<String, String> params = paymentParam.getParams();
        stringBuilder.append(params.get(PayUmoneyConstants.KEY) + "|");
        stringBuilder.append(params.get(PayUmoneyConstants.TXNID) + "|");
        stringBuilder.append(params.get(PayUmoneyConstants.AMOUNT) + "|");
        stringBuilder.append(params.get(PayUmoneyConstants.PRODUCT_INFO) + "|");
        stringBuilder.append(params.get(PayUmoneyConstants.FIRSTNAME) + "|");
        stringBuilder.append(params.get(PayUmoneyConstants.EMAIL) + "|");
        stringBuilder.append(params.get(PayUmoneyConstants.UDF1) + "|");
        stringBuilder.append(params.get(PayUmoneyConstants.UDF2) + "|");
        stringBuilder.append(params.get(PayUmoneyConstants.UDF3) + "|");
        stringBuilder.append(params.get(PayUmoneyConstants.UDF4) + "|");
        stringBuilder.append(params.get(PayUmoneyConstants.UDF5) + "||||||");

        AppEnvironment appEnvironment = ((BaseApplication) getApplication()).getAppEnvironment();
        stringBuilder.append(appEnvironment.salt());

        String hash = hashCal(stringBuilder.toString());
        paymentParam.setMerchantHash(hash);

        return paymentParam;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult called requestCode = "+requestCode+", resultCode = "+resultCode);
        if (requestCode == PayUmoneyFlowManager.REQUEST_CODE_PAYMENT) {
            sendPaymentStatus(PAYMENT_RETURN_BACK);
            if(resultCode == RESULT_OK && data != null){
                TransactionResponse transactionResponse = data.getParcelableExtra(PayUmoneyFlowManager
                        .INTENT_EXTRA_TRANSACTION_RESPONSE);
                ResultModel resultModel = data.getParcelableExtra(PayUmoneyFlowManager.ARG_RESULT);
                Log.i(TAG, "onActivityResult called transactionResponse = "+transactionResponse);
                if (transactionResponse != null && transactionResponse.getPayuResponse() != null) {
                    updatePaymentStatus(transactionResponse);
                } else if (resultModel != null && resultModel.getError() != null) {
                    Log.i(TAG, "error = "+resultModel.getError().getMessage());
                } else {
                    Log.i(TAG, "error");
                }
            }
        }else if(requestCode == 100 && resultCode == RESULT_OK){
            if(data != null){
                String promoCode = data.getStringExtra("promoCode");
                if(Utils.isValidString(promoCode)){
                    ((EditText)findViewById(R.id.promoCode)).setText(promoCode);
                    applyPromoCode();
                }
            }
        }
    }
    public static String hashCal(String str) {
        byte[] hashseq = str.getBytes();
        StringBuilder hexString = new StringBuilder();
        try {
            MessageDigest algorithm = MessageDigest.getInstance("SHA-512");
            algorithm.reset();
            algorithm.update(hashseq);
            byte messageDigest[] = algorithm.digest();
            for (byte aMessageDigest : messageDigest) {
                String hex = Integer.toHexString(0xFF & aMessageDigest);
                if (hex.length() == 1) {
                    hexString.append("0");
                }
                hexString.append(hex);
            }
        } catch (NoSuchAlgorithmException ignored) {
        }
        return hexString.toString();
    }

    private void updatePaymentStatus(TransactionResponse transactionResponse){
        PaymentStatus paymentStatus = new PaymentStatus();
        String message = "";
        if (transactionResponse.getTransactionStatus().equals(TransactionResponse.TransactionStatus.SUCCESSFUL)) {
            String payuResponse = transactionResponse.getPayuResponse();
            try {
                JSONObject resObj = new JSONObject(payuResponse);
                resObj = resObj.optJSONObject("result");
                paymentStatus.status = PAYMENT_SUCCESS;
                paymentStatus.paymentID = resObj.optString("paymentId");
                paymentStatus.paymentMode = resObj.optString("mode");
                paymentStatus.bankRefNo = resObj.optString("bank_ref_num");
                paymentStatus.pGtype = resObj.optString("pg_TYPE");
                message = resObj.optString("field9");
            }catch (Exception e){
                if(Utils.isDebugModeOn){
                    e.printStackTrace();
                }
            }
        }else if(transactionResponse.getTransactionStatus().equals(TransactionResponse.TransactionStatus.PG_REJECTED)){
            paymentStatus.status = PAYMENT_DECLINE;
        }else{
            paymentStatus.status = PAYMENT_FAILED;
        }
        paymentStatus.discount = Math.round(discount);
        paymentStatus.txnid = transId;
        paymentStatus.payStatus = transactionResponse.getTransactionStatus().toString();
        final String finalMessage = message;
        PaymentUtils.UpdateTransactionStatus(this, paymentStatus, new CompleteListener() {
            @Override
            public void success(JSONObject response) {
                if(Utils.isValidString(finalMessage)){
                    Toast.makeText(getApplicationContext(), finalMessage, Toast.LENGTH_SHORT).show();
                    PaymentUtils.removeCart(CartActivity.this, -1, new CompleteListener() {
                        @Override
                        public void success(JSONObject response) {
                            Preferences.put(getApplicationContext(), Preferences.KEY_CART_COUNT, 0);
                            if("class".equalsIgnoreCase(type)){
                                Utils.openMyPackagesNewTask(CartActivity.this, 0);
                            }else if("test".equalsIgnoreCase(type)){
                                Utils.openMyPackagesNewTask(CartActivity.this, 1);
                            }else{
                                setResult(RESULT_OK);
                                finish();
                            }
                        }

                        @Override
                        public void error(String error) {

                        }
                    });
                }
            }

            @Override
            public void error(String error) {
                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendPaymentStatus(final int status){
        PaymentStatus paymentStatus = new PaymentStatus();
        paymentStatus.discount = Math.round(discount);
        paymentStatus.txnid = transId;
        paymentStatus.status = status;
        PaymentUtils.UpdateTransactionStatus(this, paymentStatus, new CompleteListener() {
            @Override
            public void success(JSONObject response) {
                if(status == PAYMENT_SUCCESS) {
                    Toast.makeText(getApplicationContext(), "Transaction completed succesfully", Toast.LENGTH_SHORT).show();
                    PaymentUtils.removeCart(CartActivity.this, -1, new CompleteListener() {
                        @Override
                        public void success(JSONObject response) {
                            Preferences.put(getApplicationContext(), Preferences.KEY_CART_COUNT, 0);
                            if("class".equalsIgnoreCase(type)){
                                Utils.openMyPackagesNewTask(CartActivity.this, 0);
                            }else if("test".equalsIgnoreCase(type)){
                                Utils.openMyPackagesNewTask(CartActivity.this, 1);
                            }else{
                                setResult(RESULT_OK);
                                finish();
                            }
                        }

                        @Override
                        public void error(String error) {

                        }
                    });
                }
            }
            @Override
            public void error(String error) {
                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyPromoCode(){
        promoCodeValue = ((EditText)findViewById(R.id.promoCode)).getText().toString().trim();
        if(Utils.isValidString(promoCodeValue)){
            hideKeypad();
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            PaymentUtils.applypromo(this, promoCodeValue, totalamount, new CompleteListener() {
                @Override
                public void success(JSONObject response) {
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    response = response.optJSONObject("Body");
                    discount = response.optDouble("discount_amount");
                    originalAmount = amount;
                    originalPayAmount = payAmount;
                    originalTotalamount = totalamount;
                    setPriceView();
                    appliedPromoCode();
                }

                @Override
                public void error(String error) {
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            Toast.makeText(getApplicationContext(), "Invalid Promo Code", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPromoCode(){
        amount = originalAmount;
        payAmount = originalPayAmount;
        discount = 0;
        totalamount = originalTotalamount;
        setPriceView();
        findViewById(R.id.promoCodeLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.appliedLayout).setVisibility(View.GONE);
        promoCodeValue = "";
    }

    private void appliedPromoCode(){
        findViewById(R.id.promoCodeLayout).setVisibility(View.GONE);
        findViewById(R.id.appliedLayout).setVisibility(View.VISIBLE);
        ((TextView)findViewById(R.id.AppliedpromoCode)).setText(promoCodeValue);
    }
    private void hideKeypad(){
        try {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(findViewById(R.id.promoCode).getWindowToken(), 0);
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            hideKeyboard();
        }
        return super.dispatchTouchEvent(ev);
    }

    private void hideKeyboard() {
        try {
            View view = getCurrentFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch(Exception e) {}
    }

    private void getPromoCoupon(){
        TestUtils.getPromotions(this, new CompleteListener() {
            @Override
            public void success(JSONObject response) {
                String message = response.optString("Message");
                if(Utils.isValidString(message)) {
                    ((TextView) findViewById(R.id.promoCoupon)).setText(message);
                    ((TextView) findViewById(R.id.promoCoupon)).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void error(String error) {

            }
        });
    }

    private void offer(){
        Intent intent = new Intent(this, OfferDialogActivity.class);
        startActivityForResult(intent, 100);
    }
}
