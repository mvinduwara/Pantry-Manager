package com.example.snaplog.model;

import com.google.gson.annotations.SerializedName;

public class Product {
    @SerializedName("product_name")
    public String productName;

    @SerializedName("image_url")
    public String imageUrl;
}