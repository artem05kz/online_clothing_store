package com.example.online_clothing_store.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import com.example.online_clothing_store.R;
import com.example.online_clothing_store.database.entities.Promo;
import com.squareup.picasso.Picasso;
import java.util.List;

public class BannerPagerAdapter extends PagerAdapter {
    private final List<Promo> promos;
    private final Context context;

    public BannerPagerAdapter(Context context, List<Promo> promos) {
        this.context = context;
        this.promos = promos;
    }

    @Override
    public int getCount() {
        return promos.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_banner, container, false);
        ImageView imageView = view.findViewById(R.id.bannerImage);
        
        Promo promo = promos.get(position);
        Log.d("BannerPagerAdapter", "Загрузка баннера: id=" + promo.id + 
            ", code=" + promo.code + 
            ", imageUrl=" + promo.imageUrl);
            
        if (promo.imageUrl != null && !promo.imageUrl.isEmpty()) {
            Picasso.get()
                    .load(promo.imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.error)
                    .into(imageView);
            Log.d("BannerPagerAdapter", "Изображение загружается: " + promo.imageUrl);
        } else {
            imageView.setImageResource(R.drawable.error);
            Log.d("BannerPagerAdapter", "URL изображения пустой, установлен placeholder");
        }

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
} 