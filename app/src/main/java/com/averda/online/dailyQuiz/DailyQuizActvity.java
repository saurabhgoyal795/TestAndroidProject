package com.averda.online.dailyQuiz;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.averda.online.R;
import com.averda.online.common.ZTAppCompatActivity;
import com.averda.online.server.ServerApi;
import com.averda.online.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

public class DailyQuizActvity extends ZTAppCompatActivity {
    RecyclerView quizList;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_quiz);
        quizList = findViewById(R.id.quizList);
        BottomNavigationView navView = findViewById(R.id.navigation);
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.navigation_home:
                        onBackPressed();
                        break;
                    case R.id.navigation_classes:
                        Utils.openMyPackages(DailyQuizActvity.this, 0);
                        break;
                    case R.id.navigation_test:
                        Utils.openMyPackages(DailyQuizActvity.this, 1);
                        break;
                    case R.id.navigation_profile:
                        Utils.openMyProfileNewTask(DailyQuizActvity.this);
                        break;
                }
                return false;
            }
        });
        getDailyQuiz();
    }

    private void getDailyQuiz(){
        JSONObject params = new JSONObject();
        try{
            params.put("StudentID", Utils.getStudentId(this));
            params.put("SpecializationID", Utils.getStudentSpecID(this));
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        ServerApi.callServerApi(this, ServerApi.BASE_URL, "QuizExam", params, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                JSONArray values = response.optJSONArray("Body");
                if(values == null || values.length() == 0){
                    findViewById(R.id.noQuiz).setVisibility(View.VISIBLE);
                }else {
                    setList(values);
                }
            }

            @Override
            public void error(String error) {
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setList(JSONArray values){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        quizList.setLayoutManager(linearLayoutManager);
        QuizAdapter quizAdapter = new QuizAdapter(this, values);
        quizList.setAdapter(quizAdapter);
    }
}
