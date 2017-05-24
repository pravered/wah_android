package com.weareholidays.bia.retrofit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by kapil on 15/5/17.
 */
public class RetrofitSingleton {

    public static final String BASE_URL = "http://183.82.96.201:8686/";

    private static RetrofitSingleton instance = null;

    private BiaEndpoints biaEndpoints;

    private RetrofitSingleton() {
        // initialize all the services here

        // initialize http logger (only in dev mode)
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        biaEndpoints = retrofit.create(BiaEndpoints.class);

    }

    public static synchronized RetrofitSingleton getInstance() {
        if (instance == null) {
            instance = new RetrofitSingleton();
        }

        return instance;
    }

    public BiaEndpoints getBiaEndpoints() {
        return biaEndpoints;
    }
}
