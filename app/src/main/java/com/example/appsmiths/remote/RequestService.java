package com.example.appsmiths.remote;

import com.example.appsmiths.model.DeleteResponse;
import com.example.appsmiths.model.UserRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RequestService {

    @FormUrlEncoded
    @POST("requests")
    Call<UserRequest> submitRequest(@Header ("api-key") String apiKey,
                                @Field("user_id") int user_id,
                                @Field("item_id") String item_id,
                                @Field("address") String address,
                                @Field("request_date") String request_date,
                                @Field("notes") String notes);

    @GET("requests")
    Call<List<UserRequest>> getAllRequests(@Header("api-key") String api_key);

    @GET("requests/{request_id}")
    Call<UserRequest> getRequest(@Header("api-key") String api_key, @Path("request_id") int request_id);

    @DELETE("requests/{request_id}")
    Call<DeleteResponse> deleteRequest(@Header ("api-key") String apiKey, @Path("request_id") int request_id);

}
