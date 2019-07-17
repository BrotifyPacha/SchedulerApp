package brotifypacha.scheduler

import brotifypacha.scheduler.data_models.*
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

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
        @Query("password") password: String,
        @Query("firebase_id") firebase_id: String
    ) : Deferred<ResultModel<TokenModel>>

    @GET("/api/users_self")
    fun get_active_user(
        @Header("Authorization")
        token: String
    ) : Deferred<ResultModel<UserModel>>


    @GET("api/users/")
    fun get_users(
        @Query("match_fields") match_fields: String?,
        @Query("return_fields") return_fields: String?
    ): Deferred<ResultModel<List<UserModel>>>

    @POST("api/users/")
    fun create_user(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("firebase_id") firebase_id: String?
    ): Deferred<ResultModel<TokenModel>>

    @GET("api/users/{username}")
    fun get_user(
        @Header("Authorization") token: String?,
        @Path("username") username: String,
        @Query("return_fields") return_fields: String?
    ): Deferred<ResultModel<UserModel>>

    @PATCH("api/users/{username}")
    fun patch_user(
        @Header("Authorization") token: String,
        @Path("username") username: String,
        @Query("username") usernameToChange: String?,
        @Query("password") passwordToChange: String?,
        @Query("auth_password") authPassword: String?
    ): Deferred<ResultModel<Unit>>

    @DELETE("api/users/{username}")
    fun delete_user(
        @Header("Authorization") token: String
    ): Deferred<ResultModel<Unit>>


    @GET("api/schedules/")
    fun get_schedules(
        @Query("match_fields") match_fields: String?,
        @Query("return_fields") return_fields: String?
    ): Deferred<ResultModel<List<ScheduleModel>>>

    @POST("api/schedules/")
    fun create_schedule(
        @Header("Authorization") header: String,
        @Query("name") name: String,
        @Query("alias") alias: String,
        @Query("schedule") schedule: String,
        @Query("first_day") first_day: String
    ): Deferred<ResultModel<Unit>>

    @GET("api/schedules/{alias}")
    fun getScheduleByAlias(
        @Header("Authorization") header: String?,
        @Path("alias") alias: String
    ) : Deferred<ResultModel<ScheduleModel>>

    @PUT("api/schedules/{alias}")
    fun insertChange(
        @Header("Authorization") header: String,
        @Path("alias") alias: String,
        @Query("date") changeDate: Long,
        @Query("change") change: String
    ) : Deferred<ResultModel<Unit>>

    @PATCH("api/schedules/{alias}")
    fun patchSchedule(
        @Header("Authorization") header: String,
        @Path("alias") alias: String,
        @Query("alias") aliasToChange: String?,
        @Query("name") nameToChange: String?,
        @Query("first_day") first_day: String?,
        @Query("schedule") schedule: String?
    ) : Deferred<ResultModel<Unit>>

    @DELETE("api/schedules/{alias}")
    fun deleteSchedule(
        @Header("Authorization") token: String,
        @Path("alias") alias: String,
        @Query("alias") confirmAlias: String
    ) : Deferred<ResultModel<Unit>>

    @POST("api/schedules/{alias}/subscribe")
    fun subscribe(
        @Header("Authorization") token: String,
        @Path("alias") alias: String
    ) : Deferred<ResultModel<Unit>>

    @DELETE("api/schedules/{alias}/subscribe")
    fun unsubscribe(
        @Header("Authorization") token: String,
        @Path("alias") alias: String
    ) : Deferred<ResultModel<Unit>>

}
