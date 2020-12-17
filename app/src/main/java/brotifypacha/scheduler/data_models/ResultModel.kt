package brotifypacha.scheduler.data_models



data class ResultModel<T> (
    val result: Int,
    val data: T,
    val type: String = "",
    val field: String = "",
    val description: String = ""
){

    companion object {
        val CODE_SUCCESS: Int = 0
        val CODE_ERROR: Int = 1
    }

    override fun toString() : String{
        var output = "result = $result"
        output += " data = $data"
        output += " type = $type"
        output += " field = $field"
        output += " description = $description"
        return output
    }
}

