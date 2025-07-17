package com.example.appsmiths.remote;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * initialize Retrofit
 */
public class RetrofitClient {

    private static Retrofit retrofit = null;

    /**
     * Return instance of retrofit
     * @param URL REST API URL
     * @return retrofit instance
     */
    public static Retrofit getClient(String URL) {

        if (retrofit == null) {
            // Logging interceptor
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            // Lenient Gson
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            // Retrofit with logging and lenient Gson
            retrofit = new Retrofit.Builder()
                    .baseUrl(URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }

        return retrofit;
    }
}
