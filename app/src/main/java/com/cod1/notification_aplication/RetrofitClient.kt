package com.cod1.notification_aplication

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    //  10.0.2.2 e local host pentru emulator
    //http://192.168.0.103:5000/ e local host pentru telefon fizic
    //192.168.56.1
    //127.0.0.1:5000
    private const val BASE_URL_EMULATOR = "http://10.0.2.2:5000/"
    private const val BASE_URL_PHYSICAL_PHONE="http://127.0.0.1:5000"
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .callTimeout(90, TimeUnit.SECONDS)
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_PHYSICAL_PHONE)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(ApiService::class.java)
    }
}