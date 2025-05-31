package com.example.online_clothing_store.api;

import com.example.online_clothing_store.database.entities.Cart;
import com.example.online_clothing_store.database.entities.Product;
import com.example.online_clothing_store.database.entities.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    @POST("login")
    Call<User> login(@Body LoginRequest request);

    @GET("products")
    Call<List<Product>> getProducts();

    @POST("sync/user")
    Call<User> syncUser(@Body User user);

    @POST("sync/cart")
    Call<Void> syncCart(@Body List<Cart> cartItems);

    class LoginRequest {
        public String email;
        public String password;

        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }
}