package com.example.online_clothing_store;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

public class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder> {
    private final List<Object> images = new ArrayList<>();

    public void addImage(String imageUrl) {
        images.add(imageUrl);
        notifyItemInserted(images.size() - 1);
    }

    public void addImage(int imageResId) {
        images.add(imageResId);
        notifyItemInserted(images.size() - 1);
    }

    public void setImages(List<String> urls) {
        images.clear();
        images.addAll(urls);
        notifyDataSetChanged();
    }

    public void clearImages() {
        images.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Object image = images.get(position);

        if (image instanceof String) {
            Picasso.get()
                    .load((String) image)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.error)
                    .into(holder.imageView);
        } else if (image instanceof Integer) {
            holder.imageView.setImageResource((Integer) image);
        }
    }
    @Override
    public int getItemCount() {
        return images.size();
    }
    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}