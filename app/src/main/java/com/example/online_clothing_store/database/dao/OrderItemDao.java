package com.example.online_clothing_store.database.dao;

import androidx.room.*;
import com.example.online_clothing_store.database.entities.OrderItem;
import java.util.List;

@Dao
public interface OrderItemDao {
    @Insert
    void insert(OrderItem orderItem);

    @Query("SELECT * FROM order_items WHERE order_id = :orderId")
    List<OrderItem> getOrderItemsByOrderId(int orderId);

    @Delete
    void delete(OrderItem orderItem);

    @Query("DELETE FROM order_items")
    void deleteAll();
}