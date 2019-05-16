package brotifypacha.scheduler.auth_activity

import android.util.Log
import brotifypacha.scheduler.Constants
import brotifypacha.scheduler.SchedulerApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AuthRepository {

    val TAG = AuthRepository::class.java.simpleName

    var api: SchedulerApiService

    init {
        api = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(Constants.SERVER_BASE_URL)
            .build().create(SchedulerApiService::class.java)
    }

    fun signUp(username: String, password: String){

    }

    fun signIn(username: String, password: String){

    }

    suspend fun usernameTaken(username: String): Boolean {
        val result = api.get_user(username, "[]").execute().body()
        if (result?.result == "success")
            return true
        return false
    }
}