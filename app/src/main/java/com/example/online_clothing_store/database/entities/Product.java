package com.example.online_clothing_store.database.entities;

import androidx.annotation.NonNull;
import androidx.room.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "products",
        foreignKeys = @ForeignKey(
                entity = Category.class,
                parentColumns = "id",
                childColumns = "category_id",
                onDelete = ForeignKey.NO_ACTION,
                onUpdate = ForeignKey.NO_ACTION
        )
)
public class Product implements Serializable {
    private static final long serialVersionUID = 1L;
    @PrimaryKey(autoGenerate = true)
    private int id;
    @NonNull
    @ColumnInfo(name = "name")
    private String name;
    @NonNull
    @ColumnInfo(name = "price")
    private double price;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "size")
    private String size;

    @ColumnInfo(name = "brand")
    private String brand;

    @ColumnInfo(name = "rating")
    private double rating;

    @ColumnInfo(name = "image_url")
    private String mainImageUrl;

    @ColumnInfo(name = "category_id")
    private Integer categoryId;

    @ColumnInfo(name = "composition")
    private String composition;

    public Product(String name, double price, String description, String size, String brand, double rating,
                   String mainImageUrl, String composition, Integer categoryId)
    {
        this.name = name;
        this.price = price;
        this.description = description;
        this.size = size;
        this.brand = brand;
        this.rating = rating;
        this.mainImageUrl = mainImageUrl;
        this.composition = composition;
        this.categoryId = categoryId;
    }
    public void setId(int id) {this.id = id;}
    public void setName(String name) {this.name = name;}

    public void setPrice(double price) {this.price = price;}

    public void setDescription(String description) {this.description = description;}

    public void setSize(String size) {this.size = size;}

    public void setBrand(String brand) {this.brand = brand;}

    public void setRating(double rating) {this.rating = rating;}

    public void setMainImageUrl(String imageUrls) {this.mainImageUrl = mainImageUrl;}

    public void setComposition(String composition) {this.composition = composition;}
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    // Геттеры
    public int getId() {return id;}
    public String getName() { return name; }
    public double getPrice() { return price; }
    public double getRating() { return rating; }
    public String getBrand() { return brand; }
    public String getSize() { return size; }
    public String getMainImageUrl() { return mainImageUrl; }
    public String getDescription() { return description; }
    public String getComposition() { return composition; }
    public Integer getCategoryId() { return categoryId; }
}