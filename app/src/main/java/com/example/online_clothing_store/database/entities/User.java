package com.example.online_clothing_store.database.entities;

import androidx.annotation.NonNull;
import androidx.room.*;
import java.io.Serializable;

@Entity(tableName = "users")
public class User implements Serializable{
    @PrimaryKey(autoGenerate = true)
    private int id;
    @NonNull
    @ColumnInfo(name = "name")
    private String name;
    @NonNull
    @ColumnInfo(name = "email")
    private String email;
    @NonNull
    @ColumnInfo(name = "password_hash")
    private String passwordHash;
    @ColumnInfo(name = "address")
    private String address;

    // Конструкторы, геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    @NonNull
    public String getName() { return name; }
    public void setName(@NonNull String name) { this.name = name; }

    @NonNull
    public String getEmail() { return email; }
    public void setEmail(@NonNull String email) { this.email = email; }

    @NonNull
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(@NonNull String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
