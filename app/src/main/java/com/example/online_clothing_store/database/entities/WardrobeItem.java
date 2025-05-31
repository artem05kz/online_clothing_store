// WardrobeItem.java
package com.example.online_clothing_store.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import java.io.Serializable;

@Entity(
        tableName = "wardrobe_items",
        primaryKeys = {"wardrobe_id", "product_id"},
        foreignKeys = {
                @ForeignKey(
                        entity = Wardrobe.class,
                        parentColumns = "id",
                        childColumns = "wardrobe_id",
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
public class WardrobeItem implements Serializable {
    @ColumnInfo(name = "wardrobe_id")
    private int wardrobeId;

    @ColumnInfo(name = "product_id")
    private int productId;

    @ColumnInfo(name = "type")
    private String type; // HEAD, TOP, BOTTOM, SHOES

    // Геттеры и сеттеры
    public int getWardrobeId() { return wardrobeId; }
    public void setWardrobeId(int wardrobeId) { this.wardrobeId = wardrobeId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}