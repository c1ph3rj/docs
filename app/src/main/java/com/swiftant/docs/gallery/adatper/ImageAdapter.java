package com.swiftant.docs.gallery.adatper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.swiftant.docs.R;
import com.swiftant.docs.fullscreenimgPkg.FullScreenImageView;

import java.io.File;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private final List<File> imageFiles;
    Activity context;
    public static File selectedFile;

    public ImageAdapter(List<File> imageFiles) {
        this.imageFiles = imageFiles;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        this.context = (Activity) context;
        View view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        File imageFile = imageFiles.get(position);
        // Load image using a library like Glide or Picasso for better performance
        // For simplicity, we'll use the BitmapFactory here
        holder.imageView.setImageBitmap(BitmapFactory.decodeFile(imageFile.getAbsolutePath()));

        holder.removeBtn.setOnClickListener(onClickRemove -> {
            if(imageFiles.get(position).delete()) {
                imageFiles.remove(position);
            }
            if(imageFiles.size() == 0){
                context.finish();
            }
            this.notifyDataSetChanged();
        });

//        holder.imageView.setOnClickListener(onClickImage -> {
//            context.startActivity(new Intent(context, FullScreenImageView.class));
//        });
    }

    @Override
    public int getItemCount() {
        return imageFiles.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        CardView removeBtn;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageThumbnail);
            removeBtn = itemView.findViewById(R.id.removeBtn);
        }
    }
}
