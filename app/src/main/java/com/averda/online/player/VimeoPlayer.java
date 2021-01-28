package com.averda.online.player;

import android.content.Context;

import com.vimeo.networking.Configuration;
import com.vimeo.networking.VimeoClient;
import com.vimeo.networking.callbacks.ModelCallback;
import com.vimeo.networking.model.Picture;
import com.vimeo.networking.model.PictureCollection;
import com.vimeo.networking.model.Video;
import com.vimeo.networking.model.VideoFile;
import com.vimeo.networking.model.error.VimeoError;
import com.averda.online.BuildConfig;
import com.averda.online.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class VimeoPlayer{
    public static HashMap<String, HashMap<String, Object>> videoMaps;
    public static HashMap<String, String> images = new HashMap<>();
    public interface FetchListener{
        void videoData(HashMap<String, Object> videoItem);
        void error(String error);
    }

    public static String getVideoImagePath(Context context, String videoId){
        try{
            HashMap<String, String> images = (HashMap<String, String>)Utils.getObject(context, "videoImages");
            if(images != null){
                return images.get(videoId);
            }
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void getVideo(Context context, String videoId, FetchListener fetchListener){
        if(videoMaps != null && videoMaps.containsKey(videoId)){
            if(fetchListener != null){
                fetchListener.videoData(videoMaps.get(videoId));
            }
            return;
        }
        String token = BuildConfig.ACCESS_TOKEN;
        Configuration.Builder configBuilder = new Configuration.Builder(token).setCacheDirectory(context.getCacheDir());
        VimeoClient.initialize(configBuilder.build());
        String uri = "/videos/"+videoId;
        VimeoClient.getInstance().fetchNetworkContent(uri, new ModelCallback<Video>(Video.class) {
            @Override
            public void success(Video video) {
                if(fetchListener != null && video != null){
                    fetchListener.videoData(getVideoData(context, video));
                }
            }

            @Override
            public void failure(VimeoError error) {
                if(fetchListener != null){
                    fetchListener.error(error.getMessage());
                }
            }
        });
    }

    private static HashMap<String, Object> getVideoData(Context context, Video video){
        if(videoMaps == null){
            videoMaps = new HashMap<>();
        }
        HashMap<String, Object> videoItem = new HashMap<>();
        String uri = video.uri;
        String videoId = uri.replace("/videos/", "");
        if(videoMaps.get(videoId) != null){
            return videoMaps.get(videoId);
        }
        HashMap<Integer, HashMap<String, Object>> qualityMaps =new HashMap<>();
        ArrayList<Integer> qualityList = new ArrayList<>();
        ArrayList<VideoFile> videoList = video.download;
        for (int i = 0 ; i < videoList.size() ; i++){
            VideoFile videoFile = videoList.get(i);
            int height = videoFile.getHeight();
            int width = videoFile.getWidth();
            String url = videoFile.getLink();
            HashMap<String, Object> item = new HashMap<>();
            item.put("url", url);
            item.put("width", width);
            item.put("height", height);
            qualityMaps.put(height, item);
            qualityList.add(height);
        }
        Collections.sort(qualityList, Collections.reverseOrder());
        videoItem.put("qualityList", qualityList);
        videoItem.put("qualityMaps", qualityMaps);
        PictureCollection pictureCollection = video.pictures;
        ArrayList<Picture> pictures = pictureCollection.sizes;
        if(pictures.size() > 0){
            Picture p;
            if(pictures.size() > 3){
                p = pictures.get(3);
            }else {
                p = pictures.get(pictures.size() - 1);
            }
            if(p != null){
                String thumbPath = p.linkWithPlayButton;
                videoItem.put("thumbPath", thumbPath);
                images.put(videoId, thumbPath);
                Utils.saveObject(context, images, "videoImages");
            }
        }
        videoMaps.put(videoId, videoItem);
        return videoItem;
    }
}
