package com.example.online_clothing_store.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.online_clothing_store.R;
import com.example.online_clothing_store.database.AppDatabase;
import com.example.online_clothing_store.database.entities.Cart;
import com.example.online_clothing_store.database.entities.Product;
import com.squareup.picasso.Picasso;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private List<Cart> cartItems;
    private Context context;
    private OnDeleteListener deleteListener;
    private OnQuantityChangeListener quantityChangeListener;
    private Map<Integer, Product> productCache;

    public interface OnDeleteListener {
        void onDelete(Cart cartItem);
    }

    public interface OnQuantityChangeListener {
        void onQuantityChanged(Cart cartItem, int newQuantity);
    }

    public CartAdapter(List<Cart> cartItems, OnDeleteListener deleteListener, OnQuantityChangeListener quantityChangeListener) {
        this.cartItems = cartItems;
        this.deleteListener = deleteListener;
        this.quantityChangeListener = quantityChangeListener;
        this.productCache = new HashMap<>();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        Cart cartItem = cartItems.get(position);
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            Product product = db.productDao().getProductById(cartItem.getProductId());
            productCache.put(cartItem.getProductId(), product);
            ((android.app.Activity) context).runOnUiThread(() -> {
                if (product != null) {
                    holder.itemName.setText(product.getName());
                    holder.itemPrice.setText(String.format("%.2f ₽", product.getPrice()));
                    holder.itemSize.setText("Размер: " + product.getSize());
                    holder.quantityText.setText(String.valueOf(cartItem.getQuantity()));
                    if (product.getMainImageUrl() != null && !product.getMainImageUrl().isEmpty()) {
                        Picasso.get().load(product.getMainImageUrl()).into(holder.itemImage);
                    } else {
                        holder.itemImage.setImageResource(R.drawable.error);
                    }
                }
            });
        }).start();

        holder.deleteButton.setOnClickListener(v -> deleteListener.onDelete(cartItem));

        holder.increaseButton.setOnClickListener(v -> {
            int newQuantity = cartItem.getQuantity() + 1;
            cartItem.setQuantity(newQuantity);
            Product product = productCache.get(cartItem.getProductId());
            if (product != null) {
                cartItem.setPrice(product.getPrice() * newQuantity);
                quantityChangeListener.onQuantityChanged(cartItem, newQuantity);
                holder.quantityText.setText(String.valueOf(newQuantity));
                holder.itemPrice.setText(String.format("%.2f ₽", product.getPrice() * newQuantity));
            }
        });

        holder.decreaseButton.setOnClickListener(v -> {
            int newQuantity = cartItem.getQuantity() - 1;
            if (newQuantity > 0) {
                cartItem.setQuantity(newQuantity);
                Product product = productCache.get(cartItem.getProductId());
                if (product != null) {
                    cartItem.setPrice(product.getPrice() * newQuantity);
                    quantityChangeListener.onQuantityChanged(cartItem, newQuantity);
                    holder.quantityText.setText(String.valueOf(newQuantity));
                    holder.itemPrice.setText(String.format("%.2f ₽", product.getPrice() * newQuantity));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImage;
        TextView itemPrice;
        TextView itemName;
        TextView itemSize;
        TextView quantityText;
        ImageButton deleteButton;
        ImageButton increaseButton;
        ImageButton decreaseButton;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.itemImage);
            itemPrice = itemView.findViewById(R.id.itemPrice);
            itemName = itemView.findViewById(R.id.itemName);
            itemSize = itemView.findViewById(R.id.itemSize);
            quantityText = itemView.findViewById(R.id.quantityText);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            increaseButton = itemView.findViewById(R.id.increaseButton);
            decreaseButton = itemView.findViewById(R.id.decreaseButton);
        }
    }
}