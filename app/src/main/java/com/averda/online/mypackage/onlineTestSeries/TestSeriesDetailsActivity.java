package com.averda.online.mypackage.onlineTestSeries;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.averda.online.R;
import com.averda.online.common.ZTAppCompatActivity;
import com.averda.online.preferences.Preferences;
import com.averda.online.server.ServerApi;
import com.averda.online.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class TestSeriesDetailsActivity extends ZTAppCompatActivity {
    public static final String ACTIVE = "Active";
    public static final String UPCOMING = "Upcoming";
    public static final String COMPLETED = "Completed";
    public static final String MISSED = "Missed";

    private SectionsPagerAdapter sectionsPagerAdapter;
    private String[] TAB_TITLES;
    private JSONObject itemObj;
    private HashMap<String, JSONArray> tabsValues;
    BottomNavigationView navView;
    private Handler handler;
    private Runnable fetcher = new Runnable() {
        @Override
        public void run() {
            if(handler == null){
                return;
            }
            getPlanDetails();
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testseries_details);

        Bundle bundle = getIntent().getExtras();
        String item = bundle.getString("item");
        try {
            itemObj = new JSONObject(item);
            setTitle(itemObj.optString("PlanName"));
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        TAB_TITLES = new String[]{getString(R.string.active), getString(R.string.upcoming), getString(R.string.completed), getString(R.string.missed_test)};
        navView = findViewById(R.id.navigation);
        navView.getMenu().getItem(2).setChecked(true);
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.navigation_home:
                        Utils.openHome(TestSeriesDetailsActivity.this);
                        break;
                    case R.id.navigation_classes:
                        Utils.openMyPackages(TestSeriesDetailsActivity.this, 0);
                        break;
                    case R.id.navigation_test:
                        Utils.openMyPackages(TestSeriesDetailsActivity.this, 1);
                        break;
                    case R.id.navigation_profile:
                        Utils.openMyProfileNewTask(TestSeriesDetailsActivity.this);
                        break;
                }
                return false;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        getPlanDetails();

    }

    @Override
    protected void onStop() {
        super.onStop();
        stopFetchTimer();
    }

    private void startFetchTimer(){
        stopFetchTimer();
        if(Utils.isActivityDestroyed(this)){
            return;
        }
        handler = new Handler();
        handler.postDelayed(fetcher, 2*60*1000);
    }

    private void stopFetchTimer(){
        if(handler != null){
            handler.removeCallbacks(fetcher);
            handler = null;
        }
    }

    private void setTabsValues(JSONArray data){
        tabsValues = new HashMap<>();
        for (int i = 0 ; i < data.length() ; i++){
            switch (data.optJSONObject(i).optInt("ExamStatus")){
                case 0:
                    JSONArray upComingArray = tabsValues.get(UPCOMING);
                    if(upComingArray == null){
                        upComingArray = new JSONArray();
                    }
                    upComingArray.put(data.optJSONObject(i));
                    tabsValues.put(UPCOMING, upComingArray);
                    break;
                case 1:
                case 2:
                    JSONArray activeArray = tabsValues.get(ACTIVE);
                    if(activeArray == null){
                        activeArray = new JSONArray();
                    }
                    activeArray.put(data.optJSONObject(i));
                    tabsValues.put(ACTIVE, activeArray);
                    break;
                case 3:
                    JSONArray completedArray = tabsValues.get(COMPLETED);
                    if(completedArray == null){
                        completedArray = new JSONArray();
                    }
                    completedArray.put(data.optJSONObject(i));
                    tabsValues.put(COMPLETED, completedArray);
                    break;
                case 4:
                    JSONArray missedArray = tabsValues.get(MISSED);
                    if(missedArray == null){
                        missedArray = new JSONArray();
                    }
                    missedArray.put(data.optJSONObject(i));
                    tabsValues.put(MISSED, missedArray);
                    break;
            }
        }
        if(sectionsPagerAdapter != null){
            sectionsPagerAdapter.notifyDataSetChanged();
        }else {
            ViewPager viewPager = findViewById(R.id.view_pager);
            sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
            viewPager.setAdapter(sectionsPagerAdapter);
            TabLayout tabs = findViewById(R.id.tabs);
            tabs.setupWithViewPager(viewPager);
        }
    }
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
        @Override
        public Fragment getItem(int position) {
            Fragment testFragment = new TestSeriesDetailsFragment();
            Bundle bundle = new Bundle();
            switch (position){
                case 0:
                    bundle.putString("item", tabsValues.get(ACTIVE) != null ? tabsValues.get(ACTIVE).toString() : "[]");
                    bundle.putString("type", ACTIVE);
                    break;
                case 1:
                    bundle.putString("item", tabsValues.get(UPCOMING) != null ? tabsValues.get(UPCOMING).toString() : "[]");
                    bundle.putString("type", UPCOMING);
                    break;
                case 2:
                    bundle.putString("item", tabsValues.get(COMPLETED) != null ? tabsValues.get(COMPLETED).toString() : "[]");
                    bundle.putString("type", COMPLETED);
                    break;
                case 3:
                    bundle.putString("item", tabsValues.get(MISSED) != null ? tabsValues.get(MISSED).toString() : "[]");
                    bundle.putString("type", MISSED);
                    break;
            }
            testFragment.setArguments(bundle);
            return testFragment;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return TAB_TITLES[position];
        }

        @Override
        public int getCount() {
            return TAB_TITLES.length;
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
        ServerApi.callServerApi(this, ServerApi.BASE_URL, "ExamByPackage", params, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {
                if(Utils.isActivityDestroyed(TestSeriesDetailsActivity.this)){
                    return;
                }
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                JSONArray data = response.optJSONArray("Body");
                setTabsValues(data);
                startFetchTimer();
            }

            @Override
            public void error(String error) {
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                if(Utils.isActivityDestroyed(TestSeriesDetailsActivity.this)){
                    return;
                }
                startFetchTimer();
            }
        });
    }

    public void showDialog() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater li = LayoutInflater.from(this);
            View promptsView = li.inflate(R.layout.saveexamcenter_alert, null);
            builder.setView(promptsView);
            AlertDialog dialog = builder.create();
            TextView submitButton = promptsView.findViewById(R.id.submitButton);
            EditText rollNo = promptsView.findViewById(R.id.rollNo);
            EditText examCenter = promptsView.findViewById(R.id.examCenter);
            EditText picker = promptsView.findViewById(R.id.dob);
            picker.setInputType(InputType.TYPE_NULL);

            final Calendar myCalendar = Calendar.getInstance();
            DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear,
                                      int dayOfMonth) {
                    // TODO Auto-generated method stub
                    myCalendar.set(Calendar.YEAR, year);
                    myCalendar.set(Calendar.MONTH, monthOfYear);
                    myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    String myFormat = "dd/MM/yyyy"; //In which you need put here
                    SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                    picker.setText(sdf.format(myCalendar.getTime()));
                }
            };

            picker.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub

                    DatePickerDialog datePicker = new DatePickerDialog(TestSeriesDetailsActivity.this, date, myCalendar
                            .get(Calendar.YEAR) - 20, myCalendar.get(Calendar.MONTH),
                            myCalendar.get(Calendar.DAY_OF_MONTH));
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            datePicker .show();

                        }
                    }, 300);
                }
            });
            submitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(submitExamCenter(rollNo.getText().toString(), examCenter.getText().toString(), picker.getText().toString())){
                        dialog.dismiss();
                    }
                }
            });
            if (!Utils.isActivityDestroyed(this))
                dialog.show();
        } catch (Exception e) {
            if (Utils.isDebugModeOn)
                e.printStackTrace();
        }
    }

    private boolean submitExamCenter(String rollNo, String examCenter, String dob){
        String msg = "";
        if(!Utils.isValidString(rollNo)){
            msg = "Invalid roll number";
        }
        if(!Utils.isValidString(examCenter)){
            if(Utils.isValidString(msg)){
                msg = msg + ", ";
            }
            msg = msg + "Invalid exam center";
        }
        if(!Utils.isValidString(dob)){
            if(Utils.isValidString(msg)){
                msg = msg + ", ";
            }
            msg = msg + "Invalid date of birth";
        }
        if(Utils.isValidString(msg)){
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            return false;
        }
        saveExamCenter(rollNo, examCenter, dob);
        return true;
    }

    private void saveExamCenter(String rollNo, String examCenter, String dob){
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        JSONObject params = new JSONObject();
        try{
            params.put("StudentID", Utils.getStudentId(this));
            params.put("RollNo", rollNo);
            params.put("ExamCenter", examCenter);
            params.put("DOB", dob);
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        ServerApi.callServerApi(this, ServerApi.BASE_URL, "SaveExamCenter", params, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                Toast.makeText(TestSeriesDetailsActivity.this, "Submitted sucessfully!", Toast.LENGTH_SHORT).show();
                Preferences.put(TestSeriesDetailsActivity.this, Preferences.KEY_IS_TEST_FORM_SUBMITTED, true);
            }

            @Override
            public void error(String error) {
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                Toast.makeText(TestSeriesDetailsActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}