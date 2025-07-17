package com.example.appsmiths.remote;

import com.example.appsmiths.model.DeleteResponse;
import com.example.appsmiths.model.RecyclableItem;
import com.example.appsmiths.model.User;
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

public interface RecycleItem {

    @GET("recyclable_items/?order=item_id&orderType=asc")
    Call<List<RecyclableItem>> getAllItems(@Header("api-key") String api_key);

    @GET("recyclable_items/{id}")
    Call<RecyclableItem> getItem(@Header("api-key") String api_key, @Path("id") int id);

    @FormUrlEncoded
    @POST("recyclable_items")
    Call<RecyclableItem> addRecycleItem(@Header("api-key") String apiKey,
                                 @Field("item_name") String item_name,
                                 @Field("price_per_kg") Double price_per_kg);

    @FormUrlEncoded
    @POST("recyclable_items/{id}")
    Call<RecyclableItem> updateItem(@Header ("api-key") String apiKey, @Path("id") int id,
                          @Field("item_name") String item_name, @Field("price_per_kg") Double price_per_kg);

    @DELETE("recyclable_items/{id}")
    Call<DeleteResponse> deleteBook(@Header ("api-key") String apiKey, @Path("id") int id);

    @GET("recyclable_items/{id}")
    Call<RecyclableItem> getItemById(@Header("api-key") String api_key, @Path("id") int id);
}
