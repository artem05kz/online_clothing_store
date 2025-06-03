package com.example.online_clothing_store;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.online_clothing_store.Adapter.CartAdapter;
import com.example.online_clothing_store.database.AppDatabase;
import com.example.online_clothing_store.database.entities.Cart;
import com.example.online_clothing_store.database.entities.Product;
import com.example.online_clothing_store.database.dao.PromoDao;
import com.example.online_clothing_store.database.entities.Promo;
import com.example.online_clothing_store.sync.SyncHelper;

import java.util.List;
import java.util.concurrent.Executors;

public class CartActivity extends AppCompatActivity {
    private RecyclerView cartItemsList;
    private TextView totalPrice;
    private Button checkoutButton;
    private EditText promoCodeInput;
    private int currentUserId;
    private AppDatabase db;
    private List<Cart> cartItems;
    private CartAdapter adapter;
    private Promo appliedPromo = null;
    private double lastTotal = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Инициализация UI-элементов
        cartItemsList = findViewById(R.id.cartItemsList);
        totalPrice = findViewById(R.id.totalPrice);
        checkoutButton = findViewById(R.id.checkoutButton);
        promoCodeInput = findViewById(R.id.promoCodeInput);

        // Получение ID пользователя
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        currentUserId = prefs.getInt("user_id", -1);

        if (currentUserId == -1) {
            Toast.makeText(this, "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Инициализация базы данных
        db = AppDatabase.getInstance(this);
        cartItemsList.setLayoutManager(new LinearLayoutManager(this));

        // Загрузка данных корзины
        loadCartItems();

        // Попытка автозаполнения промокода из буфера обмена
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (clipboard.hasPrimaryClip()) {
            ClipData clip = clipboard.getPrimaryClip();
            if (clip != null && clip.getItemCount() > 0) {
                CharSequence text = clip.getItemAt(0).getText();
                if (text != null && text.length() > 0) {
                    promoCodeInput.setText(text.toString());
                }
            }
        }

        // Обработка кнопки "Заказать все"
        checkoutButton.setOnClickListener(v -> {
            startActivity(new Intent(this, CheckoutActivity.class));
        });

        // Обработка промокода (ручной ввод или автозаполнение)
        promoCodeInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT || actionId == 0) {
                String promoCode = promoCodeInput.getText().toString().trim();
                if (!promoCode.isEmpty()) {
                    checkAndApplyPromo(promoCode);
                }
            }
            return false;
        });
        setupMenuButtons();
    }
    @Override
    protected void onResume() {
        super.onResume();
        Executors.newSingleThreadExecutor().execute(() -> {
            SyncHelper syncHelper = new SyncHelper(this);
            syncHelper.syncCart(currentUserId);
        });
        loadCartItems();
    }
    private void loadCartItems() {
        new Thread(() -> {
            cartItems = db.cartDao().getCartItemsByUserId(currentUserId);
            runOnUiThread(() -> {
                adapter = new CartAdapter(cartItems, this::onDeleteItem, this::onQuantityChanged);
                cartItemsList.setAdapter(adapter);
                updateTotalPrice();
            });
        }).start();
    }

    private void onDeleteItem(Cart cartItem) {
        new Thread(() -> {
            db.cartDao().delete(cartItem);
            runOnUiThread(() -> {
                cartItems.remove(cartItem);
                runOnUiThread(() -> adapter.notifyDataSetChanged());
                updateTotalPrice();
            });
        }).start();
    }

    private void onQuantityChanged(Cart cartItem, int newQuantity) {
        new Thread(() -> {
            cartItem.setQuantity(newQuantity);
            if (newQuantity > 0) {
                db.cartDao().update(cartItem);
            } else {
                db.cartDao().delete(cartItem);
            }
            runOnUiThread(() -> {
                adapter.notifyDataSetChanged();
                updateTotalPrice();
            });
        }).start();
    }

    private void checkAndApplyPromo(String code) {
        new Thread(() -> {
            PromoDao promoDao = db.promoDao();
            Promo promo = promoDao.getPromoByCode(code);
            runOnUiThread(() -> {
                if (promo == null || !promo.isActive) {
                    appliedPromo = null;
                    Toast.makeText(this, "Промокод не найден или устарел", Toast.LENGTH_SHORT).show();
                    updateTotalPrice();
                } else {
                    appliedPromo = promo;
                    Toast.makeText(this, "Промокод применён: " + promo.code + " (-" + promo.discountPercent + "%)", Toast.LENGTH_SHORT).show();
                    updateTotalPrice();
                }
            });
        }).start();
    }

    private void updateTotalPrice() {
        new Thread(() -> {
            double total = 0;
            for (Cart item : cartItems) {
                Product product = db.productDao().getProductById(item.getProductId());
                if (product != null) {
                    total += product.getPrice() * item.getQuantity();
                }
            }
            double finalTotal = total;
            if (appliedPromo != null && appliedPromo.discountPercent > 0) {
                finalTotal = finalTotal * (1 - appliedPromo.discountPercent / 100.0);
            }
            lastTotal = finalTotal;
            final double totalForUi = finalTotal;
            runOnUiThread(() -> {
                totalPrice.setText(String.format("%.2f ₽", totalForUi));
            });
        }).start();
    }

    private void setupMenuButtons() {
        findViewById(R.id.imageButtonCart).setOnClickListener(v -> {
            Toast.makeText(this, "Вы уже в корзине", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.imageButtonHanger).setOnClickListener(v -> {
            startActivity(new Intent(this, CatalogActivity.class));
        });

        findViewById(R.id.imageButtonWardrobe).setOnClickListener(v -> {
            startActivity(new Intent(this, WardrobeActivity.class));
        });

        findViewById(R.id.imageButtonHome).setOnClickListener(v -> {
            startActivity(new Intent(this, RecommendationsActivity.class));
        });

        findViewById(R.id.imageButtonProfile).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });
    }
}