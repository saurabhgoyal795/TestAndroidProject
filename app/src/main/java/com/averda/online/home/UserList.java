package com.averda.online.home;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.averda.online.R;
import com.averda.online.server.ServerApi;
import com.averda.online.testseries.TestPackageDetailsActivity;
import com.averda.online.utils.Utils;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class UserList extends AppCompatActivity {
    RecyclerView mRecyclerView;
    private GridLayoutManager mLayoutManager;
    private UserListAdapter adapter = null;
    private HashMap<Integer, JSONArray> listData = new HashMap<>();
    public ArrayList<Integer> courseIds = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        this.setTitle("User list");
        mRecyclerView = findViewById(R.id.recylerView);
        if (mRecyclerView != null) {
            mRecyclerView.setHasFixedSize(true);
        }
        mLayoutManager = new GridLayoutManager(getApplicationContext(),1);
        mRecyclerView.setLayoutManager(mLayoutManager);
        JSONObject params = new JSONObject();
        Log.d("HomeFragment", "id: "+ Utils.getStudentId(getApplicationContext()));
        try{
            boolean isAdmin = Utils.isAdmin(getApplicationContext());
            params.put("user_id", Utils.getStudentId(getApplicationContext()));
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        BottomNavigationView navView = findViewById(R.id.navigation);
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.navigation_home:
                        Utils.openHome(UserList.this);
                        break;
                    case R.id.navigation_classes:
                        Utils.openMyPackagesNewTask(UserList.this, 0);
                        break;
                    case R.id.navigation_test:
                        Utils.openMyPackagesNewTask(UserList.this, 1);
                        break;
                    case R.id.navigation_profile:
                        Utils.openMyProfileNewTask(UserList.this);
                        break;
                }
                return false;
            }
        });
        ServerApi.callServerApi(getApplicationContext(), ServerApi.BASE_URL, "showallusers", params, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {
                if(Utils.isActivityDestroyed(UserList.this)){
                    return;
                }
               findViewById(R.id.progressBar).setVisibility(View.GONE);
                String statusCode = response.optString("success");
                boolean status = response.optBoolean("success");
                if("true"  == statusCode || status) {
                    JSONArray data = response.optJSONArray("data");
                    Utils.saveObject(getApplicationContext(), data.toString(), "showallusers");
                    setList(data);
                }
            }

            @Override
            public void error(String error) {
                if(Utils.isActivityDestroyed(UserList.this)){
                    return;
                }
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void setList(JSONArray data){
        if (data != null) {
            if(adapter == null) {
                adapter = new UserListAdapter(data,R.layout.user_plan_item,UserList.this);
                mRecyclerView.setAdapter(adapter);
            }else{
                adapter.refreshAdapter(data);
            }
        }
    }
    private void checkCacheValues(){
        String value = null;
        try{
            value = (String)Utils.getObject(getApplicationContext(), "getuserallrequest");
            if(Utils.isValidString(value)){
                JSONArray response = new JSONArray(value);
                setList(response);
            }
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
    }
}