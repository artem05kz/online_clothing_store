package com.example.online_clothing_store.database.dao;

import androidx.room.*;
import com.example.online_clothing_store.database.entities.User;
@Dao
public interface UserDao {
    @Insert
    long insert(User user);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getUserByEmail(String email);
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    User getUserById(int userId);
    @Update
    void update(User user);
}
