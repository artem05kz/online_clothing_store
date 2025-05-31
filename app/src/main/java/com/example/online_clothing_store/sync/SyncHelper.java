package com.example.online_clothing_store.sync;

import android.content.Context;

import com.example.online_clothing_store.api.ApiClient;
import com.example.online_clothing_store.api.ApiService;
import com.example.online_clothing_store.database.AppDatabase;
import com.example.online_clothing_store.database.entities.Product;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SyncHelper {
    private final ApiService apiService;
    private final AppDatabase db;

    public SyncHelper(Context context) {
        apiService = ApiClient.getClient().create(ApiService.class);
        db = AppDatabase.getInstance(context);
    }

    public void syncProducts() {
        apiService.getProducts().enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Product[] productsArray = response.body().toArray(new Product[0]);
                    db.productDao().insertAll(productsArray);
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                // Обработка ошибки
            }
        });
    }

    public void fullSync() {
        syncProducts();
        // Добавьте синхронизацию других данных
    }
}