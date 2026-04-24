package com.example.snaplog.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface PantryItemDao {
    @Insert
    void insert(PantryItem item);

    @Query("SELECT * FROM pantry_items ORDER BY expirationDate ASC")
    List<PantryItem> getAllItems();

    @Query("SELECT * FROM pantry_items WHERE barcode = :barcode LIMIT 1")
    PantryItem getItemByBarcode(String barcode);

    @Delete
    void delete(PantryItem item);
}