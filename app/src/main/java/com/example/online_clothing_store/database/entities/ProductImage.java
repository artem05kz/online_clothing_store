package com.example.online_clothing_store.database.entities;

import androidx.annotation.NonNull;
import androidx.room.*;

import java.io.Serializable;
@Entity(
        tableName = "product_images",
        indices = {
                @Index("product_id")
        },
        foreignKeys = @ForeignKey(
                entity = Product.class,
                parentColumns = "id",
                childColumns = "product_id",
                onDelete = ForeignKey.CASCADE
        )
)
public class ProductImage implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "product_id")
    private int productId;

    @ColumnInfo(name = "image_url")
    private String imageUrl;

    public ProductImage(int productId, String imageUrl) {
        this.productId = productId;
        this.imageUrl = imageUrl;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
