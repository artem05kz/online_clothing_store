package com.example.online_clothing_store;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.online_clothing_store.database.AppDatabase;
import com.example.online_clothing_store.database.entities.Cart;
import com.example.online_clothing_store.database.entities.Order;
import com.example.online_clothing_store.database.entities.OrderItem;
import com.example.online_clothing_store.database.entities.User;
import java.util.List;

public class CheckoutActivity extends AppCompatActivity {
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

        // Загрузка адреса из профиля
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

            // Оформление заказа
            new Thread(() -> {
                Order order = new Order(currentUserId, address);
                long orderId = db.orderDao().insert(order);

                List<Cart> cartItems = db.cartDao().getCartItemsByUserId(currentUserId);
                for (Cart cartItem : cartItems) {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrderId((int) orderId);
                    orderItem.setProductId(cartItem.getProductId());
                    orderItem.setQuantity(cartItem.getQuantity());
                    db.orderItemDao().insert(orderItem);
                }

                db.cartDao().deleteCartByUserId(currentUserId);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Заказ успешно оформлен", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, ProfileActivity.class));
                    finish();
                });
            }).start();
        });
    }
}