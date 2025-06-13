package com.example.online_clothing_store.database;

import androidx.room.*;
import com.example.online_clothing_store.database.entities.*;
import com.example.online_clothing_store.database.dao.*;
import android.content.Context;
import java.util.List;

@Database(entities = {User.class, Product.class, ProductImage.class, Category.class,
        Favorite.class, Order.class, Cart.class, OrderItem.class, Promo.class}, version = 2)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract CartDao cartDao();
    public abstract OrderItemDao orderItemDao();
    public abstract FavoriteDao favoriteDao();
    public abstract OrderDao orderDao();
    public abstract UserDao userDao();
    public abstract ProductDao productDao();
    public abstract ProductImageDao productImageDao();
    public abstract CategoryDao categoryDao();
    public abstract PromoDao promoDao();
    private static final String DB_NAME = "online_clothing_store_db.db";
    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DB_NAME
                            )
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
