package com.example.online_clothing_store.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "promos")
public class Promo {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String imageUrl;
    public String code;
    public int discountPercent;
    public String description;
    public boolean isActive;
} 