package com.averda.online.testseries;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.averda.online.R;
import com.averda.online.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;

public class StatusItemAdapter extends RecyclerView.Adapter<StatusItemAdapter.StatusViewHolder> {

    private JSONArray planItem;
    private int rowLayout;
    private Activity context;
    private DisplayMetrics metrics;
    private int imageWidth;

    protected class StatusViewHolder extends RecyclerView.ViewHolder {
        private ImageView planImage;
        private ImageView location;
        private LinearLayout layout;
        private TextView planText;
        private TextView dateText;
        private TextView locationText;
        private TextView status;
        public StatusViewHolder(View v) {
            super(v);
            planImage = v.findViewById(R.id.planImage);
            location = v.findViewById(R.id.location);
            layout = v.findViewById(R.id.view);
            planText = v.findViewById(R.id.planText);
            dateText = v.findViewById(R.id.dateText);
            locationText = v.findViewById(R.id.locationText);
            status = v.findViewById(R.id.status);
        }
    }

    public StatusItemAdapter(JSONArray planItem, int rowLayout, Activity context) {
        this.planItem = planItem;
        this.rowLayout = rowLayout;
        this.context = context;
        metrics = Utils.getMetrics(context);
        imageWidth = (metrics.widthPixels - (int)(32*metrics.density))/2;
    }

    @Override
    public StatusItemAdapter.StatusViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        view = LayoutInflater.from(parent.getContext()).inflate(rowLayout, parent, false);
        return new StatusItemAdapter.StatusViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final StatusItemAdapter.StatusViewHolder holder, final int position) {
        final JSONObject item = planItem.optJSONObject(holder.getAdapterPosition());
        String imagePath = item.optString("admin_image");
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
        holder.planText.setText(planItem.optJSONObject(position).optString("status_text"));
        holder.dateText.setText(planItem.optJSONObject(position).optString("update_time"));
        if(position == planItem.length() - 1){
            holder.itemView.setPadding((int)(8 * metrics.density), (int)(16 * metrics.density), (int)(8 * metrics.density), (int)(16 * metrics.density));
        }else{
            holder.itemView.setPadding((int)(8 * metrics.density), (int)(16 * metrics.density), (int)(8 * metrics.density), 0);
        }
//        holder.dateText.setText(planItem.optJSONObject(position).optString("created_at").split("T")[0]);
//        holder.status.setText(planItem.optJSONObject(position).optString("status_text").toUpperCase());
        if(planItem.optJSONObject(position).optString("status_color").equals("green")){
            holder.layout.setBackgroundResource(R.drawable.green);
        }else if(planItem.optJSONObject(position).optString("status_color").equals("yellow")){
            holder.layout.setBackgroundResource(R.drawable.yellow);
        }else if(planItem.optJSONObject(position).optString("status_color").equals("red")){
            holder.layout.setBackgroundResource(R.drawable.red);
        }
        if(planItem.optJSONObject(position).optString("status_color").equals("green")){
            holder.layout.setBackgroundResource(R.drawable.green);
        }else if(planItem.optJSONObject(position).optString("status_color").equals("yellow")){
            holder.layout.setBackgroundResource(R.drawable.yellow);
        }else if(planItem.optJSONObject(position).optString("status_color").equals("blue")){
            holder.layout.setBackgroundResource(R.drawable.blue);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imagePath.contains("noimage.png")){

                }else{
                    showImage(imagePath);
                }

            }
        });
    }
    public void showImage(String url) {
        Dialog builder = new Dialog(context);
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
        WindowManager w = context.getWindowManager();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)    {
            w.getDefaultDisplay().getSize(size);
            Measuredwidth = size.x;
            Measuredheight = size.y;
        }else{
            Display d = w.getDefaultDisplay();
            Measuredwidth = d.getWidth();
            Measuredheight = d.getHeight();
        }
        ImageView imageView = new ImageView(context);
        Glide.with(context)
                .load(url)
                .error(R.drawable.camera)
                .override((int)(Measuredheight-100), (int)(Measuredwidth-20))
                .placeholder(R.drawable.camera)
                .into(imageView);


        builder.addContentView(imageView, new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        builder.show();
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
