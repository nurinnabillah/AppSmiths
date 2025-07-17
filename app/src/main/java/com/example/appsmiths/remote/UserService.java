package com.example.appsmiths.remote;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

import com.example.appsmiths.model.RecyclableItem;
import com.example.appsmiths.model.User;

import java.util.List;

public interface UserService {

    @FormUrlEncoded
    @POST("users/login")
    Call<User> login(@Field("username") String username, @Field("password") String password);

    @FormUrlEncoded
    @POST("users/login")
    Call<User> loginEmail(@Field("email") String username, @Field("password") String password);

    @FormUrlEncoded
    @POST("users/register")
    Call<User> register(
            @Field("username") String username,
            @Field("email") String email,
            @Field("password") String password
    );

    @GET("users")
    Call<List<User>> getAllUsers(@Header("api-key") String api_key);

}
