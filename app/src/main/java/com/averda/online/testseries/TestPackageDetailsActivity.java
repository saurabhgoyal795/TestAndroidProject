package com.averda.online.testseries;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StrikethroughSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.averda.online.cropper.CropImage;
import com.averda.online.cropper.CropImageView;
import com.averda.online.home.MainActivity;
import com.averda.online.login.LoginActivity;
import com.averda.online.profile.NewProfileActivity;
import com.averda.online.utils.GpsTracker;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.averda.online.R;
import com.averda.online.common.ZTAppCompatActivity;
import com.averda.online.preferences.Preferences;
import com.averda.online.server.ServerApi;
import com.averda.online.utils.Utils;
import com.averda.online.views.ZTWebView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;


public class TestPackageDetailsActivity extends ZTAppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private ImageView bannerImage;
    private EditText commentBox;
    private DisplayMetrics metrics;
    private int imageWidth;
    private int imageHeigth;
    private JSONObject itemObj;
    private String pdfPath;
    private String packageName;
    private JSONObject demoTestItem;
    RecyclerView mRecyclerView;
    private GridLayoutManager mLayoutManager;
    private TestSeriesItemAdapter adapter = null;
    JSONArray dataArray = new JSONArray();
    private int RESULT_LOAD_IMG = 200;
    private static final int REQUEST_CODE_ASK_PERMISSIONS = 190;
    String imagePath = "";
    private GpsTracker gpsTracker;
    private String tvLatitude,tvLongitude;
    Spinner statusSpinner;
    boolean isAdmin;
    String statusId;
    ImageView cameraImage;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_details);
        metrics = Utils.getMetrics(this);
        statusSpinner = findViewById(R.id.statusSpinner);
        bannerImage = findViewById(R.id.bannerImage);
        cameraImage = findViewById(R.id.cameraImage);
      //  commentBox = findViewById(R.id.queryBox);
        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        gpsTracker = new GpsTracker(TestPackageDetailsActivity.this);
        isAdmin = Utils.isAdmin(getApplicationContext());
        if(isAdmin){
            statusSpinner.setVisibility(View.VISIBLE);
        }
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            String item = bundle.getString("item");
            try {
                itemObj = new JSONObject(item);
                dataArray = new JSONArray(itemObj.optString("request_data"));
            }catch (Exception e){
                if(Utils.isDebugModeOn){
                    e.printStackTrace();
                }
            }
