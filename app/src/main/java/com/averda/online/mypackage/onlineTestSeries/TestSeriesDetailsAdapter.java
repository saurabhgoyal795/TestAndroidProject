package com.averda.online.mypackage.onlineTestSeries;

import android.app.Activity;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.averda.online.R;
import com.averda.online.mypackage.onlineTestSeries.test.TestActivity;
import com.averda.online.mypackage.onlineTestSeries.test.TestResultActivity;
import com.averda.online.preferences.Preferences;
import com.averda.online.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

public class TestSeriesDetailsAdapter extends RecyclerView.Adapter<TestSeriesDetailsAdapter.ViewHolder> {
    private JSONArray planItem;
    private Activity context;
    private DisplayMetrics metrics;


    public TestSeriesDetailsAdapter(JSONArray planItem, Activity context) {
        this.planItem = planItem;
        this.context = context;
        metrics = Utils.getMetrics(context);
    }

    @Override
    public TestSeriesDetailsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mytest_product_subject_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final TestSeriesDetailsAdapter.ViewHolder holder, final int position) {
        JSONObject item = planItem.optJSONObject(position);
        holder.examName.setText(item.optString("ExamName"));
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)holder.view.getLayoutParams();
        if(holder.getAdapterPosition() == planItem.length() - 1){
            params.bottomMargin = (int)(10*metrics.density);
        }else{
            params.bottomMargin = 0;
        }
        holder.view.setLayoutParams(params);
        int studentExamID = item.optInt("StudentExamID");
        int examStatus = item.optInt("ExamStatus");
        boolean requireRollNo = item.optBoolean("RequireRollNo");
        String status = "Upcoming";
        switch (examStatus){
            case 1:
                status = "Active";
                holder.text4.setTextColor(ContextCompat.getColor(context, R.color.ca_green));
                break;
            case 2:
                status = "Partial";
                holder.text4.setTextColor(ContextCompat.getColor(context, R.color.ca_yellow));
                break;
            case 3:
                status = "Completed";
                holder.text4.setTextColor(ContextCompat.getColor(context, R.color.ca_green));
                break;
            case 4:
                status = "Missed";
                holder.text4.setTextColor(ContextCompat.getColor(context, R.color.red));
                break;
        }
        if(examStatus == 0){
            holder.text4.setVisibility(View.GONE);
            if(item.optBoolean("ISDateLimit")){
                holder.text1.setText(item.optString("FromDate"));
                if(item.optBoolean("IsTimeBound")){
                    holder.text1.setText(item.optString("FromDate")+" "+item.optString("ExamTime"));
                }
                holder.text2.setText(item.optInt("TotalQuestion")+" Ques");
                holder.text2.setVisibility(View.VISIBLE);
                holder.text3.setText(item.optString("ExamDuration")+" Min");
            }else{
                holder.text3.setVisibility(View.VISIBLE);
                holder.text2.setVisibility(View.GONE);
                holder.text1.setText(item.optInt("TotalQuestion")+" Ques");
                holder.text3.setText(item.optString("ExamDuration")+" Min");
            }
        }else{
            holder.text2.setVisibility(View.GONE);
            if(Utils.isValidString(status)) {
                if(status.equalsIgnoreCase("active")) {
                    holder.text4.setText("Start Test");
                } else if (status.equalsIgnoreCase("completed")) {
                    holder.text4.setText("View Analysis");
                }else {
                    holder.text4.setText(status);
                }
                holder.text4.setVisibility(View.VISIBLE);
            }else{
                holder.text4.setVisibility(View.GONE);
            }
            holder.text3.setVisibility(View.VISIBLE);
            holder.text1.setText(item.optInt("TotalQuestion")+" Ques");
            holder.text3.setText(item.optString("ExamDuration")+" Min");
        }
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(1 == examStatus && requireRollNo && !Preferences.get(context, Preferences.KEY_IS_TEST_FORM_SUBMITTED, false)){
                    if(context instanceof TestSeriesDetailsActivity){
                        ((TestSeriesDetailsActivity)context).showDialog();
                        return;
                    }
                }
                if(3 == examStatus){
                    Intent intent = new Intent(context, TestResultActivity.class);
                    intent.putExtra("studentExamID", studentExamID);
                    intent.putExtra("title", item.optString("ExamName"));
                    intent.putExtra("examDuration", item.optInt("ExamDuration"));
                    context.startActivity(intent);
                }else if(1 == examStatus || 2 == examStatus){
                    Intent intent = new Intent(context, TestActivity.class);
                    intent.putExtra("item", item.toString());
                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return planItem.length();
    }

    public void refreshAdapter(JSONArray items) {
        planItem = items;
        notifyDataSetChanged();
    }
    protected class ViewHolder extends RecyclerView.ViewHolder {
        private TextView examName;
        private TextView text1;
        private TextView text2;
        private TextView text3;
        private TextView text4;
        private View view;
        public ViewHolder(View v) {
            super(v);
            view = v;
            examName = v.findViewById(R.id.examName);
            text1 = v.findViewById(R.id.text1);
            text2 = v.findViewById(R.id.text2);
            text3 = v.findViewById(R.id.text3);
            text4 = v.findViewById(R.id.text4);
        }
    }
}

