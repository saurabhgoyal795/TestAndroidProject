package com.averda.online.mypackage.onlineClass;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.averda.online.R;
import com.averda.online.player.PlayerActivity;
import com.averda.online.player.VimeoPlayer;
import com.averda.online.player.YoutubePlayerActivity;
import com.averda.online.server.ServerApi;
import com.averda.online.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

public class ClassVideoListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    JSONArray values;
    Activity activity;
    int imageWidth;
    int imageHeight;
    public ClassVideoListAdapter(Activity activity, JSONArray values){
        this.values = values;
        this.activity = activity;
        DisplayMetrics metrics = Utils.getMetrics(activity);
        if(metrics != null){
            imageWidth = metrics.widthPixels - (int)(20*metrics.density);
            imageWidth = imageWidth/2;
            imageHeight = (int)((imageWidth*1f)/1.77f);
        }
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.video_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final ViewHolder viewHolder = (ViewHolder)holder;
        viewHolder.item = values.optJSONObject(holder.getAdapterPosition());
        viewHolder.title.setText(viewHolder.item.optString("VideoTitle"));
        if(viewHolder.item.optString("VideoDescription").trim().equalsIgnoreCase("")){
            viewHolder.description.setVisibility(View.GONE);
        } else {
            viewHolder.description.setVisibility(View.VISIBLE);
            viewHolder.description.setText(viewHolder.item.optString("VideoDescription"));
        }
        viewHolder.image.getLayoutParams().height = imageHeight;
        viewHolder.image.getLayoutParams().width = imageWidth;
        String videoUrl = viewHolder.item.optString("VideoURL");
        String imagePath;
        if(videoUrl.contains("youtube.com")) {
            videoUrl = videoUrl.replace("https://www.youtube.com/watch?v=", "");
            int index = videoUrl.indexOf("&");
            if(index >= 0){
                videoUrl = videoUrl.substring(0, index);
            }
            String imageName = videoUrl +"/0.jpg";
            imagePath = "https://img.youtube.com/vi/" + imageName;
        }else {
            imagePath = VimeoPlayer.getVideoImagePath(activity, videoUrl);
        }
        if(imagePath != null){
            Glide.with(activity)
                    .load(imagePath)
                    .override(imageWidth, imageHeight)
                    .into(viewHolder.image);
        }

        viewHolder.query.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, QueryActivity.class);
                intent.putExtra("videoUrl", viewHolder.item.optString("VideoURL"));
                intent.putExtra("id", MyPackageClassDetailsActivity.planId);
                intent.putExtra("topicId", viewHolder.item.optInt("TopicID"));
                intent.putExtra("title", viewHolder.item.optString("VideoTitle"));
                intent.putExtra("queryType", QueryActivity.VIDEO_QUERY);
                activity.startActivity(intent);
            }
        });
        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String videoUrl = viewHolder.item.optString("VideoURL");
                if(videoUrl.contains("youtube.com")){
                    if(videoUrl.contains("https://www.youtube.com/watch?v=")){
                        videoUrl = videoUrl.replace("https://www.youtube.com/watch?v=", "");
                    }else if(videoUrl.contains("https://www.youtube.com/embed/")){
                        videoUrl = videoUrl.replace("https://www.youtube.com/embed/", "");
                    }
                    int index = videoUrl.indexOf("&");
                    if(index > 0) {
                        videoUrl = videoUrl.substring(0, index);
                    }
                    Intent intent = new Intent(activity, YoutubePlayerActivity.class);
                    intent.putExtra("youtubeId", videoUrl);
                    activity.startActivity(intent);
                }else{
                    Intent intent = new Intent(activity, PlayerActivity.class);
                    intent.putExtra("videoUrl", videoUrl);
                    intent.putExtra("title", viewHolder.item.optString("VideoTitle"));
                    intent.putExtra("position", position);
                    if(Utils.isLollipop()){
                        viewHolder.image.setTransitionName("video_"+position);
                        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(activity, viewHolder.image, "video_"+position);
                        activity.startActivity(intent, options.toBundle());
                    }else {
                        activity.startActivity(intent);
                    }
                }
                savePlayVideoHistory(viewHolder.item.optInt("PackageID"), viewHolder.item.optInt("VideoID"));
            }
        });
        if(viewHolder.item.optBoolean("IsNew")){
            viewHolder.newVideos.setVisibility(View.VISIBLE);
        }else{
            viewHolder.newVideos.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return values.length();
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public JSONObject item;
        public TextView title;
        public TextView description;
        public ImageView image;
        private LinearLayout query;
        private TextView newVideos;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            title = view.findViewById(R.id.title);
            image = view.findViewById(R.id.image);
            query = view.findViewById(R.id.query);
            description = view.findViewById(R.id.description);
            newVideos = view.findViewById(R.id.newVideos);
        }
    }

    public void refreshValues(JSONArray values){
        this.values = values;
        DisplayMetrics metrics = Utils.getMetrics(activity);
        if(metrics != null){
            imageWidth = metrics.widthPixels - (int)(20*metrics.density);
            imageWidth = imageWidth/2;
            imageHeight = (int)((imageWidth*1f)/1.77f);
        }
        notifyDataSetChanged();
    }

    private void savePlayVideoHistory(int packageId, int videoId){
        JSONObject params = new JSONObject();
        try{
           params.put("StudentID", Utils.getStudentId(activity));
           params.put("PackageID", packageId);
           params.put("VideoID", videoId);
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        ServerApi.callServerApi(activity, ServerApi.BASE_URL, "SavePlayVideoHistory", params, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {

            }

            @Override
            public void error(String error) {

            }
        });
    }
}
