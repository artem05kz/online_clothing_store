package com.example.online_clothing_store.sync;

import android.content.Context;
import android.util.Log;
import com.example.online_clothing_store.database.AppDatabase;
import com.example.online_clothing_store.database.entities.*;
import com.example.online_clothing_store.utils.NetworkUtils;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class SyncManager {
    private static final String TAG = "SyncManager";
    private final SyncHelper syncHelper;
    private final AppDatabase db;
    private final Context context;
    private static SyncManager instance;
    private final ExecutorService executor;

    private SyncManager(Context context) {
        this.context = context.getApplicationContext();
        syncHelper = new SyncHelper(context);
        db = AppDatabase.getInstance(context);
        executor = Executors.newSingleThreadExecutor();
    }

    public static synchronized SyncManager getInstance(Context context) {
        if (instance == null) {
            instance = new SyncManager(context.getApplicationContext());
        }
        return instance;
    }

    // Полная синхронизация при запуске приложения
    public void performFullSync(int userId) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            Log.d(TAG, "Нет подключения к интернету, синхронизация отложена");
            return;
        }

        Log.d(TAG, "Начало полной синхронизации");
        
        executor.execute(() -> {
            // Синхронизация общих данных
            Log.d(TAG, "Синхронизация общих данных");
            syncHelper.syncUsers();
            syncHelper.syncProducts();
            syncHelper.syncPromos();
            syncHelper.syncCategories();

            // Синхронизация пользовательских данных
            if (userId != -1) {
                Log.d(TAG, "Синхронизация данных пользователя с ID: " + userId);
                syncHelper.syncFavorites(userId);
                syncHelper.syncCart(userId);
                syncHelper.syncOrders(userId);
                
                // Отправка локальных изменений на сервер
                Log.d(TAG, "Отправка локальных изменений на сервер");
                pushDataToServer(userId);
            } else {
                Log.d(TAG, "Пропуск синхронизации пользовательских данных: userId = -1");
            }
        });
    }

    // Отправка данных на сервер без синхронизации
    public void pushDataToServer(int userId) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            Log.d(TAG, "Нет подключения к интернету, отправка отложена");
            return;
        }

        Log.d(TAG, "Отправка данных на сервер");
        
        executor.execute(() -> {
            // Отправка избранного
            List<Favorite> favorites = db.favoriteDao().getFavoritesByUserId(userId);
            syncHelper.syncFavorites(userId);

            // Отправка корзины
            List<Cart> cartItems = db.cartDao().getCartItemsByUserId(userId);
            syncHelper.syncCart(userId);

            // Отправка заказов
            List<Order> orders = db.orderDao().getOrdersByUserId(userId);
            syncHelper.syncOrders(userId);
        });
    }

    public void shutdown() {
        executor.shutdown();
    }
} 