package com.example.online_clothing_store;

import android.app.Application;

import com.squareup.picasso.BuildConfig;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Настраиваем Picasso
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttp3Downloader(this, Integer.MAX_VALUE));
        Picasso built = builder.build();

        //  индикаторы только для отладки
        built.setIndicatorsEnabled(BuildConfig.DEBUG);
        built.setLoggingEnabled(BuildConfig.DEBUG);

        // Устанавливаем кастомный экземпляр Picasso
        Picasso.setSingletonInstance(built);
    }
}