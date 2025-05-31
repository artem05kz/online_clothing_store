package com.example.online_clothing_store;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.example.online_clothing_store.database.AppDatabase;
import com.example.online_clothing_store.database.entities.Product;
import com.example.online_clothing_store.database.entities.ProductImage;

import java.util.ArrayList;
import java.util.List;

import me.relex.circleindicator.CircleIndicator3;

public class ProductDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Получаем данные о продукте
        Product product = (Product) getIntent().getSerializableExtra("product");

        // Настройка ViewPager для изображений
        ViewPager2 viewPager = findViewById(R.id.productImagesPager);
        ImagePagerAdapter adapter = new ImagePagerAdapter();
        viewPager.setAdapter(adapter);

        // Загрузка изображений в фоновом потоке
        loadProductImages(product, adapter, viewPager);

        // Заполнение данных
        TextView title = findViewById(R.id.productTitle);
        TextView price = findViewById(R.id.productPrice);
        TextView rating = findViewById(R.id.productRating);
        TextView description = findViewById(R.id.productDescription);
        TextView composition = findViewById(R.id.productComposition);

        title.setText(product.getName());
        price.setText(String.format("%.2f ₽", product.getPrice()));
        rating.setText(String.valueOf(product.getRating()));
        description.setText(product.getDescription());
        composition.setText(product.getComposition());

        // Кнопка "Добавить в корзину"
        Button addToCart = findViewById(R.id.addToCartButton);
        addToCart.setOnClickListener(v -> {
            // Логика добавления в корзину
        });
    }

    private void loadProductImages(Product product, ImagePagerAdapter adapter, ViewPager2 viewPager) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);

            // 1. Основное изображение из продукта
            if (product.getMainImageUrl() != null && !product.getMainImageUrl().isEmpty()) {
                adapter.addImage(product.getMainImageUrl());
            }

            // 2. Дополнительные изображения из таблицы product_images
            List<ProductImage> additionalImages = db.productImageDao().getImagesForProduct(product.getId());
            for (ProductImage image : additionalImages) {
                adapter.addImage(image.getImageUrl());
            }

            // 3. Если изображений нет, добавляем заглушку
            runOnUiThread(() -> {
                if (adapter.getItemCount() == 0) {
                    adapter.addImage(R.drawable.error);
                }

                // Настройка индикатора после загрузки изображений
                setupViewPagerIndicator(viewPager);
            });
        }).start();
    }

    private void setupViewPagerIndicator(ViewPager2 viewPager) {
        CircleIndicator3 indicator = new CircleIndicator3(this);
        ViewGroup indicatorContainer = findViewById(R.id.indicatorContainer);

        // Удаляем старый индикатор если есть
        if (indicatorContainer.getChildCount() > 0) {
            indicatorContainer.removeAllViews();
        }

        // Настраиваем новый индикатор
        indicatorContainer.addView(indicator);
        indicator.setViewPager(viewPager);
    }
}