package com.example.online_clothing_store.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.online_clothing_store.database.entities.ProductImage;

import java.util.List;

@Dao
public interface ProductImageDao {
    @Insert
    void insert(ProductImage productImage);

    @Query("SELECT * FROM product_images WHERE product_id = :productId")
    List<ProductImage> getImagesForProduct(int productId);
}