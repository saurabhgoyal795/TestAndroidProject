package com.averda.online.testseries;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.averda.online.R;
import com.averda.online.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

public class TestSeriesAdapter extends RecyclerView.Adapter<TestSeriesAdapter.TestPlanViewHolder> {

    private JSONArray planItem;
    private int rowLayout;
    private Activity context;
    private DisplayMetrics metrics;
    private int imageWidth;

    protected class TestPlanViewHolder extends RecyclerView.ViewHolder {
        private ImageView planImage;
        private LinearLayout layout;
        private TextView planText;
        private TextView dateText;
        private TextView locationText;
        private TextView status;
        public TestPlanViewHolder(View v) {
            super(v);
            planImage = v.findViewById(R.id.planImage);
            layout = v.findViewById(R.id.view);
            planText = v.findViewById(R.id.planText);
            dateText = v.findViewById(R.id.dateText);
            locationText = v.findViewById(R.id.locationText);
            status = v.findViewById(R.id.status);
        }
    }

    public TestSeriesAdapter(JSONArray planItem, int rowLayout, Activity context) {
        this.planItem = planItem;
        this.rowLayout = rowLayout;
        this.context = context;
        metrics = Utils.getMetrics(context);
        imageWidth = (metrics.widthPixels - (int)(32*metrics.density))/2;
    }

    @Override
    public TestSeriesAdapter.TestPlanViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        view = LayoutInflater.from(parent.getContext()).inflate(rowLayout, parent, false);
        return new TestSeriesAdapter.TestPlanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final TestSeriesAdapter.TestPlanViewHolder holder, final int position) {
        final JSONObject item = planItem.optJSONObject(holder.getAdapterPosition());
        String imagePath = item.optString("image");
        if(Utils.isValidString(imagePath)) {
            if(Utils.isActivityDestroyed(context)){
                return;
            }
            Glide.with(context)
                    .load(imagePath)
                    .override(imageWidth, (int)(80*metrics.density))
                    .error(R.drawable.waste_material)
                    .placeholder(R.drawable.waste_material)
                    .into(holder.planImage);
        }else{
            if(Utils.isActivityDestroyed(context)){
                return;
            }
            Glide.with(context)
                    .clear(holder.planImage);
        }
        int id= planItem.optJSONObject(position).optInt("id");
        String idVal = ""+id;
        if (id < 10) {
            idVal = "00"+id;
        } else if (id < 100) {
            idVal = "0"+id;
        }
        holder.planText.setText("ID : "+ idVal);
        if (planItem.optJSONObject(position).has("location")) {
            holder.locationText.setText(planItem.optJSONObject(position).optString("location").toUpperCase());
        } else {
            holder.locationText.setText(planItem.optJSONObject(position).optString("city"));
        }
        if(position == planItem.length() - 1){
            holder.itemView.setPadding((int)(8 * metrics.density), (int)(16 * metrics.density), (int)(8 * metrics.density), (int)(16 * metrics.density));
        }else{
            holder.itemView.setPadding((int)(8 * metrics.density), (int)(16 * metrics.density), (int)(8 * metrics.density), 0);
        }
        holder.dateText.setText(planItem.optJSONObject(position).optString("created_at").split("T")[0]);
        holder.status.setText(planItem.optJSONObject(position).optString("status_text").toUpperCase());
          if(planItem.optJSONObject(position).optString("status_color").equals("green")){
             holder.layout.setBackgroundResource(R.drawable.green);
          }else if(planItem.optJSONObject(position).optString("status_color").equals("yellow")){
              holder.layout.setBackgroundResource(R.drawable.yellow);
          }else if(planItem.optJSONObject(position).optString("status_color").equals("red")){
              holder.layout.setBackgroundResource(R.drawable.red);
          }
     //   holder.layout.setBackgroundColor(Color.parseColor(planItem.optJSONObject(position).optString("status_color")));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Utils.isLollipop()){
                    Intent intent = new Intent(context, TestPackageDetailsActivity.class);
                    intent.putExtra("item", item.toString());
                    intent.putExtra("position", position);
                    holder.planImage.setTransitionName("test_"+position);
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(context, holder.planImage, "test_"+position);
                    context.startActivity(intent, options.toBundle());
                }else {
                    Intent intent = new Intent(context, TestPackageDetailsActivity.class);
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
}
