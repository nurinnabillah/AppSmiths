package com.example.appsmiths.remote;

import com.example.appsmiths.model.DeleteResponse;
import com.example.appsmiths.model.RecyclableItem;
import com.example.appsmiths.model.UpdatedStatus;
import com.example.appsmiths.model.UserRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RequestItem {
    @GET("requests/?order=status&orderType=desc")
    Call<List<UserRequest>> getAllRequests(@Header("api-key") String api_key);

    @FormUrlEncoded
    @POST("requests/{id}")
    Call<UserRequest> updateRequestItem(@Header("api-key") String apiKey,
                                        @Path("id") int id,
                                        @Field("status") String status,
                                        @Field("weight") Double weight,
                                        @Field("total_price") Double total_price);

    @GET("requests")
    Call<List<UserRequest>> getAllStatus(@Header("api-key") String api_key, @Query("status") String status);

}
