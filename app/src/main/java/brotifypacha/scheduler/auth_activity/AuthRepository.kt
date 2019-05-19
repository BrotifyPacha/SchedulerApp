package brotifypacha.scheduler.auth_activity

import android.media.session.MediaSession
import android.util.Log
import brotifypacha.scheduler.Constants
import brotifypacha.scheduler.SchedulerApiService
import brotifypacha.scheduler.data_models.ResultModel
import brotifypacha.scheduler.data_models.TokenModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception

class AuthRepository {

    val TAG = AuthRepository::class.java.simpleName

    var api: SchedulerApiService

    init {
        api = SchedulerApiService.build()
    }

    suspend fun signUp(username: String, password: String) : ResultModel<TokenModel>? {
        try {
            val result = api.create_user(username, password, "test").await()
            return result
        } catch (e : HttpException){
            Log.d(TAG, e.toString())
        } catch (e : Exception){
            Log.d(TAG, e.toString())
        }
        return null
    }

    suspend fun signIn(username: String, password: String) : ResultModel<TokenModel>? {
        try {
            val result = api.authenticate(username, password).await()
            return result
        } catch (e : HttpException){
            Log.d(TAG, e.toString())
        } catch (e : Exception){
            Log.d(TAG, e.toString())
        }
        return null
    }
}