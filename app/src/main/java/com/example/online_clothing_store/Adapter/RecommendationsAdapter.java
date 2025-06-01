package com.example.online_clothing_store.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.online_clothing_store.LoginActivity;
import com.example.online_clothing_store.ProductDetailActivity;
import com.example.online_clothing_store.R;
import com.example.online_clothing_store.database.AppDatabase;
import com.example.online_clothing_store.database.dao.FavoriteDao;
import com.example.online_clothing_store.database.entities.Cart;
import com.example.online_clothing_store.database.entities.Favorite;
import com.example.online_clothing_store.database.entities.Product;
import com.squareup.picasso.Picasso;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecommendationsAdapter extends RecyclerView.Adapter<RecommendationsAdapter.ViewHolder> {

    private final List<Product> productList;
    private final Context context;
    private final boolean isGuestMode;
    private final int currentUserId;
    private final Set<Integer> favoriteProductIds;
    private final ExecutorService executor;
    private final AppDatabase db;

    public RecommendationsAdapter(Context context, List<Product> productList, boolean isGuestMode, int currentUserId) {
        this.context = context;
        this.productList = productList;
        this.isGuestMode = isGuestMode;
        this.currentUserId = currentUserId;
        this.favoriteProductIds = new HashSet<>();
        this.executor = Executors.newFixedThreadPool(2);
        this.db = AppDatabase.getInstance(context);
        loadFavorites();
    }

    private void loadFavorites() {
        if (!isGuestMode && currentUserId != -1) {
            executor.execute(() -> {
                try {
                    List<Favorite> favorites = db.favoriteDao().getFavoritesByUserId(currentUserId);
                    synchronized (favoriteProductIds) {
                        favoriteProductIds.clear();
                        for (Favorite favorite : favorites) {
                            favoriteProductIds.add(favorite.getProductId());
                        }
                    }
                    notifyDataSetChanged();
                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(context, "Ошибка загрузки избранного", Toast.LENGTH_SHORT).show());
                }
            });
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recommendation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);

        // Установка данных
        holder.tvTitle.setText(product.getName());
        holder.tvPrice.setText(String.format("%.0f ₽", product.getPrice()));

        // Загрузка изображения
        if (product.getMainImageUrl() != null && !product.getMainImageUrl().isEmpty()) {
            Picasso.get()
                    .load(product.getMainImageUrl())
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.error)
                    .into(holder.ivProductImage);
        } else {
            holder.ivProductImage.setImageResource(R.drawable.error);
        }

        // Установка состояния избранного
        boolean isFavorite = favoriteProductIds.contains(product.getId());
        holder.ibFavorite.setImageResource(isFavorite ? R.drawable.ic_heart_filled : R.drawable.ic_heart);

        // Обработка клика на кнопку избранного
        holder.ibFavorite.setOnClickListener(v -> {
            if (isGuestMode || currentUserId == -1) {
                Toast.makeText(context, "Для добавления в избранное требуется регистрация", Toast.LENGTH_SHORT).show();
                context.startActivity(new Intent(context, LoginActivity.class));
                return;
            }

            executor.execute(() -> {
                try {
                    FavoriteDao favoriteDao = db.favoriteDao();
                    Favorite existing = favoriteDao.getFavorite(currentUserId, product.getId());
                    if (existing == null) {
                        Favorite newFavorite = new Favorite();
                        newFavorite.setUserId(currentUserId);
                        newFavorite.setProductId(product.getId());
                        favoriteDao.insert(newFavorite);
                        synchronized (favoriteProductIds) {
                            favoriteProductIds.add(product.getId());
                        }
                        runOnUiThread(() -> {
                            holder.ibFavorite.setImageResource(R.drawable.ic_heart_filled);
                            Toast.makeText(context, "Добавлено в избранное", Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        favoriteDao.delete(existing);
                        synchronized (favoriteProductIds) {
                            favoriteProductIds.remove(product.getId());
                        }
                        runOnUiThread(() -> {
                            holder.ibFavorite.setImageResource(R.drawable.ic_heart);
                            Toast.makeText(context, "Удалено из избранного", Toast.LENGTH_SHORT).show();
                        });
                    }
                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(context, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            });
        });

        // Обработка клика на кнопку "Добавить в корзину"
        holder.ibAddToCart.setOnClickListener(v -> {
            if (isGuestMode || currentUserId == -1) {
                Toast.makeText(context, "Для добавления в корзину требуется регистрация", Toast.LENGTH_SHORT).show();
                context.startActivity(new Intent(context, LoginActivity.class));
                return;
            }

            executor.execute(() -> {
                try {
                    List<Cart> existingItems = db.cartDao().getCartItemsByUserId(currentUserId);
                    boolean productExists = false;
                    for (Cart item : existingItems) {
                        if (item.getProductId() == product.getId()) {
                            item.setQuantity(item.getQuantity() + 1);
                            db.cartDao().update(item);
                            productExists = true;
                            break;
                        }
                    }
                    if (!productExists) {
                        Cart cartItem = new Cart();
                        cartItem.setUserId(currentUserId);
                        cartItem.setProductId(product.getId());
                        cartItem.setQuantity(1);
                        db.cartDao().insert(cartItem);
                    }
                    runOnUiThread(() -> {
                        Toast.makeText(context, "Добавлено в корзину", Toast.LENGTH_SHORT).show();
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(context, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            });
        });

        // Переход к деталям продукта
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("product", product);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    private void runOnUiThread(Runnable runnable) {
        new android.os.Handler(android.os.Looper.getMainLooper()).post(runnable);
    }

    public void shutdownExecutor() {
        executor.shutdown();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvTitle;
        TextView tvPrice;
        ImageButton ibFavorite;
        ImageButton ibAddToCart;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            ibFavorite = itemView.findViewById(R.id.ibFavorite);
            ibAddToCart = itemView.findViewById(R.id.ibAddToCart);
        }
    }
}