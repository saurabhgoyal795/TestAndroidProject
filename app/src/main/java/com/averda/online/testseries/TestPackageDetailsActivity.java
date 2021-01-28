package com.averda.online.testseries;

import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StrikethroughSpan;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.averda.online.R;
import com.averda.online.common.ZTAppCompatActivity;
import com.averda.online.preferences.Preferences;
import com.averda.online.server.ServerApi;
import com.averda.online.utils.Utils;
import com.averda.online.views.ZTWebView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;


public class TestPackageDetailsActivity extends ZTAppCompatActivity implements View.OnClickListener {
    private ImageView bannerImage;
    private DisplayMetrics metrics;
    private int imageWidth;
    private int imageHeigth;
    private JSONObject itemObj;
    private String pdfPath;
    private String packageName;
    private JSONObject demoTestItem;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_details);
        metrics = Utils.getMetrics(this);
        bannerImage = findViewById(R.id.bannerImage);
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            String item = bundle.getString("item");
            try {
                itemObj = new JSONObject(item);
                setTitle(itemObj.optString("OrgPlanName"));
            }catch (Exception e){
                if(Utils.isDebugModeOn){
                    e.printStackTrace();
                }
            }
            if(Utils.isLollipop()) {
                int position = bundle.getInt("position");
                bannerImage.setTransitionName("test_"+position);
            }
        }
        findViewById(R.id.schedule).setOnClickListener(this);
        findViewById(R.id.buyButton).setOnClickListener(this);
        findViewById(R.id.freeDemo).setOnClickListener(this);
        setImageViewSize();
        setBannerImage();
        setPriceText(itemObj);
        checkSavedValues();
        getPlanDetails();
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
    }

    private void getPlanDetails(){
        JSONObject params = new JSONObject();
        try{
            params.put("OrgPlanID", itemObj.optInt("OrgPlanID"));
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        ServerApi.callServerApi(this, ServerApi.BASE_URL, "PackageDetails", params, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {
                if(Utils.isActivityDestroyed(TestPackageDetailsActivity.this)){
                    return;
                }
                JSONObject data = response.optJSONObject("Body");
                saveObject(data);
                setUI(data);
                fetchDemoTest();
            }

            @Override
            public void error(String error) {
                findViewById(R.id.progressBar).setVisibility(View.GONE);
            }
        });
    }

    private void setUI(JSONObject data){
        findViewById(R.id.progressBar).setVisibility(View.GONE);
        JSONArray schedule = data.optJSONArray("Schedule");
        data = data.optJSONObject("PlanDetails");
        packageName = data.optString("OrgPlanName");
        setViews(data);
        setScheduleView(schedule);
        findViewById(R.id.mainLayout).setVisibility(View.VISIBLE);
    }

    private void setViews(JSONObject data){
        String description  = data.optString("Highlights");
        ZTWebView descriptionView = findViewById(R.id.description);
        descriptionView.loadData(description, "text/html", "UTF-8");
        setPriceText(data);
        setTitle(data.optString("OrgPlanName"));
        ((TextView)findViewById(R.id.startDate)).setText(data.optString("StartDate"));
        ((TextView)findViewById(R.id.endDate)).setText(data.optString("EndDate"));
        ((TextView)findViewById(R.id.totalTests)).setText(data.optString("TotalExam"));
        setBannerImage();
    }

    private void setPriceText(JSONObject data){
        try{
            TextView priceView = findViewById(R.id.priceView);
            double mrp = data.optDouble("PlanMRP");
            double price = data.optDouble("Fees");
            String currency = getString(R.string.currency);
            long discount = Math.round(((mrp - price)/mrp)*100);
            String mrpString = currency + Math.round(mrp);
            String priceString = currency + Math.round(price);
            String text = String.format(getResources().getString(R.string.price_value), mrpString, priceString, discount+"%");
            int index = text.indexOf(mrpString);
            SpannableString strNew = new SpannableString(text);
            StrikethroughSpan span = new StrikethroughSpan();
            strNew.setSpan(span, index, index + String.valueOf(mrpString).length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            priceView.setText(strNew);
        }catch(Throwable e){
            e.printStackTrace();
        }
    }

    private void setImageViewSize(){
        imageWidth = metrics.widthPixels;
        imageHeigth = (491 * imageWidth)/858;
        bannerImage.getLayoutParams().height = imageHeigth;
        bannerImage.getLayoutParams().width = imageWidth;
    }
    private void setBannerImage(){
        String imagePath = itemObj.optString("ImageURL");
        if(Utils.isValidString(imagePath)) {
            imagePath = ServerApi.IMAGE_URL + imagePath;
            if(Utils.isActivityDestroyed(this)){
                return;
            }
            Glide.with(this)
                    .load(imagePath)
                    .override(imageWidth, imageHeigth)
                    .error(R.drawable.samplepackage)
                    .placeholder(R.drawable.samplepackage)
                    .into(bannerImage);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.freeDemo:
                startDemoTest();
                break;
            case R.id.schedule:
                break;
            case R.id.buyButton:
                startPayment();
        }
    }
    private void setScheduleView(JSONArray array){
        for (int i = 0 ; i < array.length() ; i++){
            if(array.optJSONObject(i).optInt("SpecializationId") == Preferences.get(getApplicationContext(), Preferences.KEY_SPEC_ID, 0)){
                pdfPath = array.optJSONObject(i).optString("FilePath");
                if(Utils.isValidString(pdfPath)){
                    findViewById(R.id.schedule).setVisibility(View.VISIBLE);
                }
                break;
            }
        }
    }

    private void checkSavedValues(){
        JSONObject data = getItemObj();
        if(data != null){
            setUI(data);
        }
    }
    private JSONObject getItemObj(){
        try{
            HashMap<Integer, String> detailsMaps = (HashMap<Integer, String>) Utils.getObject(this, "seriesDetails");
            if(detailsMaps != null){
                String item = detailsMaps.get(itemObj.optInt("OrgPlanID"));
                if(Utils.isValidString(item)){
                    return new JSONObject(item);
                }
            }
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        return null;
    }
    private void saveObject(JSONObject data){
        HashMap<Integer, String> detailsMaps = null;
        try{
            detailsMaps = (HashMap<Integer, String>) Utils.getObject(this, "seriesDetails");
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        if(detailsMaps == null){
            detailsMaps = new HashMap<>();
        }
        detailsMaps.put(itemObj.optInt("OrgPlanID"), data.toString());
        Utils.saveObject(this, detailsMaps, "seriesDetails");
    }

    private void startPayment(){
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 100 && resultCode == RESULT_OK){
            finish();
        }
    }

    private void fetchDemoTest(){

    }

    private void startDemoTest(){

    }
}
