package com.averda.online.testseries;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StrikethroughSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
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
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
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
    RecyclerView mRecyclerView,statusRList;
    private GridLayoutManager mLayoutManager,mLayoutManager1;
    private TestSeriesItemAdapter adapter = null;
    private StatusItemAdapter stAdapter = null;
    JSONArray dataArray = new JSONArray();
    JSONArray userArray = new JSONArray();
    private int RESULT_LOAD_IMG = 200;
    private static final int REQUEST_CODE_ASK_PERMISSIONS = 190;
    String imagePath = "";
    private GpsTracker gpsTracker;
    private String tvLatitude,tvLongitude;
    Spinner statusSpinner;
    boolean isAdmin;
    String statusId;
    String comment= "";
    String adminComment= "";
    ImageView cameraImage,location;
    TextView userName,city,creted,adComment,textViewComment;
    LinearLayout locationDetail;
    RelativeLayout adminLayout;
    private TableLayout mTableLayout;
    ArrayList<String> statusList;
    private static final int CAMERA_REQUEST = 1888;
    String PICTURE_PATH = "/Profile Picture/";
    ArrayAdapter<String> dataAdapter = null;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_details);
        metrics = Utils.getMetrics(this);
     //   statusSpinner = findViewById(R.id.statusSpinner);
        bannerImage = findViewById(R.id.bannerImage);
        location = findViewById(R.id.location);
        cameraImage = findViewById(R.id.cameraImage);
        userName = findViewById(R.id.userName);
        city = findViewById(R.id.city);
        creted = findViewById(R.id.createdOn);
        adComment = findViewById(R.id.mainComment);
        textViewComment = findViewById(R.id.textViewComment);
        locationDetail = findViewById(R.id.activity_landing);
        adminLayout = findViewById(R.id.adminView);
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
        //    statusSpinner.setVisibility(View.VISIBLE);
        }
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            String item = bundle.getString("item");
            try {
                isAdmin = false;
                itemObj = new JSONObject(item);
                dataArray = new JSONArray(itemObj.optString("request_data"));
                userArray = new JSONArray(itemObj.optString("order_status"));
                this.setTitle("Request ID #"+itemObj.optString("id"));
                comment  =  itemObj.optString("user_comment");
                JSONObject  userAr = new JSONObject(itemObj.optString("users"));
                userName.setText(itemObj.optString("status_text"));
                city.setText(itemObj.optString("city"));
                creted.setText(itemObj.optString("update_time"));
                adminComment  =  itemObj.optString("admin_comment");
                if(adminComment.equals("")|| adminComment.equals("null")){
                    adminLayout.setVisibility(View.GONE);
                }else{
                    adComment.setText(adminComment);
                    adminLayout.setVisibility(View.VISIBLE);
                }
                if(comment.equals("")|| comment.equals("null")){
                    textViewComment.setText("");
                }else{
                    textViewComment.setText(comment);

                }
                cameraImage.setVisibility(View.INVISIBLE);
                location.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        double lat = itemObj.optDouble("latitude");
                        double longt = itemObj.optDouble("longitude");
                        String uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?daddr=%f,%f (%s)", lat, longt, "");
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                        intent.setPackage("com.google.android.apps.maps");
                        try
                        {
                            startActivity(intent);
                        }
                        catch(ActivityNotFoundException ex)
                        {
                            try
                            {
                                Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                                startActivity(unrestrictedIntent);
                            }
                            catch(ActivityNotFoundException innerEx)
                            {

                                Toast.makeText(getApplicationContext(), "Please install a maps application", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });



//
//                    TableLayout tv=(TableLayout) findViewById(R.id.tableInvoices);
//                for (int i = 0; i < userArray.length(); i++) {
//
//                    String status = userArray.optJSONObject(i).optString("status_text");
//                    String statusColor = userArray.optJSONObject(i).optString("status_color");
//                    String time = userArray.optJSONObject(i).optString("update_time").split("T")[0];
//                    String image = userArray.optJSONObject(i).optString("admin_image");
//                    TableRow tbrow = new TableRow(this);
//
//                    if(statusColor.equals("green")){
//                        tbrow.setBackgroundResource(R.drawable.green);
//                    }else if(statusColor.equals("yellow")){
//                        tbrow.setBackgroundResource(R.drawable.yellow);
//                    }else if(statusColor.equals("blue")){
//                        tbrow.setBackgroundResource(R.drawable.blue);
//                    }else if(statusColor.equals("red")){
//                        tbrow.setBackgroundResource(R.drawable.red);
//                    }else{
//                        tbrow.setBackgroundResource(R.drawable.blue);
//                    }
//
//
//                    TableLayout.LayoutParams tableRowParams=
//                            new TableLayout.LayoutParams
//                                    (TableLayout.LayoutParams.FILL_PARENT,100);
//
//                    int leftMargin=10;
//                    int topMargin=10;
//                    int rightMargin=10;
//                    int bottomMargin=2;
//
//                    tableRowParams.setMargins(leftMargin, topMargin, rightMargin, bottomMargin);
//                    tbrow.setLayoutParams(tableRowParams);
//                    ImageView imageData = new ImageView(this);
//                    Glide.with(this)
//                            .load(image)
//                            .override((int)(60), (int)(60))
//                            .into(imageData);
//                    tbrow.addView(imageData);
//                    TextView t2v = new TextView(this);
//                    t2v.setText(status);
//                    t2v.setTextColor(Color.WHITE);
//                    t2v.setGravity(Gravity.CENTER);
//                    tbrow.addView(t2v);
//                    TextView t3v = new TextView(this);
//                    t3v.setText(time);
//                    t3v.setTextColor(Color.WHITE);
//                    t3v.setGravity(Gravity.CENTER);
//                    tbrow.addView(t3v);
//                    tv.addView(tbrow);
//                    tbrow.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            showImage(image);
//                        }
//                    });
//                }




            }catch (Exception e){
                if(Utils.isDebugModeOn){
                    e.printStackTrace();
                }
            }
//            setImageViewSize();

            setBannerImage();


            if(Utils.isAdmin(getApplicationContext())){
                TextView btnText=(TextView)findViewById(R.id.submitButton);
                btnText.setText("Update detail");
                //findViewById(R.id.submitButton).setText("fg");
           // findViewById(R.id.submitButton).setVisibility(View.GONE);
            }else{
                findViewById(R.id.submitButton).setVisibility(View.GONE);
            }
          //  cameraImage.setVisibility(View.VISIBLE);
        } else {
            isAdmin = true;
            adminLayout.setVisibility(View.GONE);
            locationDetail.setVisibility(View.INVISIBLE);
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
        statusRList = findViewById(R.id.recylerViewStatus);
        if (mRecyclerView != null) {
            mRecyclerView.setHasFixedSize(false);
            statusRList.setHasFixedSize(false);
        }
        if (statusRList != null) {
            statusRList.setHasFixedSize(false);
        }

        statusRList.setNestedScrollingEnabled(false);
        mLayoutManager1 = new GridLayoutManager(getApplicationContext(),1);
        statusRList.setLayoutManager(mLayoutManager1);

        mRecyclerView.setNestedScrollingEnabled(false);
        mLayoutManager = new GridLayoutManager(getApplicationContext(),1);
        mRecyclerView.setLayoutManager(mLayoutManager);
        statusSetList(userArray);
        setList(dataArray);
        findViewById(R.id.submitButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 if(Utils.isAdmin(getApplicationContext())){
                     //updateStatus();
                     showDialog();
                 }
                   // if(!isAdmin) {
//                if(Utils.isAdmin(getApplicationContext())) {
//                    if (imagePath.trim().equalsIgnoreCase("")) {
//                        Toast.makeText(getApplicationContext(), "Image is blank", Toast.LENGTH_LONG).show();
//                    } else {
//                        new Thread(new Runnable() {
//                            @Override
//                            public void run() {
//                                try {
//                                    uploadProfileImage(imagePath);
//                                } catch (Exception e) {
//                                }
//                            }
//                        }).start();
//                    }
//                }
                  //  }
//                    else{
//                        updateStatus();
//                    }
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
        if(Utils.isAdmin(getApplicationContext())) {
            ServerApi.callServerApi(getApplicationContext(), ServerApi.BASE_URL, "getallstatus", params, new ServerApi.CompleteListener() {
                @Override
                public void response(JSONObject response) {
                    String statusCode = response.optString("success");
                    boolean status = response.optBoolean("success");
                    if ("true" == statusCode || status) {
                        JSONArray data = response.optJSONArray("data");
                       statusList = new ArrayList<>();
                        for (int i = 0; i < data.length(); i++) {
                            try {
                                String value = data.getJSONObject(i).getString("title");
                                statusList.add(value);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dataAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, statusList);
                                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            }
                        });

                    }
                }

                @Override
                public void error(String error) {
                }
            });
        }
    }

    public void showImage(String url) {
        Dialog builder = new Dialog(this);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                //nothing;
            }
        });
        int Measuredwidth = 0;
        int Measuredheight = 0;
        Point size = new Point();
        WindowManager w = getWindowManager();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)    {
            w.getDefaultDisplay().getSize(size);
            Measuredwidth = size.x;
            Measuredheight = size.y;
        }else{
            Display d = w.getDefaultDisplay();
            Measuredwidth = d.getWidth();
            Measuredheight = d.getHeight();
        }
        ImageView imageView = new ImageView(this);
        Glide.with(this)
                .load(url)
                .error(R.drawable.camera)
                .override((int)(Measuredwidth-50), (int)(500))
                .placeholder(R.drawable.camera)
                .into(imageView);


        builder.addContentView(imageView, new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        builder.show();
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
                adapter = new TestSeriesItemAdapter(data,comment,isAdmin,"",R.layout.plan_item, this);
                mRecyclerView.setAdapter(adapter);
            }else{
                adapter.refreshAdapter(data,comment,isAdmin,adminComment);
            }
        }
    }

    private void statusSetList(JSONArray data){
        if (data != null) {
            if(stAdapter == null) {
                stAdapter = new StatusItemAdapter(data,R.layout.statusitem, TestPackageDetailsActivity.this);
                statusRList.setAdapter(stAdapter);
            }else{
                stAdapter.refreshAdapter(data);
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
            try {
                selectImage();
            } catch (ActivityNotFoundException e) {
                if (Utils.isDebugModeOn) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void selectImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Library",
                "Cancel" };

        TextView title = new TextView(getApplicationContext());
        title.setText("Add Photo!");
        title.setBackgroundColor(Color.BLACK);
        title.setPadding(10, 15, 15, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setTextSize(22);


        AlertDialog.Builder builder = new AlertDialog.Builder(
                TestPackageDetailsActivity.this);



        builder.setCustomTitle(title);

        // builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                } else if (items[item].equals("Choose from Library")) {
                    Intent galleryIntent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(galleryIntent, RESULT_LOAD_IMG);

                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }



    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void checkForPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int hasGetAccountsPermission = checkSelfPermission(Manifest.permission.CAMERA );
            int hasGetAccountsPermission2 = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);

            if (hasGetAccountsPermission != PackageManager.PERMISSION_GRANTED && hasGetAccountsPermission2 != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_ASK_PERMISSIONS);
            } else {
                try {
                    selectImage();
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
                        try {
                            selectImage();
                        } catch (ActivityNotFoundException e) {
                            if (Utils.isDebugModeOn) {
                                e.printStackTrace();
                            }
                        }
                    } else if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
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
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            bannerImage.setImageBitmap(photo);
            String savePath1 = getFilesDir() + PICTURE_PATH + "images/"
                    + "profile_picture.png";
            saveBitmap(photo, savePath1);
            imagePath = savePath1;
            // CALL THIS METHOD TO GET THE ACTUAL PATH
        }else if (requestCode == RESULT_LOAD_IMG) {
            if (resultCode == Activity.RESULT_OK && null != data) {
                try {
                    final Uri selectedImage = data.getData();
                    Bitmap photo = getBitmapFromUri(selectedImage);
                    bannerImage.setImageBitmap(photo);
                    String savePath1 = getFilesDir() + PICTURE_PATH + "images/"
                            + "profile_picture.png";
                    saveBitmap(photo, savePath1);
                    imagePath = savePath1;
//                    CropImage.activity(selectedImage)
//                            .setGuidelines(CropImageView.Guidelines.ON)
//                            .setAspectRatio(1, 1)
//                            .start(this);
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
                                        cameraImage.setVisibility(View.INVISIBLE);
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

    private Boolean saveBitmap(Bitmap BitmapImage, String savePath) {
        FileOutputStream fos = null;
        try {
            File file = new File(savePath);
            file.delete();
            file.getParentFile().mkdirs();
            if (!file.exists())
                file.createNewFile();
            fos = new FileOutputStream(file);
            BitmapImage.compress(Bitmap.CompressFormat.JPEG, 70, fos);
            fos.flush();
            fos.close();
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image; }
    androidx.appcompat.app.AlertDialog dialog;
    private void showDialog() {
        try {
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(TestPackageDetailsActivity.this);
            LayoutInflater li = LayoutInflater.from(TestPackageDetailsActivity.this);
            View promptsView = li.inflate(R.layout.updatedetail_alert, null);
            Spinner  statusSpinner = promptsView.findViewById(R.id.statusSpinner);
            EditText comment = promptsView.findViewById(R.id.comment);
            Button selectImage = promptsView.findViewById(R.id.buttonLoadPicture);
            String savePass = Utils.userPass(getApplicationContext());
            TextView ok = promptsView.findViewById(R.id.ok);
            selectImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(Utils.isConnectedToInternet(getApplicationContext())) {
                        openPhotos();
                    }else{
                        Toast.makeText(getApplicationContext(), getString(R.string.network_error_1), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        if(Utils.isAdmin(getApplicationContext())) {
                            if (imagePath.trim().equalsIgnoreCase("")) {
                                Toast.makeText(getApplicationContext(), "Image is blank", Toast.LENGTH_LONG).show();
                            } else if (comment.getText().toString().equals("")) {
                                Toast.makeText(getApplicationContext(), "Please enter comment", Toast.LENGTH_LONG).show();
                            } else {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            uploadProfileImage(imagePath, "hello");
                                        } catch (Exception e) {
                                        }
                                    }
                                }).start();
                            }
                        }





//                    if (checkValidity(oldPass.getText().toString(), newPass.getText().toString())) {
//                        JSONObject params = new JSONObject();
//                        try {
//                            params.put("user_id", Utils.getStudentId(getApplicationContext()));
//                            params.put("password", newPass.getText().toString().trim());
//                        } catch (Exception e) {
//                            if (Utils.isDebugModeOn) {
//                                e.printStackTrace();
//                            }
//                        }
//                        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
//                        ServerApi.callServerApi(getApplicationContext(), ServerApi.BASE_URL, "updatepassword", params, new ServerApi.CompleteListener() {
//                            @Override
//                            public void response(JSONObject response) {
//                                dialog.dismiss();
//                                findViewById(R.id.progressBar).setVisibility(View.GONE);
//                                Preferences.put(getApplicationContext(), Preferences.KEY_USER_PASSWORD, newPass.getText().toString().trim());
//                                Toast.makeText(getApplicationContext(), "Successfully Changed Password", Toast.LENGTH_LONG).show();
//                                dialog.dismiss();
//                            }
//
//                            @Override
//                            public void error(String error) {
//                                dialog.dismiss();
//                                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
//                                findViewById(R.id.progressBar).setVisibility(View.GONE);
//                                if (Utils.isActivityDestroyed(TestPackageDetailsActivity.this)) {
//                                    return;
//                                }
//                            }
//                        });
//                    }
                }
            });
            builder.setView(promptsView);
            dialog = builder.create();
            if (!Utils.isActivityDestroyed(TestPackageDetailsActivity.this))
                dialog.show();
            if (statusList.size() > 0) {
                statusSpinner.setAdapter(dataAdapter);
                statusSpinner.setOnItemSelectedListener(TestPackageDetailsActivity.this);
            }
        } catch (Exception e) {
            if (Utils.isDebugModeOn)
                e.printStackTrace();
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


    public boolean uploadProfileImage(String filePath,String admin_comment) {
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
                    .addFormDataPart("request_id", String.valueOf(itemObj.getInt("id")))
                    .addFormDataPart("user_id", Utils.getStudentId(TestPackageDetailsActivity.this)+"")
                    .addFormDataPart("admin_comment", admin_comment)
                    .addFormDataPart("status_id", "3")
                    .addFormDataPart("image", new File(filePath).getName(), RequestBody.create(MediaType.parse(mime), new File(filePath)))
                    .build();
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(ServerApi.BASE_URL+"updatestatus")
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
                                Intent intent = new Intent(TestPackageDetailsActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
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
