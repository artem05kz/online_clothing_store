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
        Log.d("SyncHelper", "Начало синхронизации избранного для пользователя " + userId);
        
        // Сначала отправляем локальные данные на сервер
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Favorite> localFavorites = db.favoriteDao().getFavoritesByUserId(userId);
            apiService.syncFavorites(localFavorites).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    Log.d("SyncHelper", "Локальные избранные товары отправлены на сервер");
                }
                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e("SyncHelper", "Ошибка отправки избранных товаров на сервер", t);
                }
            });
        });

        // Затем получаем данные с сервера
        apiService.getFavorites(userId).enqueue(new Callback<List<Favorite>>() {
            @Override
            public void onResponse(Call<List<Favorite>> call, Response<List<Favorite>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Favorite> favorites = response.body();
                    Log.d("SyncHelper", "Получено избранных товаров: " + favorites.size());
                    
                    Executors.newSingleThreadExecutor().execute(() -> {
                        try {
                            db.favoriteDao().deleteByUserId(userId);
                            if (!favorites.isEmpty()) {
                                db.favoriteDao().insertAll(favorites.toArray(new Favorite[0]));
                            }
                            Log.d("SyncHelper", "Избранное сохранено в локальную БД");
                        } catch (Exception e) {
                            Log.e("SyncHelper", "Ошибка сохранения избранного", e);
                        }
                    });
                }
            }
            @Override
            public void onFailure(Call<List<Favorite>> call, Throwable t) {
                Log.e("SyncHelper", "Ошибка получения избранного", t);
            }
        });
    }

    // --- PRODUCTS ---
    public void syncProducts() {
        Log.d("Sync", "Продукты отправлены на сервер");
        apiService.getProductsWithCategories().enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> products = response.body();
                    Log.d("Sync", "Получено продуктов с сервера: " + products.size());
                
                    for (Product product : products) {
                        if (product.getCategoryId() == null) {
                            Log.w("Sync", "Продукт без категории: id=" + product.getId() + 
                                ", name=" + product.getName());
                        }
                    }

                    Executors.newSingleThreadExecutor().execute(() -> {
                        try {
                            db.productDao().deleteAll();
                            db.productDao().insertAll(products.toArray(new Product[0]));
                            Log.d("Sync", "Продукты сохранены в локальную БД");
                        } catch (Exception e) {
                            Log.e("Sync", "Ошибка сохранения продуктов", e);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Log.e("Sync", "Ошибка получения продуктов", t);
            }
        });
    }

    // --- CART ---
    public void syncCart(int userId) {
        Log.d("SyncHelper", "Начало синхронизации корзины для пользователя " + userId);
        apiService.getCart(userId).enqueue(new Callback<List<Cart>>() {
            @Override
            public void onResponse(Call<List<Cart>> call, Response<List<Cart>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Cart> cartItems = response.body();
                    Log.d("SyncHelper", "Получено товаров в корзине: " + cartItems.size());
                    
                    Executors.newSingleThreadExecutor().execute(() -> {
                        try {
                            db.cartDao().deleteCartByUserId(userId);
                            if (!cartItems.isEmpty()) {
                                db.cartDao().insertAll(cartItems.toArray(new Cart[0]));
                            }
                            Log.d("SyncHelper", "Корзина сохранена в локальную БД");
                        } catch (Exception e) {
                            Log.e("SyncHelper", "Ошибка сохранения корзины", e);
                        }
                    });
                }
            }
            @Override
            public void onFailure(Call<List<Cart>> call, Throwable t) {
                Log.e("SyncHelper", "Ошибка получения корзины", t);
            }
        });
    }

    // --- ORDERS ---
    public void syncOrders(int userId) {
        Log.d("SyncHelper", "Начало синхронизации заказов для пользователя " + userId);
        apiService.getOrders(userId).enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Order> orders = response.body();
                    Log.d("SyncHelper", "Получено заказов: " + orders.size());
                    
                    Executors.newSingleThreadExecutor().execute(() -> {
                        try {
                            db.orderDao().deleteByUserId(userId);
                            if (!orders.isEmpty()) {
                                db.orderDao().insertAll(orders.toArray(new Order[0]));
                            }
                            Log.d("SyncHelper", "Заказы сохранены в локальную БД");
                        } catch (Exception e) {
                            Log.e("SyncHelper", "Ошибка сохранения заказов", e);
                        }
                    });
                }
            }
            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                Log.e("SyncHelper", "Ошибка получения заказов", t);
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

    // --- CATEGORIES ---
    public void syncCategories() {
        Log.d("SyncHelper", "Начало синхронизации категорий");
        apiService.getCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Category> categories = response.body();
                    Log.d("SyncHelper", "Получено категорий с сервера: " + categories.size());
                    
                    Executors.newSingleThreadExecutor().execute(() -> {
                        try {
                            db.productDao().deleteAll();
                            db.categoryDao().deleteAll();
                            db.categoryDao().insertAll(categories.toArray(new Category[0]));
                            Log.d("SyncHelper", "Категории сохранены в локальную БД");
                        } catch (Exception e) {
                            Log.e("SyncHelper", "Ошибка сохранения категорий", e);
                        }
                    });
                } else {
                    Log.e("SyncHelper", "Ошибка получения категорий: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Log.e("SyncHelper", "Ошибка получения категорий", t);
            }
        });
    }
}