package com.example.online_clothing_store.database.dao;

import androidx.room.*;
import com.example.online_clothing_store.database.entities.User;

import java.util.List;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(User user);
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getUserByEmail(String email);
    @Query("SELECT * FROM users WHERE id = :id")
    User getUserById(int id);
    @Update
    void update(User user);
    @Query("SELECT * FROM users")
    List<User> getAll();
    @Query("DELETE FROM users")
    void deleteAll();
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(User... users);
}