//            setImageViewSize();

            setBannerImage();


            if(!isAdmin){
            findViewById(R.id.submitButton).setVisibility(View.GONE);
            }
            cameraImage.setVisibility(View.VISIBLE);
        } else {
            cameraImage.setVisibility(View.GONE);
//            setImageViewSize();
            setCameraImage();
            setDataArray();
            findViewById(R.id.submitButton).setVisibility(View.VISIBLE);
        }
        findViewById(R.id.progressBar).setVisibility(View.GONE);
        BottomNavigationView navView = findViewById(R.id.navigation);
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.navigation_home:
                        Utils.openHome(TestPackageDetailsActivity.this);
                        break;
                    case R.id.navigation_classes:
                        Utils.openMyPackagesNewTask(TestPackageDetailsActivity.this, 0);
                        break;
                    case R.id.navigation_test:
                        Utils.openMyPackagesNewTask(TestPackageDetailsActivity.this, 1);
                        break;
                    case R.id.navigation_profile:
                        Utils.openMyProfileNewTask(TestPackageDetailsActivity.this);
                        break;
                }
                return false;
            }
        });
        mRecyclerView = findViewById(R.id.recylerView);
        if (mRecyclerView != null) {
            mRecyclerView.setHasFixedSize(true);
        }
        mLayoutManager = new GridLayoutManager(getApplicationContext(),1);
        mRecyclerView.setLayoutManager(mLayoutManager);
        setList(dataArray);
        findViewById(R.id.submitButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    if(!isAdmin) {
                        if (imagePath.trim().equalsIgnoreCase("")) {
                            Toast.makeText(getApplicationContext(), "Image is blank", Toast.LENGTH_LONG).show();
                        } else {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        uploadProfileImage(imagePath);
                                    } catch (Exception e) {
                                    }
                                }
                            }).start();
                        }
                    }else{
                        updateStatus();
                    }
            }
        });
        getLocation();
        JSONObject params = new JSONObject();
        try{
            params.put("StudentId",  Utils.getStudentId(getApplicationContext()));
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        if(isAdmin) {
            ServerApi.callServerApi(getApplicationContext(), ServerApi.BASE_URL, "getallstatus", params, new ServerApi.CompleteListener() {
                @Override
                public void response(JSONObject response) {
                    String statusCode = response.optString("success");
                    boolean status = response.optBoolean("success");
                    if ("true" == statusCode || status) {
                        JSONArray data = response.optJSONArray("data");
                        ArrayList<String> statusList = new ArrayList<>();
                        for (int i = 0; i < data.length(); i++) {
                            try {
                                String value = data.getJSONObject(i).getString("title");
                                statusList.add(value);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                        if (statusList.size() > 0) {
                            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, statusList);
                            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            statusSpinner.setAdapter(dataAdapter);
                            statusSpinner.setOnItemSelectedListener(TestPackageDetailsActivity.this);
                        }
                    }
                }

                @Override
                public void error(String error) {
                }
            });
        }
    }

    public void getLocation(){

        if(gpsTracker.canGetLocation()){
            double latitude = gpsTracker.getLatitude();
            double longitude = gpsTracker.getLongitude();
            tvLatitude = String.valueOf(latitude);
            tvLongitude = String.valueOf(longitude);
        }else{
            gpsTracker.showSettingsAlert();
        }
    }

    private void setDataArray() {
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        JSONObject params = new JSONObject();
        try{
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        ServerApi.callServerApi(getApplicationContext(), ServerApi.BASE_URL, "getrequestform", params, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                String statusCode = response.optString("success");
                boolean status = response.optBoolean("success");
                if("true"  == statusCode || status) {
                    JSONArray data = response.optJSONArray("data");
                    setList(data);
                }
            }

            @Override
            public void error(String error) {
                if(Utils.isActivityDestroyed(TestPackageDetailsActivity.this)){
                    return;
                }
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                Toast.makeText(TestPackageDetailsActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setList(JSONArray data){
        if (data != null) {
            if(adapter == null) {
                adapter = new TestSeriesItemAdapter(data,R.layout.plan_item, this);
                mRecyclerView.setAdapter(adapter);
            }else{
                adapter.refreshAdapter(data);
            }
        }
    }

    private void setImageViewSize(){
        imageWidth = metrics.widthPixels;
        imageHeigth = (491 * imageWidth)/858;
        bannerImage.getLayoutParams().height = imageHeigth;
        bannerImage.getLayoutParams().width = imageWidth;
    }

    private void setCameraImage() {
        Glide.with(this)
                .load("")
                .override(imageWidth, imageHeigth)
                .error(R.drawable.camera)
                .placeholder(R.drawable.camera)
                .into(bannerImage);
        bannerImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Utils.isConnectedToInternet(getApplicationContext())) {
                    openPhotos();
                }else{
                    Toast.makeText(getApplicationContext(), getString(R.string.network_error_1), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void openPhotos() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkForPermissions();
        } else {
            Intent galleryIntent = new Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            try {
                startActivityForResult(galleryIntent,
                        RESULT_LOAD_IMG);
            } catch (ActivityNotFoundException e) {
                if (Utils.isDebugModeOn) {
                    e.printStackTrace();
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void checkForPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int hasGetAccountsPermission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            if (hasGetAccountsPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_ASK_PERMISSIONS);
            } else {
                Intent galleryIntent = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                try {
                    startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
                } catch (ActivityNotFoundException e) {
                    if (Utils.isDebugModeOn) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        try {
            switch (requestCode) {
                case REQUEST_CODE_ASK_PERMISSIONS: {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Intent galleryIntent = new Intent(
                                Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        try {
                            startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
                        } catch (ActivityNotFoundException e) {
                            if (Utils.isDebugModeOn) {
                                e.printStackTrace();
                            }
                        }
                    } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        showWhyWeNeedPermissionDialog(R.string.perm_readexternal_why_we_need_message);
                    } else {
                        showEnablePermissionInSettingsDialog(R.string.perm_readexternal_go_to_settings_message);
                    }
                    break;
                }
                default: {
                    super.onRequestPermissionsResult(requestCode, permissions,
                            grantResults);
                }
            }
        } catch (Exception e) {
            if (Utils.isDebugModeOn) {
                e.printStackTrace();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void showWhyWeNeedPermissionDialog(int messageId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this,
                android.R.style.Theme_Material_Light_Dialog);
        builder.setCancelable(false);
        builder.setMessage(messageId);
        builder.setPositiveButton(R.string.unlock_confirm_accept,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create();
        if (!Utils.isActivityDestroyed(this))
            builder.show();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void showEnablePermissionInSettingsDialog(int messageId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this,
                android.R.style.Theme_Material_Light_Dialog);
        builder.setCancelable(false);
        builder.setMessage(messageId);
        builder.setPositiveButton(R.string.unlock_confirm_accept,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();


                    }
                });
        builder.create();
        if (!Utils.isActivityDestroyed(this))
            builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMG) {
            if (resultCode == Activity.RESULT_OK && null != data) {
                try {
                    final Uri selectedImage = data.getData();
                    CropImage.activity(selectedImage)
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setAspectRatio(1, 1)
                            .start(this);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.picked_no_image), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                 imagePath = result.getUri().getPath();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            File imgFile = new  File(imagePath);
                            if(imgFile.exists()){
                                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        bannerImage.setImageBitmap(myBitmap);
                                        bannerImage.setVisibility(View.VISIBLE);
                                        cameraImage.setVisibility(View.VISIBLE);
                                    }
                                });
                            }
                        } catch (Exception e) {
                            if (Utils.isDebugModeOn) {
                                e.printStackTrace();
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                                }
                            });
                        }
                    }
                }).start();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.picked_no_image), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean updateStatus() {
        JSONObject params = new JSONObject();
        try{
            params.put("request_id", itemObj.getInt("id"));
            params.put("user_id", itemObj.getInt("users_id"));
            params.put("status_id", statusId);
            params.put("admin_comment", "hello");
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        ServerApi.callServerApi(this, ServerApi.BASE_URL, "updatestatus", params, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {
                if(Utils.isActivityDestroyed(TestPackageDetailsActivity.this)){
                    return;
                }
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                String statusCode = response.optString("success");
                if("true"  == statusCode) {
                    Intent intent = new Intent(TestPackageDetailsActivity.this, MainActivity.class);
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
                if(Utils.isActivityDestroyed(TestPackageDetailsActivity.this)){
                    return;
                }
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
      return  false;

    }


    public boolean uploadProfileImage(String filePath) {
        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build();

            String mime = getMimeType(filePath);
            if (tvLatitude == null) {
                tvLatitude = "";
            }
            if (tvLongitude == null) {
                tvLongitude = "";
            }
            RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("user_id", Utils.getStudentId(TestPackageDetailsActivity.this)+"")
                    .addFormDataPart("comment", adapter.getCommentText())
                    .addFormDataPart("latitude", tvLatitude)
                    .addFormDataPart("longitude", tvLongitude)
                    .addFormDataPart("questions", adapter.getItemCheckedList())
                    .addFormDataPart("image", new File(filePath).getName(), RequestBody.create(MediaType.parse(mime), new File(filePath)))
                    .build();
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(ServerApi.BASE_URL+"submitrequest")
                    .method("POST", body)
                    .header("accept", "application/json")
                    .build();
            okhttp3.Response response = client.newCall(request).execute();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (response.isSuccessful()) {
                            String res = response.body().string();
                            JSONObject object = new JSONObject(res);
                            String statusCode = object.optString("success");
                            boolean status = object.optBoolean("success");
                            if("true"  == statusCode || status) {
                                Toast.makeText(getApplicationContext(), "Form request submitted successfully", Toast.LENGTH_SHORT).show();
                                Preferences.put(getApplicationContext(), Preferences.KEY_STUDENT_PROFILE_PIC, imagePath);
                            } else {
                                Toast.makeText(getApplicationContext(), object.optString("message"), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }catch (Exception e){
                        if(Utils.isDebugModeOn){
                            e.printStackTrace();
                        }
                    }
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                }
            });
            return true;
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Form request not updated, try again", Toast.LENGTH_SHORT).show();
                }
            });
        }
        return false;
    }


    private void setProfileImage() {
        DisplayMetrics metrics = Utils.getMetrics(this);
        String fileName = Preferences.get(getApplicationContext(), Preferences.KEY_STUDENT_PROFILE_PIC, "");
        if(Utils.isValidString(fileName)){
            Glide.with(this)
                    .load( fileName)
                    .override((int)(60*metrics.density), (int)(60*metrics.density))
                    .circleCrop()
                    .into(bannerImage);
        }
    }

    private String getMimeType(String path) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentTypeFor = fileNameMap.getContentTypeFor(path);
        if (contentTypeFor == null)
        {
            contentTypeFor = "application/octet-stream";
        }
        return contentTypeFor;
    }


    private void setBannerImage(){
        String imagePath = itemObj.optString("image");
        if(Utils.isValidString(imagePath)) {
            Glide.with(this)
                    .load(imagePath)
                    .override(imageWidth, imageHeigth)
                    .error(R.drawable.waste_material)
                    .placeholder(R.drawable.waste_material)
                    .into(bannerImage);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(position==0){
            statusId = "1";
        }else  if(position==1){
            statusId = "2";
        }else  if(position==2){
            statusId = "3";
        }else  if(position==3){
            statusId = "4";
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
