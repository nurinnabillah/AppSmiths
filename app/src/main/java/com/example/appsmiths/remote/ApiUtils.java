package com.example.appsmiths.remote;

import com.example.appsmiths.model.UserRequest;

public class ApiUtils {

    // REST API server URL
    public static final String BASE_URL = "http://178.128.220.20/theappsmiths/api/";

    // return UserService instance
    public static UserService getUserService() {
        return RetrofitClient.getClient(BASE_URL).create(UserService.class);
    }

    // return BookService instance
    public static RecycleItem getRecycleItem() {
        return RetrofitClient.getClient(BASE_URL).create(RecycleItem.class);
    }

    public static RequestItem getRequestUser() {
        return RetrofitClient.getClient(BASE_URL).create(RequestItem.class);
    }

    public static RecycleItem getItem() {
        return RetrofitClient.getClient(BASE_URL).create(RecycleItem.class);
    }

    public static RequestService getRequestService() {
        return RetrofitClient.getClient(BASE_URL).create(RequestService.class);
    }

}