package brotifypacha.scheduler.data_models

data class ResultModel (
    val result: String,
    val data: List<Any>,
    val type: String,
    val field: String,
    val description: String
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

