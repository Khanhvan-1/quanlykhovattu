package com.example.khovattu.api;

import android.content.Context;
import android.content.SharedPreferences;
import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static Retrofit retrofit;

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request original = chain.request();

                        SharedPreferences prefs = context.getSharedPreferences("khovattu_session", Context.MODE_PRIVATE);
                        String token = prefs.getString("token", "");

                        System.out.println("🪪 Token gửi đi: " + token);

                        Request.Builder builder = original.newBuilder()
                                .header("Content-Type", "application/json");

                        if (!token.isEmpty()) {
                            builder.header("Authorization", "Bearer " + token);
                        }

                        Request request = builder.build();
                        Response response = chain.proceed(request);

                        System.out.println("📩 Kết quả API: " + response.code() + " " + request.url());
                        return response;
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl("http://10.0.2.2:3000/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }

    public static ApiService getApiService(Context context) {
        return getClient(context).create(ApiService.class);
    }

}
