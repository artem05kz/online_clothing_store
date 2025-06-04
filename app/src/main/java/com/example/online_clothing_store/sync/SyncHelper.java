package com.example.online_clothing_store.sync;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
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
import java.util.ArrayList;

public class SyncHelper {
    private final ApiService apiService;
    private final AppDatabase db;
    private static final String TAG = "SyncHelper";

    public SyncHelper(Context context) {
        apiService = ApiClient.getClient().create(ApiService.class);
        db = AppDatabase.getInstance(context);
    }

    // --- USERS ---
    public void syncUsers() {
        Log.d(TAG, "Начало синхронизации пользователей");
        
        // Получаем пользователей с сервера
        apiService.getUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<User> users = response.body();
                    Log.d(TAG, "Получено пользователей с сервера: " + users.size());
                    
                    // Сохраняем пользователей в локальную БД
                    Executors.newSingleThreadExecutor().execute(() -> {
                        try {
                            db.userDao().deleteAll();
                            for (User user : users) {
                                db.userDao().insert(user);
                            }
                            Log.d(TAG, "Пользователи сохранены в локальную БД");
                        } catch (Exception e) {
                            Log.e(TAG, "Ошибка сохранения пользователей в БД", e);
                        }
                    });
                } else {
                    Log.e(TAG, "Ошибка получения пользователей с сервера: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Log.e(TAG, "Ошибка сети при получении пользователей", t);
            }
        });
    }

    // --- FAVORITES ---
    public void syncFavorites(int userId) {
        Log.d("SyncHelper", "Начало синхронизации избранного для пользователя " + userId);
        
        // Сначала получаем данные с сервера
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

        // Затем отправляем локальные данные на сервер
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
    }

    // --- PRODUCTS ---
    public void syncProducts() {
        // Сначала синхронизируем категории
        syncCategories();
        
        // Затем синхронизируем продукты
        apiService.getProducts().enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> products = response.body();
                    Log.d(TAG, "Получено продуктов с сервера: " + products.size());
                    
                    // Добавляем небольшую задержку для гарантии завершения синхронизации категорий
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        // Выполняем операции с БД в фоновом потоке
                        Executors.newSingleThreadExecutor().execute(() -> {
                            try {
                                db.productDao().insertAll(products.toArray(new Product[0]));
                                Log.d(TAG, "Продукты сохранены в локальную БД");
                            } catch (Exception e) {
                                Log.e(TAG, "Ошибка сохранения продуктов", e);
                            }
                        });
                    }, 500); // 500мс задержка
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Log.e(TAG, "Ошибка получения продуктов", t);
            }
        });
    }

    public void syncCategories() {
        Log.d(TAG, "Начало синхронизации категорий");
        apiService.getCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Category> categories = response.body();
                    Log.d(TAG, "Получено категорий с сервера: " + categories.size());
                    
                    // Сохраняем категории в локальную БД
                    Executors.newSingleThreadExecutor().execute(() -> {
                        try {
                            // Сначала удаляем все продукты
                            db.productDao().deleteAll();
                            // Затем удаляем все категории
                            db.categoryDao().deleteAll();
                            // Вставляем новые категории
                            db.categoryDao().insertAll(categories.toArray(new Category[0]));
                            Log.d(TAG, "Категории сохранены в локальную БД");
                        } catch (Exception e) {
                            Log.e(TAG, "Ошибка сохранения категорий", e);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Log.e(TAG, "Ошибка получения категорий", t);
            }
        });
    }

    // --- CART ---
    public void syncCart(int userId) {
        Log.d("SyncHelper", "Начало синхронизации корзины для пользователя " + userId);
        
        // Сначала получаем данные с сервера
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

        // Затем отправляем локальные данные на сервер
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Cart> localCartItems = db.cartDao().getCartItemsByUserId(userId);
            apiService.syncCart(localCartItems).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    Log.d("SyncHelper", "Локальные товары корзины отправлены на сервер");
                }
                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e("SyncHelper", "Ошибка отправки товаров корзины на сервер", t);
                }
            });
        });
    }

    // --- ORDERS ---
    public void syncOrders(int userId) {
        Log.d(TAG, "Начало синхронизации заказов для пользователя " + userId);
        
        // Сначала отправляем локальные данные на сервер
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<Order> localOrders = db.orderDao().getOrdersByUserId(userId);
                Log.d(TAG, "Локальные заказы для синхронизации: " + localOrders.size());

                if (!localOrders.isEmpty()) {
                    // Подготавливаем заказы для отправки
                    List<Order> ordersToSend = new ArrayList<>();
                    for (Order order : localOrders) {
                        // Создаем новый объект Order для отправки
                        Order orderToSend = new Order();
                        
                        // Получаем объект User
                        User user = db.userDao().getUserById(order.getUserId());
                        if (user == null) {
                            Log.e(TAG, "Пользователь не найден для заказа: " + order.getId());
                            continue;
                        }
                        
                        orderToSend.setUserId(order.getUserId());
                        orderToSend.setUser(user);
                        orderToSend.setAddress(order.getAddress());
                        orderToSend.setOrderDate(order.getOrderDate());
                        orderToSend.setStatus(order.getStatus());
                        orderToSend.setTotalAmount(order.getTotalAmount());
                        
                        // Получаем элементы заказа
                        List<OrderItem> orderItems = db.orderItemDao().getOrderItemsByOrderId(order.getId());
                        if (orderItems != null && !orderItems.isEmpty()) {
                            // Добавляем элементы заказа
                            for (OrderItem item : orderItems) {
                                orderToSend.addOrderItem(item);
                            }
                        }
                        
                        // Логируем данные заказа перед отправкой
                        Log.d(TAG, "Подготовка заказа для отправки: id=" + orderToSend.getId() + 
                            ", userId=" + orderToSend.getUserId() + 
                            ", address=" + orderToSend.getAddress() + 
                            ", date=" + orderToSend.getOrderDate() + 
                            ", status=" + orderToSend.getStatus() + 
                            ", items=" + (orderToSend.getOrderItems() != null ? orderToSend.getOrderItems().size() : 0));
                        
                        // Проверяем наличие всех необходимых полей
                        if (orderToSend.getUserId() <= 0 || 
                            orderToSend.getAddress() == null || 
                            orderToSend.getAddress().isEmpty() ||
                            orderToSend.getOrderDate() == null || 
                            orderToSend.getOrderDate().isEmpty() ||
                            orderToSend.getStatus() == null || 
                            orderToSend.getStatus().isEmpty() ||
                            orderToSend.getOrderItems() == null || 
                            orderToSend.getOrderItems().isEmpty()) {
                            Log.e(TAG, "Заказ не содержит всех необходимых полей: " + orderToSend);
                            continue;
                        }
                        
                        ordersToSend.add(orderToSend);
                    }

                    if (!ordersToSend.isEmpty()) {
                        try {
                            // Отправляем заказы на сервер
                            Response<Void> response = apiService.syncOrders(ordersToSend).execute();
                            if (response.isSuccessful()) {
                                Log.d(TAG, "Заказы успешно отправлены на сервер");
                            } else {
                                Log.e(TAG, "Ошибка отправки заказов на сервер: " + response.code());
                                if (response.errorBody() != null) {
                                    String errorBody = response.errorBody().string();
                                    Log.e(TAG, "Тело ошибки: " + errorBody);
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Ошибка при отправке заказов на сервер", e);
                        }
                    } else {
                        Log.e(TAG, "Нет заказов для отправки после проверки полей");
                    }
                }

                // Затем получаем обновленные данные с сервера
                fetchOrdersFromServer(userId);
            } catch (Exception e) {
                Log.e(TAG, "Ошибка синхронизации заказов", e);
            }
        });
    }

    private void fetchOrdersFromServer(int userId) {
        apiService.getOrders(userId).enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Order> orders = response.body();
                    Log.d(TAG, "Получено заказов с сервера: " + orders.size());
                    
                    // Сохраняем заказы в локальную БД
                    new Thread(() -> {
                        try {
                            // Сначала удаляем старые заказы и их элементы
                            db.orderItemDao().deleteAll();
                            db.orderDao().deleteByUserId(userId);
                            
                            // Проверяем существование пользователя
                            User user = db.userDao().getUserById(userId);
                            if (user == null) {
                                Log.e(TAG, "Пользователь не найден: " + userId);
                                return;
                            }
                            
                            // Сохраняем новые заказы
                            List<Order> ordersToInsert = new ArrayList<>();
                            for (Order order : orders) {
                                // Устанавливаем userId для заказа
                                order.setUserId(userId);
                                
                                // Устанавливаем значения по умолчанию для отсутствующих полей
                                if (order.getStatus() == null || order.getStatus().isEmpty()) {
                                    order.setStatus("В обработке");
                                }
                                if (order.getOrderDate() == null || order.getOrderDate().isEmpty()) {
                                    order.setOrderDate(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                        .format(new java.util.Date()));
                                }
                                
                                // Проверяем все продукты в заказе
                                boolean allProductsExist = true;
                                if (order.getOrderItems() != null) {
                                    for (OrderItem item : order.getOrderItems()) {
                                        Product product = db.productDao().getProductById(item.getProductId());
                                        if (product == null) {
                                            Log.e(TAG, "Продукт не найден: " + item.getProductId());
                                            allProductsExist = false;
                                            break;
                                        }
                                    }
                                }
                                
                                if (allProductsExist) {
                                    ordersToInsert.add(order);
                                } else {
                                    Log.e(TAG, "Пропуск заказа из-за отсутствующих продуктов");
                                }
                            }
                            
                            if (!ordersToInsert.isEmpty()) {
                                // Сохраняем все заказы одним запросом
                                db.orderDao().insertAll(ordersToInsert.toArray(new Order[0]));
                                
                                // Сохраняем элементы заказов
                                for (Order order : ordersToInsert) {
                                    if (order.getOrderItems() != null) {
                                        for (OrderItem item : order.getOrderItems()) {
                                            item.setOrderId(order.getId());
                                            db.orderItemDao().insert(item);
                                        }
                                    }
                                }
                            }
                            Log.d(TAG, "Заказы сохранены в локальную БД");
                        } catch (Exception e) {
                            Log.e(TAG, "Ошибка сохранения заказов", e);
                        }
                    }).start();
                }
            }
            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                Log.e(TAG, "Ошибка получения заказов", t);
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