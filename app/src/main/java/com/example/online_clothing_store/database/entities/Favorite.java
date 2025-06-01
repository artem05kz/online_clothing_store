// Favorite.java
package com.example.online_clothing_store.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

import java.io.Serializable;

@Entity(
        tableName = "favorites",
        primaryKeys = {"user_id", "product_id"},
        indices = {
                @Index("user_id"),
                @Index("product_id")
        },
        foreignKeys = {
                @ForeignKey(
                        entity = User.class,
                        parentColumns = "id",
                        childColumns = "user_id",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Product.class,
                        parentColumns = "id",
                        childColumns = "product_id",
                        onDelete = ForeignKey.CASCADE
                )
        }
)
public class Favorite implements Serializable {
    @ColumnInfo(name = "user_id")
    private int userId;

    @ColumnInfo(name = "product_id")
    private int productId;

    // Геттеры и сеттеры
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
}