package com.example.online_clothing_store.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.online_clothing_store.database.entities.Category;

@Dao
public interface CategoryDao {
    @Insert
    void insert(Category category);

    @Insert
    void insertAll(Category... categories);

    @Query("SELECT COUNT(*) FROM categories")
    int getCategoryCount();

    @Query("SELECT * FROM categories WHERE id = :id")
    Category getById(int id);
}