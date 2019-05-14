package brotifypacha.scheduler.auth_activity

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

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

        return "test"
    }
}
