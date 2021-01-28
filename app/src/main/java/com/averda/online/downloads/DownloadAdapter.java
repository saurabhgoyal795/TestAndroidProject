package com.averda.online.downloads;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.averda.online.R;
import com.averda.online.server.ServerApi;
import com.averda.online.utils.PdfOpenActivity;
import com.averda.online.utils.Utils;


import java.io.File;

public class DownloadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    File[] values;
    Activity activity;
    public DownloadAdapter(Activity activity, File[] values){
        this.values = values;
        this.activity = activity;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.download_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder)holder;
        viewHolder.item = values[holder.getAdapterPosition()];
        viewHolder.title.setText(viewHolder.item.getName());
        viewHolder.share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                share(viewHolder.item);
            }
        });
        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, PdfOpenActivity.class);
                intent.putExtra("fileName", viewHolder.item.getName());
                intent.putExtra("downloadPath", ServerApi.TEST_SOLUTION_PATH);
                intent.putExtra("basePath", activity.getFilesDir() + "/SubjectPdf/");
                activity.startActivity(intent);
            }
        });
        viewHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String filePath = viewHolder.item.getAbsolutePath();
                new File(filePath).delete();
                ((MyDownloadActivity)activity).getDownloadFiles();
            }
        });
    }

    public void refreshValues(File[] values){
        this.values = values;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return values.length;
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public File item;
        public TextView title;
        public ImageView share;
        public ImageView delete;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            title = view.findViewById(R.id.title);
            share = view.findViewById(R.id.share);
            delete = view.findViewById(R.id.delete);
        }
    }

    private void share(File file){
        String emailChooserHeading = "Choose an option to share";
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        Uri uri = FileProvider.getUriForFile(activity, activity.getPackageName(), file);
        shareIntent.setType("application/pdf");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        try {
            activity.startActivity(Intent.createChooser(shareIntent, emailChooserHeading));
        } catch (Exception e) {
            if(Utils.isDebugModeOn) {
                e.printStackTrace();
            }
        }
    }
}
