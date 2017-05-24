package com.weareholidays.bia.retrofit;

import com.weareholidays.bia.database.model.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by kapil on 15/5/17.
 */

public interface BiaEndpoints {

    @GET("users/{username}")
    Call<User> getUser(@Path("username") String username);

    @POST("api/register")
    Call<User> createUser(@Body User user);

}