package com.example.online_clothing_store.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.online_clothing_store.database.entities.Promo;
import java.util.List;

@Dao
public interface PromoDao {
    @Query("SELECT * FROM promos WHERE isActive = 1 LIMIT 1")
    Promo getActivePromo();

    @Query("SELECT * FROM promos WHERE code = :code LIMIT 1")
    Promo getPromoByCode(String code);

    @Query("SELECT * FROM promos WHERE isActive = 1")
    List<Promo> getAllPromos();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Promo promo);
    @Query("DELETE FROM promos")
    void deleteAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Promo... promos);
} 