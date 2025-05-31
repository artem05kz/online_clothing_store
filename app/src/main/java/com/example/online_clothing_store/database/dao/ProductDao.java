package com.example.online_clothing_store.database.dao;

import androidx.room.*;
import com.example.online_clothing_store.database.entities.Product;
import com.example.online_clothing_store.database.entities.ProductImage;
import java.util.List;

@Dao
public interface ProductDao {
    @Insert
    long  insertProductImage(ProductImage productImage);
    @Insert
    long  insertProduct(Product product);
    @Query("SELECT * FROM products")
    List<Product> getAllProducts();
    @Query("SELECT * FROM products WHERE id = :productId LIMIT 1")
    Product getProductById(int productId);
    @Update
    void updateProduct(Product product);

    @Delete
    void deleteProduct(Product product);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Product... products);

    @Query("SELECT * FROM products ORDER BY id DESC LIMIT 10")
    List<Product> getNewArrivals();

    @Query("SELECT * FROM products WHERE rating > 4.5 ORDER BY RANDOM() LIMIT 8")
    List<Product> getRecommendedProducts();
}