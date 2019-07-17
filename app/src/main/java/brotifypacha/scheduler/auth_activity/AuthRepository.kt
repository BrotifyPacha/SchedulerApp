package brotifypacha.scheduler.auth_activity

import android.media.session.MediaSession
import android.util.ArrayMap
import android.util.Log
import brotifypacha.scheduler.Constants
import brotifypacha.scheduler.SchedulerApiService
import brotifypacha.scheduler.data_models.ResultModel
import brotifypacha.scheduler.data_models.TokenModel
import brotifypacha.scheduler.data_models.UserModel
import com.google.firebase.iid.FirebaseInstanceId
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

    suspend fun signUp(username: String, password: String, firebaseId: String) : ResultModel<TokenModel>? {
        try {
            val result = api.create_user(username, password, firebaseId).await()
            return result
        } catch (e : HttpException){
            Log.e(TAG, e.toString())
        } catch (e : Exception){
            Log.e(TAG+" signUp", e.toString())
        }
        return null
    }

    suspend fun signIn(username: String, password: String, firebaseId: String) : ResultModel<TokenModel>? {
        try {
            val result = api.authenticate(username, password, firebaseId).await()
            Log.d(TAG, result.toString())
            return result
        } catch (e : HttpException){
            Log.e(TAG, e.toString())
        } catch (e : Exception){
            Log.e(TAG+" signIn", e.toString())
        }
        return null
    }

    suspend fun getUserData(token: String): ResultModel<UserModel>? {
        try {
            val result = api.get_active_user(token).await()
            return result
        } catch (e : HttpException){
            Log.e(TAG+" getUserData", e.toString())
        } catch (e : Exception){
            Log.e(TAG+" getUserData", e.toString())
        }
        return null
    }

}