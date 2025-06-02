package com.example.online_clothing_store.api;

import com.example.online_clothing_store.database.entities.*;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

public interface ApiService {
    // --- USERS ---
    @GET("users")
    Call<List<User>> getUsers();

    @POST("sync/users")
    Call<Void> syncUsers(@Body List<User> users);

    // --- FAVORITES ---
    @GET("favorites?user_id={userId}")
    Call<List<Favorite>> getFavorites(@Path("userId") int userId);

    @POST("sync/favorites")
    Call<Void> syncFavorites(@Body List<Favorite> favorites);

    // --- PRODUCTS ---
    @GET("products")
    Call<List<Product>> getProducts();

    @POST("sync/products")
    Call<Void> syncProducts(@Body List<Product> products);

    // --- CART ---
    @GET("cart?user_id={userId}")
    Call<List<Cart>> getCart(@Path("userId") int userId);

    @POST("sync/cart")
    Call<Void> syncCart(@Body List<Cart> cartItems);

    // --- ORDERS ---
    @GET("orders?user_id={userId}")
    Call<List<Order>> getOrders(@Path("userId") int userId);

    @POST("sync/orders")
    Call<Void> syncOrders(@Body List<Order> orders);

    // --- PROMOS ---
    @GET("promos")
    Call<List<Promo>> getPromos();

    @GET("promos/active")
    Call<Promo> getActivePromo();

    @GET("promos/{code}")
    Call<Promo> getPromoByCode(@Path("code") String code);
}