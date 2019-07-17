package brotifypacha.scheduler

class Constants {

    companion object {
        val MIN_USERNAME_LEN = 5
        val MIN_PASSWORD_LEN = 6
        val CHANNEL_ID = "default channel"
        val SHARED_PREFERENCES_NAME = "scheduler_preferences"
        val PREF_VALUE_OFFLINE= "offline"
        val PREF_KEY_TOKEN = "token"
        val PREF_KEY_ID = "id"

        val SERVER_BASE_URL = "http://46.173.218.120:5001/"
        //val SERVER_BASE_URL = "http://178.219.153.30:5000/"


        val SUCCESS = "success"
        val NETWORK_ERROR = "network_error"
        val AUTH_ERROR = "auth_error"
        val ERROR = "error"

        val ADMOB_APP_ID = "ca-app-pub-4572037752382250~1980086575"
        val ADMOB_BANNER_ID = "ca-app-pub-4572037752382250/5160073242"
    }

}