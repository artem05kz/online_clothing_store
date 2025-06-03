package com.example.online_clothing_store.database.dao;

import androidx.room.*;
import com.example.online_clothing_store.database.entities.Order;
import java.util.List;

@Dao
public interface OrderDao {
    @Query("SELECT * FROM orders WHERE user_id = :userId ORDER BY order_date DESC")
    List<Order> getOrdersByUserId(int userId);
    @Insert
    long insert(Order order);
    @Query("DELETE FROM orders WHERE user_id = :userId")
    void deleteByUserId(int userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Order... orders);
}