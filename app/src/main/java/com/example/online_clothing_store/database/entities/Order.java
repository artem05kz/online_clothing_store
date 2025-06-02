package com.example.online_clothing_store.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "orders",
        indices = {
                @Index("user_id") // Индекс для user_id
        },
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "id",
                childColumns = "user_id",
                onDelete = ForeignKey.CASCADE
        )
)
public class Order {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "user_id")
    private int userId;

    @ColumnInfo(name = "order_date", defaultValue = "CURRENT_TIMESTAMP")
    private String orderDate;

    @ColumnInfo(name = "status", defaultValue = "'В обработке'")
    private String status;
    
    @ColumnInfo(name = "address")
    private String address;
    

    // Конструктор, геттеры и сеттеры
    public Order(int userId, String address) {
        this.userId = userId;
        this.address = address;
        this.status = "В обработке";
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getOrderDate() { return orderDate; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
}