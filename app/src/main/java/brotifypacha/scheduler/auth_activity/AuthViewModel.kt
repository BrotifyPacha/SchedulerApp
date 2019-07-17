package brotifypacha.scheduler.auth_activity

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.*
import brotifypacha.scheduler.Constants
import brotifypacha.scheduler.database.SchedulerDataBase
import brotifypacha.scheduler.database.User
import brotifypacha.scheduler.database.UsersDao
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.coroutines.*
import java.util.regex.Pattern
import kotlinx.coroutines.runBlocking as runBlocking

class AuthViewModel(private val app: Application) : AndroidViewModel(app) {

    val TAG : String = AuthViewModel::class.java.simpleName

    private val username: MutableLiveData<String> = MutableLiveData()
    private val password: MutableLiveData<String> = MutableLiveData()
    //can be ["username", "password"]
    private val focusedField : MutableLiveData<String> = MutableLiveData()
    private val usernameHelperMsg : MutableLiveData<String> = MutableLiveData()
    private val passwordHelperMsg : MutableLiveData<String> = MutableLiveData()

    private val eventError : MutableLiveData<String?> = MutableLiveData()
    private val eventAuthenticated: MutableLiveData<Boolean> = MutableLiveData()

    lateinit var signInJob : Job
    lateinit var signUpJob : Job

    var repository: AuthRepository
    var pref: SharedPreferences
    lateinit var instanceId: String

    init {
        repository = AuthRepository()
        pref = app.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener {
            if (it.isSuccessful){
                instanceId = it.result!!.token
            }
        }

    }

    fun signIn(username: String, password: String) {
        if (!::signInJob.isInitialized || !signInJob.isActive || signInJob.isCompleted) {
            signInJob = viewModelScope.launch {
                val result = repository.signIn(username, password, instanceId)
                if (result == null) {
                    setErrorEvent("Ошибка сети")
                    return@launch
                }
                Log.d(TAG, "Начинаем обрабатывать запрос ${result}")

                if ((result.result == "error") and (result.type == "wrong_credentials")) {
                    setErrorEvent("Неверное имя пользователя или пароль")
                } else if (result.result == Constants.SUCCESS) {
                    val token = result.data.token
                    val result = repository.getUserData(token)
                    Log.d(TAG, "$result")
                    if (result == null){
                        setErrorEvent("Ошибка сети")
                        return@launch
                    }

                    if (result.result.equals(Constants.SUCCESS)){
                        val username = result.data.username
                        val id = result.data._id

                        Log.d(TAG, "id = ${id} token = ${token}")
                        withContext(Dispatchers.IO) {
                            SchedulerDataBase.getInstance(app)
                                .getUsersDao()
                                .insert(User(id, username))
                        }
                        setAuthorizationVariables(token, id)
                        setEventAuthenticated()
                    }
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
                val result = repository.signUp(username, password, instanceId)
                if (result == null) {
                    setErrorEvent("Ошибка сети")
                    return@launch
                }
                if ((result.result == "error") and (result.type == "field")) {
                    Log.d(TAG, result.description)
                    if (result.field == "username"){
                        reactToUsername(result.description)
                    } else if (result.field == password) {
                        reactToPassword(result.description)
                    }
                } else if (result.result == Constants.SUCCESS) {

                    val token = result.data.token
                    val result = repository.getUserData(token)
                    if (result == null){
                        setErrorEvent("Ошибка сети")
                        return@launch
                    }
                    if (result.result.equals(Constants.SUCCESS)){
                        val username = result.data.username
                        val id = result.data._id

                        withContext(Dispatchers.IO) {
                            SchedulerDataBase.getInstance(app)
                                .getUsersDao()
                                .insert(User(id, username))
                        }
                        setAuthorizationVariables(token, id)
                        setEventAuthenticated()

                    }
                }

            }
        }
    }

    fun setAuthorizationVariables(token: String, id: String){
        pref.edit().clear().apply()
        pref.edit()
            .putString(Constants.PREF_KEY_ID, id)
            .putString(Constants.PREF_KEY_TOKEN, token)
            .apply()
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
        if (username.length < Constants.MIN_USERNAME_LEN){
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
                setUsernameHelperMsg("Имя пользователя должно быть больше ${Constants.MIN_USERNAME_LEN} символов в длину")
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
        if (password.length < Constants.MIN_PASSWORD_LEN)
            return "error_1"
        return Constants.SUCCESS
    }

    fun reactToPassword(result: String){
        when (result){
            "error_1" -> setPasswordHelperMsg("Пароль должен быть больше ${Constants.MIN_PASSWORD_LEN} символов в длину")
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

    fun getEventAuthenticated() : LiveData<Boolean>{
        return eventAuthenticated
    }

    private fun setEventAuthenticated(){
        eventAuthenticated.value = true
    }

    fun setAuthenticationEventHandled(){
        eventAuthenticated.value = null
    }
}
