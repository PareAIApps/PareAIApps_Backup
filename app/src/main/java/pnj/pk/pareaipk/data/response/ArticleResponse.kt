package pnj.pk.pareaipk.data.response

import com.google.gson.annotations.SerializedName

data class ArticleResponse(
    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("image_url")
    val imageUrl: String,

    @SerializedName("label")
    val label: String,

    @SerializedName("subtitle")
    val subtitle: String
)

// For handling the nested JSON structure
typealias ArticlesResponseMap = Map<String, ArticleResponse>