package brotifypacha.scheduler.auth_activity

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import brotifypacha.scheduler.Constants
import kotlinx.coroutines.*
import java.util.regex.Pattern
import kotlinx.coroutines.runBlocking as runBlocking

class AuthViewModel : ViewModel() {

    val TAG : String = AuthViewModel::class.java.simpleName

    private val username: MutableLiveData<String> = MutableLiveData()
    private val password: MutableLiveData<String> = MutableLiveData()
    //can be ["username", "password"]
    private val focusedField : MutableLiveData<String> = MutableLiveData()
    private val usernameHelperMsg : MutableLiveData<String> = MutableLiveData()
    private val usernameErrorMsg : MutableLiveData<String> = MutableLiveData()
    private val passwordHelperMsg : MutableLiveData<String> = MutableLiveData()

    private val eventError : MutableLiveData<String?> = MutableLiveData()
    private val eventAuthenticated: MutableLiveData<String?> = MutableLiveData()

    lateinit var signInJob : Job
    lateinit var signUpJob : Job


    fun signIn(username: String, password: String) {
        if (!::signInJob.isInitialized || !signInJob.isActive || signInJob.isCompleted) {
            Log.d(TAG, "job: sign in started")
            signInJob = viewModelScope.launch {
                var result = AuthRepository().signIn(username, password)
                if (result == null) {
                    setErrorEvent("Ошибка сети")
                    return@launch
                }
                if (result.result == Constants.SUCCESS) {
                    setEventAuthenticated(result.data.token)
                } else if ((result.result == "error") and (result.type == "wrong_credentials")) {
                    Log.d(TAG, "job: неверный логин или пароль")
                    setErrorEvent("Неверное имя пользователя или пароль")
                }
            }
        } else {
            Log.d(TAG, "Waiting for job to finish")
        }
    }
    fun signUp(username: String, password: String) {
        if (!::signUpJob.isInitialized || !signUpJob.isActive || signUpJob.isCompleted) {
            Log.d(TAG, "job: sign up started")
            signUpJob = viewModelScope.launch {
                var result = AuthRepository().signUp(username, password)
                if (result == null) {
                    setErrorEvent("Ошибка сети")
                    return@launch
                }
                if (result.result == Constants.SUCCESS) {
                    setEventAuthenticated(result.data.token)
                } else if ((result.result == "error") and (result.type == "field")) {
                    Log.d(TAG, result.description)
                    if (result.field == "username"){
                        reactToUsername(result.description)
                    } else if (result.field == password) {
                        reactToPassword(result.description)
                    }
                }
            }
        }
    }
    /**
     * Осуществляет проверку [username] на соответствие четырем требованиям и возвращает
     * результат в зависимости от ошибки:
     * @return 'error_1' - Минимальная длина = 3
     * @return 'error_2' - Разрешенные символы = [a-z0-9_.-]
     * @return 'error_3' - Только один символ _ . - подряд
     * @return 'error_4' - Имя пользователя не должно быть занято
     * @return 'success' - Если ошибок нет
     */
    fun evaluateUsername(username: String) : String{
        if (username.length < AuthConstants.MIN_USERNAME_LEN){
            return "error_1"
        } else if (!Pattern.compile("[a-z0-9_.\\-]+").matcher(username).matches()){
            return "error_2"
        } else if (Pattern.compile(".*(__|\\.\\.|--).*").matcher(username).matches()){
            return "error_3"
        }
        return Constants.SUCCESS
    }

    fun reactToUsername(result : String){
        when (result) {
            "error_1" -> {
                setUsernameHelperMsg("Имя пользователя должно быть больше ${AuthConstants.MIN_USERNAME_LEN} символов в длину")
            }
            "error_2" -> {
                setUsernameHelperMsg("В имени пользователя разрешены лишь сиволы a-z, 0-9 и _ . - ")
            }
            "error_3" -> {
                setUsernameHelperMsg("В имени пользователя не разрешено иметь несколько _ . или - подряд")
            }
            "error_4" -> {
                setUsernameHelperMsg("Пользователь с этим именем уже зарегестрирован")
            }
            Constants.SUCCESS -> {
                setUsernameHelperMsg(null)
            }
        }
    }

    /**
     * Осуществляет проверку [username] на соответствие четырем требованиям и возвращает
     * результат в зависимости от ошибки:
     * @return 'error_1' - Минимальная длина = 3
     * @return 'error_2' - Разрешенные символы = [a-z0-9_.-]
     * @return 'error_3' - Только один символ _ . - подряд
     * @return 'error_4' - Имя пользователя не должно быть занято
     * @return 'success' - Если ошибок нет
     */
    fun evaluatePassword(password: String) : String {
        if (password.length < AuthConstants.MIN_PASSWORD_LEN)
            return "error_1"
        return Constants.SUCCESS
    }

    fun reactToPassword(result: String){
        when (result){
            "error_1" -> setPasswordHelperMsg("Пароль должен быть больше ${AuthConstants.MIN_PASSWORD_LEN} символов в длину")
            Constants.SUCCESS -> setPasswordHelperMsg(null)
        }
    }

    fun setUsername(username: String){
        this.username.value = username
    }
    fun getUsername(): LiveData<String>{
        return username
    }

    fun setPassword(password: String){
        this.password.value = password
    }
    fun getPassword(): LiveData<String>{
        return password
    }

    fun setFocusedField(field: String){
        this.focusedField.value = field
    }
    fun getFocusedField() : LiveData<String>{
        return focusedField
    }

    fun setUsernameHelperMsg(msg: String?){
        this.usernameHelperMsg.value = msg
    }
    fun getUsernameHelperMsg() : LiveData<String?>{
        return usernameHelperMsg
    }

    fun setPasswordHelperMsg(msg: String?){
        this.passwordHelperMsg.value = msg
    }
    fun getPasswordHelperMsg() : LiveData<String?>{
        return passwordHelperMsg
    }

    private fun setErrorEvent(msg: String){
        eventError.value = msg
    }
    fun getErrorEvent() : LiveData<String?>{
        return eventError
    }

    fun setErrorShown() {
        eventError.value = null
    }

    fun getEventAuthenticated() : LiveData<String?>{
        return eventAuthenticated
    }

    private fun setEventAuthenticated(token: String){
        eventAuthenticated.value = token
    }

    fun setAuthenticationComplete(){
        eventAuthenticated.value = null
    }
}
