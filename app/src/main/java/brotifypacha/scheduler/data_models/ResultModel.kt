package brotifypacha.scheduler.data_models

data class ResultModel<T> (
    val result: String,
    val data: T,
    val type: String = "",
    val field: String = "",
    val description: String = ""
){

    override fun toString() : String{
        var output = "result = $result"
        output += " data = $data"
        output += " type = $type"
        output += " field = $field"
        output += " description = $description"
        return output
    }
}

