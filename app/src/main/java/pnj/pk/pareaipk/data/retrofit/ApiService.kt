package pnj.pk.pareaipk.data.retrofit

import okhttp3.MultipartBody
import okhttp3.RequestBody
import pnj.pk.pareaipk.data.response.ModelMLResponse
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @Multipart
    @POST("/predict")
    suspend fun predictImage(
        @Part image: MultipartBody.Part,
        @Part("suggestion") suggestion: RequestBody
    ): Response<ModelMLResponse>
}
