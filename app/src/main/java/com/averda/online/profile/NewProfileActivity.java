package com.averda.online.profile;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.averda.online.R;
import com.averda.online.cropper.CropImage;
import com.averda.online.cropper.CropImageView;
import com.averda.online.home.LogoutActivity;
import com.averda.online.preferences.Preferences;
import com.averda.online.server.ServerApi;
import com.averda.online.utils.Utils;

import org.json.JSONObject;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

public class NewProfileActivity extends AppCompatActivity {
    public static final String PROFILE_IMAGE_PATH = "/ProfileImage/profileImage.png";
    private static final int REQUEST_CODE_ASK_PERMISSIONS = 190;
    private int RESULT_LOAD_IMG = 200;
    ImageView profile;
    TextView studentName;
    TextView cityName;
    TextView mobileNumber;
    TextView emailId;
    TextView nameText;
    TextView cityVal;
    ScrollView profileLayout;
    TextView lastName;
    TextView country;
    TextView organization;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_profile);
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        profile = findViewById(R.id.profileImage);
        studentName = findViewById(R.id.studentName);
        cityName = findViewById(R.id.cityName);
        mobileNumber = findViewById(R.id.mobileNumber);
        emailId = findViewById(R.id.emailId);
        nameText = findViewById(R.id.nameText);
        cityVal = findViewById(R.id.cityVal);
        profileLayout = findViewById(R.id.profileLayout);
        lastName = findViewById(R.id.lastnameText);
        organization = findViewById(R.id.organization);
        country = findViewById(R.id.country);
        findViewById(R.id.logoutbutton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        findViewById(R.id.changepassword).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });
        JSONObject params = new JSONObject();
        try {
//            params.put("StudentId", Utils.getStudentId(getApplicationContext()));
              params.put("user_id", Utils.getStudentId(getApplicationContext()));
        } catch (Exception e) {
            if (Utils.isDebugModeOn) {
                e.printStackTrace();
            }
        }
        ServerApi.callServerApi(getApplicationContext(), ServerApi.BASE_URL, "getuserprofile", params, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {
                findViewById(R.id.progressBar).setVisibility(View.GONE);

                if (Utils.isActivityDestroyed(NewProfileActivity.this)) {
                    return;
                }
                Utils.saveObject(getApplicationContext(), response.toString(), "profile");
                JSONObject profileJSONNew = response.optJSONObject("data");
                profileLayout.setVisibility(View.VISIBLE);
                setProfileJSON(profileJSONNew);
            }

            @Override
            public void error(String error) {
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                if (Utils.isActivityDestroyed(NewProfileActivity.this)) {
                    return;
                }
            }
        });
        BottomNavigationView navView = findViewById(R.id.navigation);
        navView.getMenu().getItem(3).setChecked(true);
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        onBackPressed();
                        break;
                    case R.id.navigation_classes:
                        Utils.openMyPackages(NewProfileActivity.this, 0);
                        break;
                    case R.id.navigation_test:
                        Utils.openMyPackages(NewProfileActivity.this, 1);
                        break;
                    case R.id.navigation_profile:
                        break;
                }
                return false;
            }
        });

        findViewById(R.id.pencilIcon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NewProfileActivity.this, ProfileActivity.class));
            }
        });

        findViewById(R.id.card_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NewProfileActivity.this, TransactionActivity.class));
            }
        });
        profile = findViewById(R.id.profileImage);
    }


    private void setProfileJSON(JSONObject data) {
        try {
            emailId.setText(data.optString("email"));
            studentName.setText(data.optString("first_name"));
            nameText.setText(data.optString("first_name"));
           lastName.setText(data.optString("last_name"));
            mobileNumber.setText(data.optString("phone"));
            cityName.setText(data.optString("city"));
            cityVal.setText(data.optString("city"));
            country.setText(data.optString("country"));
            organization.setText(data.optString("organization"));
        } catch (Exception e) {

        }
    }

    private void logout() {
        startActivity(new Intent(this, LogoutActivity.class));
    }



    androidx.appcompat.app.AlertDialog dialog;
    private void showDialog() {
        try {
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(NewProfileActivity.this);
            LayoutInflater li = LayoutInflater.from(NewProfileActivity.this);
            View promptsView = li.inflate(R.layout.changepassword_alert, null);
            EditText oldPass = promptsView.findViewById(R.id.oldPass);
            EditText newPass = promptsView.findViewById(R.id.newPass);
            TextView ok = promptsView.findViewById(R.id.ok);
            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
//                    JSONObject params = new JSONObject();
//                    try {
//                        params.put("StudentId", Utils.getStudentId(getApplicationContext()));
//                        params.put("Passwords", oldPass.getText().toString().trim());
//                        params.put("NewPassword", newPass.getText().toString().trim());
//                    } catch (Exception e) {
//                        if (Utils.isDebugModeOn) {
//                            e.printStackTrace();
//                        }
//                    }
//                    findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
//                    ServerApi.callServerApi(getApplicationContext(), ServerApi.BASE_URL, "ChangePassword", params, new ServerApi.CompleteListener() {
//                        @Override
//                        public void response(JSONObject response) {
//                            dialog.dismiss();
//                            findViewById(R.id.progressBar).setVisibility(View.GONE);
//                            Toast.makeText(getApplicationContext(), "Successfully Changed Password", Toast.LENGTH_LONG).show();
//                        }
//                        @Override
//                        public void error(String error) {
//                            dialog.dismiss();
//                            Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
//                            findViewById(R.id.progressBar).setVisibility(View.GONE);
//                            if (Utils.isActivityDestroyed(NewProfileActivity.this)) {
//                                return;
//                            }
//                        }
//                    });
                }
            });
            builder.setView(promptsView);
            dialog = builder.create();
            if (!Utils.isActivityDestroyed(NewProfileActivity.this))
                dialog.show();
        } catch (Exception e) {
            if (Utils.isDebugModeOn)
                e.printStackTrace();
        }
    }
}