package com.example.online_clothing_store.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "promos")
public class Promo {
    @PrimaryKey(autoGenerate = true)
    public Integer id;
    public String imageUrl;
    public String code;
    public Integer discountPercent;
    public String description;
    @ColumnInfo(defaultValue = "1")
    public Boolean isActive = true;
} 