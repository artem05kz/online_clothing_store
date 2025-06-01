package com.example.online_clothing_store.Adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import com.example.online_clothing_store.ProductDetailActivity;
import com.example.online_clothing_store.R;
import com.example.online_clothing_store.database.AppDatabase;
import com.example.online_clothing_store.database.dao.FavoriteDao;
import com.example.online_clothing_store.database.entities.Favorite;
import com.example.online_clothing_store.database.entities.Product;
import com.squareup.picasso.Picasso;
import android.os.Handler;
import android.os.Looper;
public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private final List<Product> productList;
    private final boolean isGuestMode;
    private final int currentUserId;
    public ProductAdapter(List<Product> productList, boolean isGuestMode, int currentUserId) {
        this.productList = productList;
        this.isGuestMode = isGuestMode;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);

        int width = parent.getWidth() / 2 - 20; // 20dp - отступы
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                width,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        view.setLayoutParams(params);

        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.tvTitle.setText(product.getName());
        holder.tvPrice.setText(String.format("%.2f ₽", product.getPrice()));
        holder.tvRating.setText(String.valueOf(product.getRating()));
        if (product.getMainImageUrl() != null && !product.getMainImageUrl().isEmpty()) {
            String firstImageUrl = product.getMainImageUrl();
            Picasso.get()
                    .load(product.getMainImageUrl())
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.error)
                    .into(holder.ivProductImage);
        } else {
            holder.ivProductImage.setImageResource(R.drawable.error);
        }

        // Проверяем, добавлен ли товар в избранное
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(holder.itemView.getContext());
            Favorite favorite = db.favoriteDao().getFavorite(currentUserId, product.getId());

            // Обновляем UI через Handler
            new Handler(Looper.getMainLooper()).post(() -> {
                if (favorite != null) {
                    holder.ibFavorite.setImageResource(R.drawable.ic_heart_filled);
                } else {
                    holder.ibFavorite.setImageResource(R.drawable.ic_heart);
                }
            });
        }).start();

        // Обработка клика на кнопку избранного
        holder.ibFavorite.setOnClickListener(v -> {
            if (isGuestMode) {
                Toast.makeText(v.getContext(),
                        "Для добавления в избранное требуется регистрация",
                        Toast.LENGTH_SHORT).show();
            } else {
                new Thread(() -> {
                    AppDatabase db = AppDatabase.getInstance(v.getContext());
                    FavoriteDao favoriteDao = db.favoriteDao();
                    Favorite existing = favoriteDao.getFavorite(currentUserId, product.getId());

                    if (existing == null) {
                        // Добавляем в избранное
                        Favorite newFavorite = new Favorite();
                        newFavorite.setUserId(currentUserId);
                        newFavorite.setProductId(product.getId());
                        favoriteDao.insert(newFavorite);

                        new Handler(Looper.getMainLooper()).post(() -> {
                            holder.ibFavorite.setImageResource(R.drawable.ic_heart_filled);
                            Toast.makeText(v.getContext(),
                                    "Добавлено в избранное",
                                    Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        // Удаляем из избранного
                        favoriteDao.delete(existing);

                        new Handler(Looper.getMainLooper()).post(() -> {
                            holder.ibFavorite.setImageResource(R.drawable.ic_heart);
                            Toast.makeText(v.getContext(),
                                    "Удалено из избранного",
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                }).start();
            }
        });

        holder.ibAddToCart.setOnClickListener(v -> {
            if (isGuestMode) {
                Toast.makeText(v.getContext(),
                        "Для добавления в корзину требуется регистрация",
                        Toast.LENGTH_SHORT).show();
            } else {
                // Логика добавления в корзину
            }
        });
        holder.ivProductImage.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ProductDetailActivity.class);
            intent.putExtra("product", product);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvRating;
        TextView tvTitle;
        TextView tvPrice;
        ImageButton ibFavorite;
        ImageButton ibAddToCart;
        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            ibFavorite = itemView.findViewById(R.id.ibFavorite);
            ibAddToCart = itemView.findViewById(R.id.ibAddToCart);

        }
    }
}