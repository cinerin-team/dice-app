package com.example.dice_app

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("/register")
    fun registerDevice(@Body data: DeviceData): Call<Map<String, String>>

    @GET("/check_status")
    fun checkStatus(
        @Query("mac_address") macAddress: String,
        @Query("date") date: String
    ): Call<Map<String, String>>

    @GET("/get_device")
    fun getDeviceColor(@Query("mac_address") macAddress: String): Call<Map<String, String>>
}