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
import java.util.concurrent.Executors;

public class SyncHelper {
    private final ApiService apiService;
    private final AppDatabase db;

    public SyncHelper(Context context) {
        apiService = ApiClient.getClient().create(ApiService.class);
        db = AppDatabase.getInstance(context);
    }

    // --- USERS ---
    public void syncUsers() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<User> localUsers = db.userDao().getAllUsers();
            apiService.syncUsers(localUsers).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    Log.d("Sync", "Пользователи отправлены на сервер");
                }
                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e("Sync", "Не удалось синхронизировать пользователей", t);
                }
            });
        });
        apiService.getUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        db.userDao().deleteAll();
                        db.userDao().insertAll(response.body().toArray(new User[0]));
                    });
                }
            }
            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Log.e("Sync", "Не удалось получить пользователей", t);
            }
        });
    }

    // --- FAVORITES ---
    public void syncFavorites(int userId) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Favorite> localFavorites = db.favoriteDao().getFavoritesByUserId(userId);
            apiService.syncFavorites(localFavorites).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    Log.d("Sync", "Избранное отправлено на сервер");
                }
                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e("Sync", "Не удалось синхронизировать избранное", t);
                }
            });
        });
        apiService.getFavorites(userId).enqueue(new Callback<List<Favorite>>() {
            @Override
            public void onResponse(Call<List<Favorite>> call, Response<List<Favorite>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        db.favoriteDao().deleteByUserId(userId);
                        db.favoriteDao().insertAll(response.body().toArray(new Favorite[0]));
                    });
                }
            }
            @Override
            public void onFailure(Call<List<Favorite>> call, Throwable t) {
                Log.e("Sync", "Не удалось получить избранное", t);
            }
        });
    }

    // --- PRODUCTS ---
    public void syncProducts() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Product> localProducts = db.productDao().getAllProducts();
            apiService.syncProducts(localProducts).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    Log.d("Sync", "Продукты отправлены на сервер");
                }
                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e("Sync", "Не удалось синхронизировать продукты", t);
                }
            });
        });
        apiService.getProducts().enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        db.productDao().deleteAll();
                        db.productDao().insertAll(response.body().toArray(new Product[0]));
                    });
                }
            }
            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Log.e("Sync", "Не удалось получить продукты", t);
            }
        });
    }

    // --- CART ---
    public void syncCart(int userId) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Cart> localCart = db.cartDao().getCartItemsByUserId(userId);
            apiService.syncCart(localCart).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    Log.d("Sync", "Корзина отправлена на сервер");
                }
                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e("Sync", "Не удалось синхронизировать корзину", t);
                }
            });
        });
        apiService.getCart(userId).enqueue(new Callback<List<Cart>>() {
            @Override
            public void onResponse(Call<List<Cart>> call, Response<List<Cart>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        db.cartDao().deleteCartByUserId(userId);
                        db.cartDao().insertAll(response.body().toArray(new Cart[0]));
                    });
                }
            }
            @Override
            public void onFailure(Call<List<Cart>> call, Throwable t) {
                Log.e("Sync", "Не удалось получить корзину", t);
            }
        });
    }

    // --- ORDERS ---
    public void syncOrders(int userId) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Order> localOrders = db.orderDao().getOrdersByUserId(userId);
            apiService.syncOrders(localOrders).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    Log.d("Sync", "Заказы отправлены на сервер");
                }
                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e("Sync", "Не удалось синхронизировать заказы", t);
                }
            });
        });
        apiService.getOrders(userId).enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        db.orderDao().deleteByUserId(userId);
                        db.orderDao().insertAll(response.body().toArray(new Order[0]));
                    });
                }
            }
            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                Log.e("Sync", "Не удалось получить заказы", t);
            }
        });
    }

    // --- PROMOS ---
    public void syncPromos() {
        Log.d("SyncHelper", "Начало синхронизации промо-акций");
        apiService.getPromos().enqueue(new Callback<List<Promo>>() {
            @Override
            public void onResponse(Call<List<Promo>> call, Response<List<Promo>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Promo> promos = response.body();
                    Log.d("SyncHelper", "Получено промо-акций с сервера: " + promos.size());
                    
                    // Устанавливаем isActive для всех промо-акций
                    for (Promo promo : promos) {
                        if (promo.isActive == null) {
                            promo.isActive = true;
                        }
                        Log.d("SyncHelper", "Промо: id=" + promo.id + 
                            ", code=" + promo.code + 
                            ", imageUrl=" + promo.imageUrl + 
                            ", isActive=" + promo.isActive);
                    }
                    
                    // Выполняем операции с БД в фоновом потоке
                    Executors.newSingleThreadExecutor().execute(() -> {
                        try {
                            db.promoDao().deleteAll();
                            db.promoDao().insertAll(promos.toArray(new Promo[0]));
                            Log.d("SyncHelper", "Промо-акции синхронизированы с локальной БД");
                        } catch (Exception e) {
                            Log.e("SyncHelper", "Ошибка при работе с БД", e);
                        }
                    });
                } else {
                    Log.e("SyncHelper", "Ошибка получения промо-акций: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Promo>> call, Throwable t) {
                Log.e("SyncHelper", "Ошибка синхронизации промо-акций", t);
            }
        });
    }
}