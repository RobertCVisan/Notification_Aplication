package com.cod1.notification_aplication
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("house/{houseId}/recommendations")
    suspend fun getRecommendations(
        @Path("houseId") houseId: Int,
        @Query("day") day: Int
    ):  Recommendations

    @GET("house/{houseId}/self_sufficiency_consumption")
    suspend fun getSelfSufficiency(
        @Path("houseId") houseId: Int,
        @Query("day") day: Int
    ): SelfSufficiencyConsumption
}