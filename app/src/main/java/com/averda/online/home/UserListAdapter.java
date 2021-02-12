package com.averda.online.home;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.averda.online.login.NewUser;
import com.bumptech.glide.Glide;
import com.averda.online.R;
import com.averda.online.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserListViewHolder> {

    private JSONArray planItem;
    private int rowLayout;
    private Activity context;
    private DisplayMetrics metrics;
    private int imageWidth;

    protected class UserListViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout layout;
        private TextView userName;
        private TextView city,created;
        private TextView orgnization;
        public UserListViewHolder(View v) {
            super(v);
            layout = v.findViewById(R.id.view);
            userName = v.findViewById(R.id.userName);
            city = v.findViewById(R.id.city);
            orgnization = v.findViewById(R.id.orgnization);
            created = v.findViewById(R.id.created);
        }
    }

    public UserListAdapter(JSONArray planItem, int rowLayout, Activity context) {
        this.planItem = planItem;
        this.rowLayout = rowLayout;
        this.context = context;
        metrics = Utils.getMetrics(context);
        imageWidth = (metrics.widthPixels - (int)(32*metrics.density))/2;
    }

    @Override
    public UserListAdapter.UserListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        view = LayoutInflater.from(parent.getContext()).inflate(rowLayout, parent, false);
        return new UserListAdapter.UserListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final UserListAdapter.UserListViewHolder holder, final int position) {
        final JSONObject item = planItem.optJSONObject(holder.getAdapterPosition());
        String userName= planItem.optJSONObject(position).optString("first_name")+" "+planItem.optJSONObject(position).optString("last_name");
        holder.userName.setText(userName);
        if(position == planItem.length() - 1){
            holder.itemView.setPadding((int)(8 * metrics.density), (int)(16 * metrics.density), (int)(8 * metrics.density), (int)(16 * metrics.density));
        }else{
            holder.itemView.setPadding((int)(8 * metrics.density), (int)(16 * metrics.density), (int)(8 * metrics.density), 0);
        }
        holder.city.setText(planItem.optJSONObject(position).optString("city"));
        holder.created.setText(planItem.optJSONObject(position).optString("created_at"));
        holder.orgnization.setText(planItem.optJSONObject(position).optString("organization"));
        //   holder.layout.setBackgroundColor(Color.parseColor(planItem.optJSONObject(position).optString("status_color")));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Utils.isLollipop()){
                    Intent intent = new Intent(context, NewUser.class);
                    intent.putExtra("item", item.toString());
                    context.startActivity(intent);
                }else {
                    Intent intent = new Intent(context, NewUser.class);
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
