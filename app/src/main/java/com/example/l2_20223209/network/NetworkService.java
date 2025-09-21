package com.example.l2_20223209.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import okhttp3.ResponseBody;

public interface NetworkService {
    @GET("ping")
    Call<Void> ping();
    
    @GET("cat/says/{text}")
    Call<ResponseBody> getCatWithText(@Path("text") String text);
}