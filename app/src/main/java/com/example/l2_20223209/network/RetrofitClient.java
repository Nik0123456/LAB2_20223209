package com.example.l2_20223209.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import java.util.concurrent.TimeUnit;

public class RetrofitClient {
    
    private static final String BASE_URL = "https://cataas.com/";
    private static Retrofit retrofit = null;
    
    public static Retrofit getClient() {
        if (retrofit == null) {
            // Configurar logging interceptor
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            
            // Configurar OkHttpClient
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor);
            
            // Crear instancia de Retrofit
            retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();
        }
        return retrofit;
    }
    
    public static NetworkService getNetworkService() {
        return getClient().create(NetworkService.class);
    }
}