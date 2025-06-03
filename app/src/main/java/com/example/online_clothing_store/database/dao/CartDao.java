package com.example.online_clothing_store.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Query;
import androidx.room.OnConflictStrategy;
import com.example.online_clothing_store.database.entities.Cart;
import java.util.List;

@Dao
public interface CartDao {
    @Query("SELECT * FROM cart WHERE user_id = :userId")
    List<Cart> getCartItemsByUserId(int userId);

    @Insert
    void insert(Cart cart);

    @Update
    void update(Cart cart);

    @Delete
    void delete(Cart cart);
    @Query("DELETE FROM cart WHERE user_id = :userId")
    void deleteCartByUserId(int userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Cart... carts);

}