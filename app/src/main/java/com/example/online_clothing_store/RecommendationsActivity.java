package com.example.online_clothing_store;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import com.example.online_clothing_store.Adapter.BannerPagerAdapter;
import com.example.online_clothing_store.Adapter.ProductAdapter;
import com.example.online_clothing_store.Adapter.RecommendationsAdapter;
import com.example.online_clothing_store.database.AppDatabase;
import com.example.online_clothing_store.database.dao.ProductDao;
import com.example.online_clothing_store.database.dao.PromoDao;
import com.example.online_clothing_store.database.entities.Product;
import com.example.online_clothing_store.database.entities.Promo;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.widget.ImageView;
import android.content.ClipData;
import android.content.ClipboardManager;
import com.squareup.picasso.Picasso;
import com.example.online_clothing_store.sync.SyncHelper;
import android.app.ProgressDialog;
import java.util.ArrayList;

public class RecommendationsActivity extends AppCompatActivity {
    private static final String TAG = "RecommendationsActivity";

    private RecyclerView newArrivalsList;
    private RecyclerView recommendationsList;
    private TextView tvWelcome;
    private int currentUserId = -1;
    private boolean isGuestMode = false;
    private ProductAdapter newArrivalsAdapter;
    private RecommendationsAdapter recommendedAdapter;
    private ExecutorService executor;
    private ViewPager bannerPager;
    private BannerPagerAdapter bannerAdapter;
    private Promo currentPromo;
    private boolean isLoadingProducts = false;
    private boolean isLoadingBanners = false;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendations);

        // Инициализация ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Загрузка данных...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Получаем ID пользователя
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        currentUserId = prefs.getInt("user_id", -1);
        isGuestMode = getIntent().getBooleanExtra("is_guest", false);

        // Настройка Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Инициализация элементов
        tvWelcome = findViewById(R.id.tvWelcome);
        newArrivalsList = findViewById(R.id.newArrivalsList);
        recommendationsList = findViewById(R.id.recommendationsList);
        bannerPager = findViewById(R.id.bannerPager);

        // Настройка RecyclerView
        newArrivalsList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recommendationsList.setLayoutManager(new GridLayoutManager(this, 2));

        // Инициализация пула потоков
        executor = Executors.newFixedThreadPool(2);

        // Загрузка данных
        loadPromoBanners();
        loadProducts();

        // Настройка кнопок навигации
        setupMenuButtons();
    }

    private void loadPromoBanners() {
        if (isLoadingBanners) return;
        isLoadingBanners = true;

        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(this);
                PromoDao promoDao = db.promoDao();
                List<Promo> promos = promoDao.getAllPromos();
                
                Log.d(TAG, "Загружено промо-баннеров: " + promos.size());
                for (Promo promo : promos) {
                    Log.d(TAG, "Промо: id=" + promo.id + 
                        ", code=" + promo.code + 
                        ", imageUrl=" + promo.imageUrl + 
                        ", isActive=" + promo.isActive);
                }

                runOnUiThread(() -> {
                    if (!promos.isEmpty()) {
                        bannerAdapter = new BannerPagerAdapter(this, promos);
                        bannerPager.setAdapter(bannerAdapter);
                        bannerPager.setVisibility(View.VISIBLE);
                        Log.d(TAG, "Баннеры установлены в ViewPager");
                    } else {
                        bannerPager.setVisibility(View.GONE);
                        Log.d(TAG, "Нет активных промо-баннеров");
                    }
                    isLoadingBanners = false;
                });
            } catch (Exception e) {
                Log.e(TAG, "Ошибка загрузки промо-баннеров", e);
                runOnUiThread(() -> {
                    bannerPager.setVisibility(View.GONE);
                    isLoadingBanners = false;
                });
            }
        });
    }

    private void loadProducts() {
        Log.d(TAG, "Начало загрузки продуктов");
        
        // Проверяем, завершена ли синхронизация
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                
                // Загружаем новые поступления
                AppDatabase db = AppDatabase.getInstance(this);
                List<Product> newArrivals = db.productDao().getNewArrivals();
                Log.d(TAG, "Загружено новых поступлений: " + newArrivals.size());
                
                // Загружаем рекомендуемые товары
                List<Product> recommended = db.productDao().getRecommendedProducts();
                Log.d(TAG, "Загружено рекомендуемых товаров: " + recommended.size());
                
                // Если данные не загрузились, пробуем еще раз через 1 секунду
                if (newArrivals.isEmpty() && recommended.isEmpty()) {
                    Thread.sleep(1000);
                    newArrivals = db.productDao().getNewArrivals();
                    recommended = db.productDao().getRecommendedProducts();
                    Log.d(TAG, "Повторная попытка загрузки - новых поступлений: " + newArrivals.size() + 
                          ", рекомендуемых товаров: " + recommended.size());
                }
                
                // Создаем финальные копии для использования в лямбда-выражении
                final List<Product> finalNewArrivals = new ArrayList<>(newArrivals);
                final List<Product> finalRecommended = new ArrayList<>(recommended);
                
                runOnUiThread(() -> {
                    newArrivalsAdapter = new ProductAdapter(this, finalNewArrivals, isGuestMode, currentUserId, true);
                    newArrivalsList.setAdapter(newArrivalsAdapter);
                    
                    recommendedAdapter = new RecommendationsAdapter(this, finalRecommended, isGuestMode, currentUserId);
                    recommendationsList.setAdapter(recommendedAdapter);
                    
                    // Скрываем ProgressDialog после загрузки всех данных
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Ошибка загрузки продуктов", e);
                runOnUiThread(() -> {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    Toast.makeText(this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isLoadingProducts) {
            loadProducts();
        }
        if (!isLoadingBanners) {
            loadPromoBanners();
        }
    }

    private void setupMenuButtons() {
        findViewById(R.id.imageButtonHome).setOnClickListener(v -> {
            // Уже на главной - обновляем страницу
            if (!isLoadingProducts) {
            loadProducts();
            }
            if (!isLoadingBanners) {
                loadPromoBanners();
            }
        });

        findViewById(R.id.imageButtonHanger).setOnClickListener(v -> {
            startActivity(new Intent(this, CatalogActivity.class));
        });

        findViewById(R.id.imageButtonWardrobe).setOnClickListener(v -> {
            if (isGuestMode) {
                Toast.makeText(this, "Для использования этой функции требуется регистрация", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
            } else {
                startActivity(new Intent(this, WardrobeActivity.class));
            }
        });

        findViewById(R.id.imageButtonCart).setOnClickListener(v -> {
            if (isGuestMode) {
                Toast.makeText(this, "Для использования этой функции требуется регистрация", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
            } else {
                startActivity(new Intent(this, CartActivity.class));
            }
        });

        findViewById(R.id.imageButtonProfile).setOnClickListener(v -> {
            if (isGuestMode) {
                Toast.makeText(this, "Для использования этой функции требуется регистрация", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
            } else {
                startActivity(new Intent(this, ProfileActivity.class));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (newArrivalsAdapter != null) {
            newArrivalsAdapter.shutdownExecutor();
        }
        if (recommendedAdapter != null) {
            recommendedAdapter.shutdownExecutor();
        }
        if (executor != null) {
            executor.shutdown();
        }
    }
}