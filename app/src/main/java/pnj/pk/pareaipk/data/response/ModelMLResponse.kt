package pnj.pk.pareaipk.data.response

import com.google.gson.annotations.SerializedName

data class ModelMLResponse(
    @SerializedName("class_label")
    val class_label: String,

    @SerializedName("confidence")
    val confidence: Double,

    @SerializedName("description")
    val description: String,


    @SerializedName("suggestion")
    val suggestion: String,

    @SerializedName("tools_receipt")
    val tools_receipt: String,

    @SerializedName("tutorial")
    val tutorial: String,

    @SerializedName("createdAt")
    var createdAt: String
)