package com.averda.online.mypackage.onlineClass;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.MenuItem;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.averda.online.R;
import com.averda.online.common.ZTAppCompatActivity;
import com.averda.online.preferences.Preferences;
import com.averda.online.server.ServerApi;
import com.averda.online.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MyPackageClassDetailsActivity extends ZTAppCompatActivity {
    public static final String REFRESH_REQUEST = "com.zonetech.download";
    private JSONObject itemObj;
    RecyclerView mRecyclerView;
    private GridLayoutManager mLayoutManager;
    private MyPackageClassProductItemAdapter adapter = null;
    BottomNavigationView navView;
    public static int planId;
    BroadcastReceiver event = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setList();
        }
    };
    JSONArray selectedDataArray;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_package_class_details);
        navView = findViewById(R.id.navigation);
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            String item = bundle.getString("item");
            try {
                itemObj = new JSONObject(item);
                planId = itemObj.optInt("PlanID");
                setTitle(itemObj.optString("PlanName"));
            }catch (Exception e){
                if(Utils.isDebugModeOn){
                    e.printStackTrace();
                }
            }
        }
        mRecyclerView = findViewById(R.id.recylerView);
        if (mRecyclerView != null) {
            mRecyclerView.setHasFixedSize(true);
        }
        mLayoutManager = new GridLayoutManager(getApplicationContext(),1);
        mRecyclerView.setLayoutManager(mLayoutManager);
        getPlanDetails();
        navView.getMenu().getItem(1).setChecked(true);
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.navigation_home:
                        Utils.openHome(MyPackageClassDetailsActivity.this);
                        break;
                    case R.id.navigation_classes:
                        Utils.openMyPackages(MyPackageClassDetailsActivity.this, 0);
                        break;
                    case R.id.navigation_test:
                        Utils.openMyPackages(MyPackageClassDetailsActivity.this, 1);
                        break;
                    case R.id.navigation_profile:
                        Utils.openMyProfileNewTask(MyPackageClassDetailsActivity.this);
                        break;
                }
                return false;
            }
        });
        LocalBroadcastManager.getInstance(this).registerReceiver(event, new IntentFilter(REFRESH_REQUEST));
    }

    private void setViews(JSONArray data){
        selectedDataArray = new JSONArray();
        if (data != null){
            for(int i = 0 ; i<data.length(); i++){
                try {
                    if (data.getJSONObject(i).optBoolean("IsSelected") == true) {
                        selectedDataArray.put(data.getJSONObject(i));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            setList();
        }
    }

    private void setList(){
        if(selectedDataArray != null && selectedDataArray.length() > 0) {
            if (adapter == null) {
                adapter = new MyPackageClassProductItemAdapter(selectedDataArray, R.layout.myclass_product_subject_item, MyPackageClassDetailsActivity.this);
                mRecyclerView.setAdapter(adapter);
            } else {
                adapter.refreshAdapter(selectedDataArray);
            }
        }
    }



    private void getPlanDetails(){
        JSONObject params = new JSONObject();
        try{
            int specializationId = Preferences.get(getApplicationContext(), Preferences.KEY_SPEC_ID, 0);
            params.put("OrgPlanID", itemObj.optInt("PlanID"));
            params.put("StudentID", Utils.getStudentId(getApplicationContext()));
            params.put("SpecializationID", specializationId);

        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        ServerApi.callServerApi(this, ServerApi.BASE_URL, "OnlineClassesByPackage", params, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {
                if(Utils.isActivityDestroyed(MyPackageClassDetailsActivity.this)){
                    return;
                }
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                JSONArray data = response.optJSONArray("Body");
                setViews(data);
            }

            @Override
            public void error(String error) {
                findViewById(R.id.progressBar).setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(event);
    }
}