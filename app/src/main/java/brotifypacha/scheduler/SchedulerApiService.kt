package brotifypacha.scheduler

import brotifypacha.scheduler.data_models.*
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SchedulerApiService {

    companion object {
        fun build() : SchedulerApiService{
            return Retrofit.Builder()
                .baseUrl(Constants.SERVER_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .build().create(SchedulerApiService::class.java)
        }
    }

    @POST("/api/authenticate")
    fun authenticate(
        @Query("username") username: String,
        @Query("password") password: String
    ) : Deferred<ResultModel<TokenModel>>

    @GET("api/users/")
    fun get_users(
        @Query("match_fields") match_fields: String,
        @Query("return_fields") return_fields: String,
        @Query("limit") limit: Int,
        @Query("skip") skip: Int
    ): Deferred<ResultModel<List<UserModel>>>

    @POST("api/users/")
    fun create_user(
        @Query("username") username: String?,
        @Query("password") password: String?,
        @Query("firebase_id") firebase_id: String?
    ): Deferred<ResultModel<TokenModel>>

    @GET("api/users/{username}")
    fun get_user(
        @Path("username") username: String,
        @Query("return_fields") return_fields: String?
    ): Deferred<ResultModel<UserModel>>

    @GET("api/schedules/")
    fun get_schedules(
        @Query("match_fields") match_fields: String,
        @Query("return_fields") return_fields: String,
        @Query("limit") limit: Int,
        @Query("skip") skip: Int
    ): Deferred<ResultModel<List<ScheduleModel>>>

    @POST("api/schedules/")
    fun create_schedule(
        @Query("name") name: String,
        @Query("alias") alias: String,
        @Query("availability") availability: String,
        @Query("schedule") schedule: String,
        @Query("first_day") first_day: String
    ): Deferred<ResultModel<ScheduleModel>>
}
