package com.example.online_clothing_store;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.online_clothing_store.database.AppDatabase;
import com.example.online_clothing_store.database.entities.Cart;
import com.example.online_clothing_store.database.entities.Order;
import com.example.online_clothing_store.database.entities.OrderItem;
import com.example.online_clothing_store.database.entities.User;
import com.example.online_clothing_store.sync.SyncHelper;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class CheckoutActivity extends AppCompatActivity {
    private static final String TAG = "CheckoutActivity";
    private EditText addressInput;
    private Button confirmOrderButton;
    private int currentUserId;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        addressInput = findViewById(R.id.addressInput);
        confirmOrderButton = findViewById(R.id.confirmOrderButton);

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        currentUserId = prefs.getInt("user_id", -1);
        if (currentUserId == -1) {
            Toast.makeText(this, "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = AppDatabase.getInstance(this);

        new Thread(() -> {
            User user = db.userDao().getUserById(currentUserId);
            runOnUiThread(() -> {
                if (user != null && user.getAddress() != null && !user.getAddress().isEmpty()) {
                    addressInput.setText(user.getAddress());
                }
            });
        }).start();

        confirmOrderButton.setOnClickListener(v -> {
            String address = addressInput.getText().toString().trim();
            if (address.isEmpty()) {
                Toast.makeText(this, "Пожалуйста, введите адрес доставки", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                try {
                    Log.d(TAG, "Начало создания заказа для пользователя " + currentUserId);
                    
                    // Создаем заказ
                    Order order = new Order();
                    order.setUserId(currentUserId);
                    order.setAddress(address);
                    order.setOrderDate(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
                    order.setStatus("В обработке");
                    List<Cart> cartItems = db.cartDao().getCartItemsByUserId(currentUserId);
                    order.setTotalAmount(calculateTotalAmount(cartItems));
                    long orderId = db.orderDao().insert(order);
                    Log.d(TAG, "Заказ создан с ID: " + orderId);

                    // Получаем товары из корзины
                    cartItems = db.cartDao().getCartItemsByUserId(currentUserId);
                    Log.d(TAG, "Получено товаров из корзины: " + cartItems.size());

                    // Создаем элементы заказа
                    for (Cart cartItem : cartItems) {
                        OrderItem orderItem = new OrderItem();
                        orderItem.setOrderId((int) orderId);
                        orderItem.setProductId(cartItem.getProductId());
                        orderItem.setQuantity(cartItem.getQuantity());
                        orderItem.setPrice(cartItem.getPrice());
                        db.orderItemDao().insert(orderItem);
                    }
                    Log.d(TAG, "Элементы заказа созданы");

                    // Очищаем корзину
                    db.cartDao().deleteCartByUserId(currentUserId);
                    Log.d(TAG, "Корзина очищена");

                    // Синхронизируем с сервером
                    SyncHelper syncHelper = new SyncHelper(this);
                    CountDownLatch syncLatch = new CountDownLatch(2);
                    
                    // Синхронизация заказов
                    new Thread(() -> {
                        syncHelper.syncOrders(currentUserId);
                        syncLatch.countDown();
                    }).start();
                    
                    // Синхронизация корзины
                    new Thread(() -> {
                        syncHelper.syncCart(currentUserId);
                        syncLatch.countDown();
                    }).start();

                    // Ждем завершения синхронизации
                    syncLatch.await();
                    Log.d(TAG, "Синхронизация завершена");

                runOnUiThread(() -> {
                    Toast.makeText(this, "Заказ успешно оформлен", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, ProfileActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    finish();
                });
                } catch (Exception e) {
                    Log.e(TAG, "Ошибка при создании заказа", e);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Ошибка при создании заказа: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();
        });

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }

    private double calculateTotalAmount(List<Cart> cartItems) {
        double total = 0.0;
        for (Cart item : cartItems) {
            total += item.getPrice() * item.getQuantity();
        }
        return total;
    }
}