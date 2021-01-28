package com.averda.online.mypackage;

import android.app.Activity;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.averda.online.R;
import com.averda.online.mypackage.onlineClass.MyPackageClassDetailsActivity;
import com.averda.online.mypackage.onlineTestSeries.TestSeriesDetailsActivity;
import com.averda.online.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

public class MyPackageAdapter extends RecyclerView.Adapter<MyPackageAdapter.TestPlanViewHolder> {

    private JSONArray planItem;
    private int rowLayout;
    private Activity context;
    private DisplayMetrics metrics;
    private int imageWidth;

    protected class TestPlanViewHolder extends RecyclerView.ViewHolder {
        private ImageView planImage;
        private TextView planText;
        private TextView text1;
        private TextView text2;
        private TextView text3;
        private TextView text4;
        View view;
        public TestPlanViewHolder(View v) {
            super(v);
            view = v;
            planImage = v.findViewById(R.id.planImage);
            planText = v.findViewById(R.id.planText);
            text1 = v.findViewById(R.id.text1);
            text2 = v.findViewById(R.id.text2);
            text3 = v.findViewById(R.id.text3);
            text4 = v.findViewById(R.id.text4);
        }
    }

    public MyPackageAdapter(JSONArray planItem, int rowLayout, Activity context) {
        this.planItem = planItem;
        this.rowLayout = rowLayout;
        this.context = context;
        metrics = Utils.getMetrics(context);
        if(metrics != null){
            imageWidth = (metrics.widthPixels - (int)(32*metrics.density))/2;
        }
    }

    @Override
    public MyPackageAdapter.TestPlanViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        view = LayoutInflater.from(parent.getContext()).inflate(rowLayout, parent, false);
        return new MyPackageAdapter.TestPlanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyPackageAdapter.TestPlanViewHolder holder, final int position) {
        final JSONObject item = planItem.optJSONObject(holder.getAdapterPosition());
        holder.planText.setText(planItem.optJSONObject(position).optString("PlanName"));
        if (planItem.optJSONObject(position).optBoolean("IsFree")  || planItem.optJSONObject(position).optString("IsFreeText") =="false"){
            holder.text4.setText("Free");
            holder.text4.setVisibility(View.VISIBLE);
        } else {
            holder.text4.setText("Paid");
            holder.text4.setVisibility(View.INVISIBLE);
        }
        if (planItem.optJSONObject(position).optInt("TypeID") == 1){
            if(item.optInt("NewVideos") > 0){
                int newVideos = item.optInt("NewVideos");
                holder.text1.setText(newVideos + (newVideos > 1 ? " new videos" : " new video"));
            }else{
                holder.text1.setText("0 new video");
            }
//            holder.text2.setText(planItem.optJSONObject(position).optInt("TotalExam")+" Subject");
            holder.text3.setText(planItem.optJSONObject(position).optInt("Duration")+" Days left");
        }else{
            holder.text1.setTextColor(ContextCompat.getColor(context, R.color.ca_blue));
            holder.text1.setAlpha(.70f);
            holder.text1.setText("Total "+planItem.optJSONObject(position).optInt("TotalExam")+" Tests");
            if(Utils.isValidString(planItem.optJSONObject(position).optString("DurationText"))){
                holder.text3.setText(planItem.optJSONObject(position).optString("DurationText"));
            }else{
                holder.text3.setText("Expire till exam");
            }
//            holder.text2.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (planItem.optJSONObject(position).optInt("TypeID") == 1){
                    //Open Video Details Fragment
                    Intent intent = new Intent(context, MyPackageClassDetailsActivity.class);
                    intent.putExtra("item", item.toString());
                    context.startActivity(intent);
                } else {
                    Intent intent = new Intent(context, TestSeriesDetailsActivity.class);
                    intent.putExtra("item", item.toString());
                    context.startActivity(intent);
                }
//                Intent intent = new Intent(context, TestPackageDetailsActivity.class);
//                intent.putExtra("item", item.toString());
//                context.startActivity(intent);
            }
        });
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)holder.view.getLayoutParams();
        if(holder.getAdapterPosition() == planItem.length() - 1){
            params.bottomMargin = (int)(10*metrics.density);
        }else{
            params.bottomMargin = 0;
        }
        holder.view.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return planItem.length();
    }
    public void refreshAdapter(JSONArray items) {
        planItem = items;
        notifyDataSetChanged();
    }
}
