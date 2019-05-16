package brotifypacha.scheduler.auth_activity

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import brotifypacha.scheduler.auth_activity.AuthContants
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class AuthViewModel : ViewModel() {

    val TAG : String = AuthViewModel::class.java.simpleName

    var username: MutableLiveData<String> = MutableLiveData()
    var password: MutableLiveData<String> = MutableLiveData()

    fun setUsername(username: String){
        this.username.value = username
    }

    fun setPassword(password: String){
        this.password.value = password
    }

    fun getUsername(): LiveData<String>{
        return username
    }
    fun getPassword(): LiveData<String>{
        return password
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
    fun evaluateUsername(username: String) : String {

        if (username.length < AuthContants.MIN_USERNAME_LEN){
           return AuthContants.ERROR_LEN_USERNAME
        }

        if (!Pattern.compile("[a-z0-9_.-]+").matcher(username).matches()){
            return AuthContants.ERROR_CHAR_USERNAME
        }

        if (!Pattern.compile("(__|\\.\\.|--)").matcher(username).matches()){
            return AuthContants.ERROR_SYMBOL_CHAIN_USERNAME
        }

        if (!Pattern.compile("(__|\\.\\.|--)").matcher(username).matches()){
            return AuthContants.ERROR_SYMBOL_CHAIN_USERNAME
        }

        var isUsernameTaken: Boolean = false
        viewModelScope.launch {
            isUsernameTaken = AuthRepository().usernameTaken(username)
        }
        if (isUsernameTaken){
            return AuthContants.ERROR_TAKEN_USERNAME
        }

        return AuthContants.SUCCESS
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



        return "test"
    }
}
