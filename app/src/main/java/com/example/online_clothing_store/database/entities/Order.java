package com.example.online_clothing_store.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;
import java.util.List;
import java.util.ArrayList;

@Entity(
        tableName = "orders",
        indices = {
                @Index("user_id")
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
    private Integer id;

    @ColumnInfo(name = "user_id")
    private int userId;

    @ColumnInfo(name = "order_date", defaultValue = "CURRENT_TIMESTAMP")
    private String orderDate;

    @ColumnInfo(name = "status", defaultValue = "'В обработке'")
    private String status;
    
    @ColumnInfo(name = "address")
    private String address;
    
    @ColumnInfo(name = "total_amount")
    private double totalAmount;
    
    @Ignore
    private User user;
    
    @Ignore
    private List<OrderItem> orderItems = new ArrayList<>();

    // Конструкторы
    public Order() {
        this.status = "В обработке";
    }

    public Order(int userId, String address) {
        this.userId = userId;
        this.address = address;
        this.status = "В обработке";
    }

    // Конструктор, геттеры и сеттеры
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getOrderDate() { return orderDate; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public List<OrderItem> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItem> orderItems) { this.orderItems = orderItems; }
    
    public void addOrderItem(OrderItem orderItem) {
        if (orderItems == null) {
            orderItems = new ArrayList<>();
        }
        orderItems.add(orderItem);
    }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}