package com.example.online_clothing_store.api;

import com.example.online_clothing_store.database.entities.*;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

public interface ApiService {
    // --- USERS ---
    @GET("users")
    Call<List<User>> getUsers();
    @POST("users/register")
    Call<User> registerUser(@Body User user);
    @POST("sync/users")
    Call<Void> syncUsers(@Body List<User> users);
    @PUT("users/{id}")
    Call<User> updateUser(@Path("id") int id, @Body User user);
    // --- FAVORITES ---
    @GET("favorites")
    Call<List<Favorite>> getFavorites(@Query("user_id") int userId);

    @POST("sync/favorites")
    Call<Void> syncFavorites(@Body List<Favorite> favorites);

    // --- PRODUCTS ---
    @GET("products")
    Call<List<Product>> getProducts();

    @GET("products/with-categories")
    Call<List<Product>> getProductsWithCategories();

    @POST("sync/products")
    Call<Void> syncProducts(@Body List<Product> products);

    // --- CART ---
    @GET
    ("cart") Call<List<Cart>> getCart(@Query("user_id") int userId);

    @POST("sync/cart")
    Call<Void> syncCart(@Body List<Cart> cartItems);

    // --- ORDERS ---
    @GET("orders")
    Call<List<Order>> getOrders(@Query("user_id") int userId);

    @POST("orders/sync")
    Call<Void> syncOrders(@Body List<Order> orders);

    // --- PROMOS ---
    @GET("promos")
    Call<List<Promo>> getPromos();

    @GET("promos/active")
    Call<Promo> getActivePromo();

    @GET("promos/{code}")
    Call<Promo> getPromoByCode(@Path("code") String code);

    // --- CATEGORIES ---
    @GET("categories")
    Call<List<Category>> getCategories();
}