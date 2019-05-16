package brotifypacha.scheduler

import brotifypacha.scheduler.data_models.*
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SchedulerApiService {

    @GET("api/users/")
    suspend fun get_users(
        @Query("match_fields") match_fields: String,
        @Query("return_fields") return_fields: String,
        @Query("limit") limit: Int,
        @Query("skip") skip: Int
    ): Call<ResultModel>

    @POST("api/users/")
    suspend fun create_user(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("firebase_id") firebase_id: String
    ): Call<ResultModel>

    @GET("api/users/{username}")
    suspend fun get_user(
        @Path("username") username: String,
        @Query("return_fields") return_fields: String
    ): Call<ResultModel>

    @GET("api/schedules/")
    suspend fun get_schedules(
        @Query("match_fields") match_fields: String,
        @Query("return_fields") return_fields: String,
        @Query("limit") limit: Int,
        @Query("skip") skip: Int
    ): Call<ResultModel>

    @POST("api/schedules/")
    suspend fun create_schedule(
        @Query("name") name: String,
        @Query("alias") alias: String,
        @Query("availability") availability: String,
        @Query("schedule") schedule: String,
        @Query("first_day") first_day: String
    ): Call<ResultModel>
}
