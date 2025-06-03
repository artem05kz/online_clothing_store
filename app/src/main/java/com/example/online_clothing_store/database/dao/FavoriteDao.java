package com.example.online_clothing_store.database.dao;

import androidx.room.*;
import com.example.online_clothing_store.database.entities.Favorite;
import com.example.online_clothing_store.database.entities.Product;

import java.util.List;

@Dao
public interface FavoriteDao {
    @Insert
    void insert(Favorite favorite);

    @Delete
    void delete(Favorite favorite);

    @Query("SELECT * FROM favorites WHERE user_id = :userId")
    List<Favorite> getFavoritesByUserId(int userId);

    @Query("SELECT * FROM favorites WHERE user_id = :userId AND product_id = :productId")
    Favorite getFavorite(int userId, int productId);

    @Query("SELECT products.* FROM products " +
            "INNER JOIN favorites ON products.id = favorites.product_id " +
            "WHERE favorites.user_id = :userId")
    List<Product> getFavoriteProductsForUser(int userId);
    @Query("DELETE FROM favorites WHERE user_id = :userId")
    void deleteByUserId(int userId);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Favorite... favorites);
}