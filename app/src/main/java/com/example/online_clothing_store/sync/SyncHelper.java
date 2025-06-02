package com.example.online_clothing_store.sync;

import android.content.Context;
import android.util.Log;
import com.example.online_clothing_store.api.ApiClient;
import com.example.online_clothing_store.api.ApiService;
import com.example.online_clothing_store.database.AppDatabase;
import com.example.online_clothing_store.database.entities.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;

public class SyncHelper {
    private final ApiService apiService;
    private final AppDatabase db;

    public SyncHelper(Context context) {
        apiService = ApiClient.getClient().create(ApiService.class);
        db = AppDatabase.getInstance(context);
    }
    // --- USERS ---
    public void syncUsers() {
        List<User> localUsers = db.userDao().getAllUsers();
        apiService.syncUsers(localUsers).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.d("Sync", "Users sent to server");
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("Sync", "Failed to sync users", t);
            }
        });
        apiService.getUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (User u : response.body()) db.userDao().insert(u);
                }
            }
            @Override public void onFailure(Call<List<User>> call, Throwable t) { /* ... */ }
        });
    }

    // --- FAVORITES ---
    public void syncFavorites(int userId) {
        List<Favorite> localFavorites = db.favoriteDao().getFavoritesByUserId(userId);
        apiService.syncFavorites(localFavorites).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.d("Sync", "Favorites sent to server");
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("Sync", "Failed to sync favorites", t);
            }
        });
        apiService.getFavorites(userId).enqueue(new Callback<List<Favorite>>() {
            @Override
            public void onResponse(Call<List<Favorite>> call, Response<List<Favorite>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Favorite f : response.body()) db.favoriteDao().insert(f);
                }
            }
            @Override public void onFailure(Call<List<Favorite>> call, Throwable t) { /* ... */ }
        });
    }

    // --- PRODUCTS ---
    public void syncProducts() {
        List<Product> localProducts = db.productDao().getAllProducts();
        apiService.syncProducts(localProducts).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.d("Sync", "Products sent to server");
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("Sync", "Failed to sync products", t);
            }
        });
        apiService.getProducts().enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    db.productDao().insertAll(response.body().toArray(new Product[0]));
                    Log.d("Sync", "Products updated from server");
                }
            }
            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Log.e("Sync", "Failed to fetch products", t);
            }
        });
    }

    // --- CART ---
    public void syncCart(int userId) {
        List<Cart> localCart = db.cartDao().getCartItemsByUserId(userId);
        apiService.syncCart(localCart).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.d("Sync", "Cart sent to server");
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("Sync", "Failed to sync cart", t);
            }
        });
        apiService.getCart(userId).enqueue(new Callback<List<Cart>>() {
            @Override
            public void onResponse(Call<List<Cart>> call, Response<List<Cart>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Можно сделать db.cartDao().deleteCartByUserId(userId); перед вставкой
                    for (Cart c : response.body()) {
                        db.cartDao().insert(c);
                    }
                    Log.d("Sync", "Cart updated from server");
                }
            }
            @Override
            public void onFailure(Call<List<Cart>> call, Throwable t) {
                Log.e("Sync", "Failed to fetch cart", t);
            }
        });
    }

    // --- ORDERS ---
    public void syncOrders(int userId) {
        List<Order> localOrders = db.orderDao().getOrdersByUserId(userId);
        apiService.syncOrders(localOrders).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.d("Sync", "Orders sent to server");
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("Sync", "Failed to sync orders", t);
            }
        });
        apiService.getOrders(userId).enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Order o : response.body()) {
                        db.orderDao().insert(o);
                    }
                    Log.d("Sync", "Orders updated from server");
                }
            }
            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                Log.e("Sync", "Failed to fetch orders", t);
            }
        });
    }

    // --- PROMOS ---
    public void syncPromos() {
        apiService.getPromos().enqueue(new Callback<List<Promo>>() {
            @Override
            public void onResponse(Call<List<Promo>> call, Response<List<Promo>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Promo p : response.body()) {
                        db.promoDao().insert(p);
                    }
                    Log.d("Sync", "Promos updated from server");
                }
            }
            @Override
            public void onFailure(Call<List<Promo>> call, Throwable t) {
                Log.e("Sync", "Failed to fetch promos", t);
            }
        });
    }
}