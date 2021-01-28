package com.averda.online.news;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.averda.online.R;
import com.averda.online.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private JSONArray planItem;
    private int rowLayout;
    private Activity context;
    private DisplayMetrics metrics;
    private int imageWidth;

    protected class NewsViewHolder extends RecyclerView.ViewHolder {
        private TextView planText;
        private TextView newsDate;
        private TextView viewMore;
        public NewsViewHolder(View v) {
            super(v);
            planText = v.findViewById(R.id.planText);
            newsDate = v.findViewById(R.id.newsDate);
            viewMore = v.findViewById(R.id.viewMore);
        }
    }

    public NewsAdapter(JSONArray planItem, int rowLayout, Activity context) {
        this.planItem = planItem;
        this.rowLayout = rowLayout;
        this.context = context;
        metrics = Utils.getMetrics(context);
        imageWidth = (metrics.widthPixels - (int)(32*metrics.density))/2;
    }

    @Override
    public NewsAdapter.NewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        view = LayoutInflater.from(parent.getContext()).inflate(rowLayout, parent, false);
        return new NewsAdapter.NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final NewsAdapter.NewsViewHolder holder, final int position) {
        final JSONObject item = planItem.optJSONObject(holder.getAdapterPosition());
        holder.planText.setText(planItem.optJSONObject(position).optString("NewsTitle"));
        holder.newsDate.setText("News Date: "+planItem.optJSONObject(position).optString("NewsDate"));
        SpannableString content = new SpannableString("View More");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        holder.viewMore.setText(content);
        if(position == planItem.length() - 1){
            holder.itemView.setPadding((int)(8 * metrics.density), (int)(16 * metrics.density), (int)(8 * metrics.density), (int)(16 * metrics.density));
        }else{
            holder.itemView.setPadding((int)(8 * metrics.density), (int)(16 * metrics.density), (int)(8 * metrics.density), 0);
        }
        if (!planItem.optJSONObject(position).has("URL")){
            holder.viewMore.setVisibility(View.GONE);
        } else {
            holder.viewMore.setVisibility(View.VISIBLE);
        }

        holder.viewMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    String url = planItem.optJSONObject(position).optString("URL");
                    if (!url.startsWith("http://") && !url.startsWith("https://"))
                        url = "https://" + url;
//                    if (url.contains(".pdf")){
//                        Intent intent = new Intent(context, PdfOpenActivity.class);
//                        intent.putExtra("fileName", url);
//                        context.startActivity(intent);
//                    } else {
//                        Intent browserIntent = new Intent(context, CommonWebViewActivity.class);
//                        browserIntent.putExtra("url", url);
//                        context.startActivity(browserIntent);
//                    }
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                context.startActivity(browserIntent);
                }catch (Exception e){
                    if(Utils.isDebugModeOn){
                        e.printStackTrace();
                    }
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
