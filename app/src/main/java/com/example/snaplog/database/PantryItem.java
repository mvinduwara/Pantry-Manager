package com.example.snaplog.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "pantry_items")
public class PantryItem {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String barcode;
    public String name;
    public long dateAdded;
    public long expirationDate;

    public PantryItem(String barcode, String name, long dateAdded, long expirationDate) {
        this.barcode = barcode;
        this.name = name;
        this.dateAdded = dateAdded;
        this.expirationDate = expirationDate;
    }
}