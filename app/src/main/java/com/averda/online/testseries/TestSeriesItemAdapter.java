package com.averda.online.testseries;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.averda.online.R;
import com.averda.online.utils.Utils;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TestSeriesItemAdapter extends RecyclerView.Adapter<TestSeriesItemAdapter.TestPlanViewHolder> {

    private JSONArray planItem;
    private int rowLayout;
    private Activity context;
    private DisplayMetrics metrics;
    private int imageWidth;

    protected class TestPlanViewHolder extends RecyclerView.ViewHolder {
        private TextView planText;
        private TextView textData;
        private CheckBox checkBox;
        public TestPlanViewHolder(View v) {
            super(v);
            planText = v.findViewById(R.id.planText);
            textData = v.findViewById(R.id.textData);
            checkBox = v.findViewById(R.id.checkBox);
        }
    }

    public TestSeriesItemAdapter(JSONArray planItem, int rowLayout, Activity context) {
        this.planItem = planItem;
        this.rowLayout = rowLayout;
        this.context = context;
        metrics = Utils.getMetrics(context);
        imageWidth = (metrics.widthPixels - (int)(32*metrics.density))/2;
    }

    @Override
    public TestSeriesItemAdapter.TestPlanViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        view = LayoutInflater.from(parent.getContext()).inflate(rowLayout, parent, false);
        return new TestSeriesItemAdapter.TestPlanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final TestSeriesItemAdapter.TestPlanViewHolder holder, final int position) {
        final JSONObject item = planItem.optJSONObject(holder.getAdapterPosition());
        String id= planItem.optJSONObject(position).optString("text_id");
        holder.planText.setText( id+" : ");
        holder.textData.setText( planItem.optJSONObject(position).optString("title"));
        if(position == planItem.length() - 1){
            holder.itemView.setPadding((int)(8 * metrics.density), (int)(16 * metrics.density), (int)(8 * metrics.density), (int)(16 * metrics.density));
        }else{
            holder.itemView.setPadding((int)(8 * metrics.density), (int)(16 * metrics.density), (int)(8 * metrics.density), 0);
        }
        if (planItem.optJSONObject(position).optInt("is_checked") == 1) {
            holder.checkBox.setChecked(true);
            holder.checkBox.setButtonDrawable(R.drawable.checkboxselect);
        } else {
            holder.checkBox.setChecked(false);
            holder.checkBox.setButtonDrawable(R.drawable.checkbox);
        }
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
             public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                JSONObject itemObj = planItem.optJSONObject(position);
                if (isChecked ) {
                    try {
                        itemObj.put("is_checked", 1);
                        holder.checkBox.setButtonDrawable(R.drawable.checkboxselect);
                        planItem.put(position, itemObj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        itemObj.put("is_checked", 0);
                        holder.checkBox.setButtonDrawable(R.drawable.checkbox);
                        planItem.put(position, itemObj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
        );
    }

    @Override
    public int getItemCount() {
        return planItem.length();
    }
    public void refreshAdapter(JSONArray items) {
        planItem = items;
        notifyDataSetChanged();
    }

    public String getItemCheckedList() {
        String questionText = "";
        for (int p = 0 ; p<planItem.length(); p++) {
            JSONObject itemObj = planItem.optJSONObject(p);
            if (itemObj.optInt("is_checked") == 1) {
                questionText = questionText + itemObj.optInt("id")+",";
            }
        }
        return  questionText;
    }
}
