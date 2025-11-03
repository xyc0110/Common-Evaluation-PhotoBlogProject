package com.example.photoviewer;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.OutputStream;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private List<Bitmap> bitmapList;
    private Context context;

    public ImageAdapter(Context context, List<Bitmap> bitmapList) {
        this.context = context;
        this.bitmapList = bitmapList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Bitmap bitmap = bitmapList.get(position);
        holder.imageView.setImageBitmap(bitmap);

        //  点击放大功能
        holder.imageView.setOnClickListener(v -> showFullScreenImage(bitmap));

        //  点击保存按钮：将图片保存到系统相册
        holder.btnSave.setOnClickListener(v -> {
            saveImageToGallery(bitmap);
            Toast.makeText(context, "이미지가 갤러리에 저장되었습니다!", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return bitmapList.size();
    }

    public void clearImages() {
        bitmapList.clear();
        notifyDataSetChanged();
    }

    public void setImages(List<Bitmap> newImages) {
        bitmapList.clear();
        bitmapList.addAll(newImages);
        notifyDataSetChanged();
    }

    //  全屏查看
    private void showFullScreenImage(Bitmap image) {
        Dialog dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_fullscreen_image);

        ImageView fullImageView = dialog.findViewById(R.id.fullscreenImageView);
        fullImageView.setImageBitmap(image);
        fullImageView.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    //  保存图片到相册
    private void saveImageToGallery(Bitmap bitmap) {
        try {
            ContentResolver resolver = context.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, "PhotoViewer_" + System.currentTimeMillis() + ".jpg");
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/PhotoViewer");
            Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (uri != null) {
                OutputStream out = resolver.openOutputStream(uri);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "저장 실패!", Toast.LENGTH_SHORT).show();
        }
    }

    //  ViewHolder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        Button btnSave;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewItem);
            btnSave = itemView.findViewById(R.id.btnSave);
        }
    }
}
