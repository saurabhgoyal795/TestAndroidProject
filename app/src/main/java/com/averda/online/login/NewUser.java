package com.averda.online.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.averda.online.common.ZTAppCompatActivity;
import com.averda.online.home.MainActivity;
import com.averda.online.R;
import com.averda.online.preferences.Preferences;
import com.averda.online.server.ServerApi;
import com.averda.online.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class NewUser extends ZTAppCompatActivity implements View.OnClickListener {
    private EditText email;
    private EditText password;
    private EditText name;
    private EditText lastName;
    private EditText phone;
    private EditText otp;
    private boolean isSignUpScreen;
    private DisplayMetrics metrics;
    private ImageView passwordVisibleIcon;
    private String deviceType;
    private int loginStudentId;
    private String loginDeviceType;
    private EditText city;
    private EditText country;
    private EditText organization;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        metrics = Utils.getMetrics(this);
        setContentView(R.layout.activity_login);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        name = findViewById(R.id.name);
        lastName = findViewById(R.id.lastName);
        phone = findViewById(R.id.phone);
        city = findViewById(R.id.city);
        country = findViewById(R.id.country);
        organization = findViewById(R.id.organization);
        passwordVisibleIcon = findViewById(R.id.passwordVisibleIcon);
        otp = findViewById(R.id.otp);
        findViewById(R.id.newUser).setOnClickListener(this);
        findViewById(R.id.loginButton).setOnClickListener(this);
        findViewById(R.id.progressBar).setOnClickListener(this);
        findViewById(R.id.contentView).setOnClickListener(this);
        passwordVisibleIcon.setOnClickListener(this);
        deviceType = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        setNewUserLayout();
    }

    @Override
    public void onClick(View v) {
        hideKeyboard();
        switch (v.getId()) {
            case R.id.loginButton:
                loginButtonClicked();
                break;
        }
    }

    private void setNewUserLayout() {
        isSignUpScreen = true;
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)findViewById(R.id.loginView).getLayoutParams();
        params.topMargin = (int)(16 * metrics.density);
        findViewById(R.id.loginView).setLayoutParams(params);
        findViewById(R.id.nameLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.nameLayout2).setVisibility(View.VISIBLE);
        findViewById(R.id.phoneLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.emailLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.passwordLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.cityLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.countryLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.orgainizationLayout).setVisibility(View.VISIBLE);

        findViewById(R.id.newUser).setVisibility(View.GONE);
        ((TextView) findViewById(R.id.loginButton)).setText(R.string.singup);
        ((TextView) findViewById(R.id.loginMode)).setText(R.string.singup);
    }




    private void loginButtonClicked() {
        if (checkValidity()) {
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            signUp();
        }
    }

    private void signUp() {
        JSONObject params = new JSONObject();
        try{
            params.put("first_name", name.getText().toString().trim());
            params.put("last_name", lastName.getText().toString().trim());
            params.put("phone", phone.getText().toString().trim());
            params.put("email", email.getText().toString().trim());
            params.put("EmailID", email.getText().toString().trim());
            params.put("password", password.getText().toString().trim());
            params.put("city", city.getText().toString().trim());
            params.put("country", country.getText().toString().trim());
            params.put("organization", organization.getText().toString().trim());
            params.put("device_info", Utils.getPhoneDetail());
            params.put("device_id", deviceType);
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        ServerApi.callServerApi(this, ServerApi.BASE_URL, "createuser", params, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {
                if(Utils.isActivityDestroyed(NewUser.this)){
                    return;
                }
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                String statusCode = response.optString("success");
                boolean status = response.optBoolean("success");
                if("true"  == statusCode || status) {
                    Intent intent = new Intent(NewUser.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                  //  setLoginLayout();
                }
                String message = response.optString("message");
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }

            @Override
            public void error(String error) {
                if(Utils.isActivityDestroyed(NewUser.this)){
                    return;
                }
                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
                findViewById(R.id.progressBar).setVisibility(View.GONE);
            }
        });
    }

    private void signIn() {
        JSONObject params = new JSONObject();
        try{
            params.put("email", email.getText().toString().trim());
            params.put("password", password.getText().toString().trim());
            params.put("device_id", deviceType);
            params.put("device_info", Utils.getPhoneDetail());
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        ServerApi.callServerApi(this, ServerApi.BASE_URL, "login", params, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {
                if(Utils.isActivityDestroyed(NewUser.this)){
                    return;
                }
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                String statusCode = response.optString("success");
                if("true"  == statusCode) {
                    JSONObject data = response.optJSONObject("data");
                    if (data != null) {
                        Utils.setUserProperties(NewUser.this, data);
                    }
                    int flag = data.optInt("is_admin");
                    Preferences.put(getApplicationContext(), Preferences.KEY_LOGIN_TYPE, flag);
                    Preferences.put(getApplicationContext(), Preferences.KEY_IS_LOGIN_COMPLTED, true);
                    Intent intent = new Intent(NewUser.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }else{
                    String message = response.optString("message");
                    if(Utils.isValidString(message)) {
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void error(String error) {
                if(Utils.isActivityDestroyed(NewUser.this)){
                    return;
                }
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean checkValidity() {
        boolean isAllValid = true;
        StringBuffer errorMsg = new StringBuffer();
        if (!Utils.isValidEmailAddress(email.getText().toString())) {
            errorMsg.append("Invalid email id");
            isAllValid = false;
        }
        if (!Utils.isValidPassword(password.getText().toString())) {
            if (Utils.isValidString(errorMsg.toString())) {
                errorMsg.append("\n");
            }
            errorMsg.append("Password should be minimum 5 characters");
            isAllValid = false;
        }
        if(isSignUpScreen) {
            if (!Utils.isValidMobileNumber(phone.getText().toString())) {
                if (Utils.isValidString(errorMsg.toString())) {
                    errorMsg.append("\n");
                }
                errorMsg.append("Invalid mobile number");
                isAllValid = false;
            }
            if (!Utils.isValidString(name.getText().toString())) {
                if (Utils.isValidString(errorMsg.toString())) {
                    errorMsg.append("\n");
                }
                errorMsg.append("Invalid name");
                isAllValid = false;
            }
            if (!Utils.isValidString(lastName.getText().toString())) {
                if (Utils.isValidString(errorMsg.toString())) {
                    errorMsg.append("\n");
                }
                errorMsg.append("Invalid name");
                isAllValid = false;
            }
            if (!Utils.isValidString(city.getText().toString())) {
                if (Utils.isValidString(errorMsg.toString())) {
                    errorMsg.append("\n");
                }
                errorMsg.append("Invalid city");
                isAllValid = false;
            }
            if (!Utils.isValidString(country.getText().toString())) {
                if (Utils.isValidString(errorMsg.toString())) {
                    errorMsg.append("\n");
                }
                errorMsg.append("Invalid country");
                isAllValid = false;
            }
            if (!Utils.isValidString(organization.getText().toString())) {
                if (Utils.isValidString(errorMsg.toString())) {
                    errorMsg.append("\n");
                }
                errorMsg.append("Invalid organization");
                isAllValid = false;
            }
        }
        if (!isAllValid) {
            Toast.makeText(getApplicationContext(), errorMsg.toString(), Toast.LENGTH_SHORT).show();
        }
        return isAllValid;
    }

    private void hideKeyboard() {
        try {
            View view = getCurrentFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch(Throwable e) {}
    }




    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void togglePasswordVisiblity(){
        if(password.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())){
            passwordVisibleIcon.setImageResource(R.drawable.baseline_visibility_off_black_18);
            password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        }
        else{
            passwordVisibleIcon.setImageResource(R.drawable.baseline_visibility_black_18);
            password.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }
    }
}
