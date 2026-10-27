package com.cod1.notification_aplication

import java.util.Date

data class HourlyData(
    val hour:Int,
    val Recommendation_type:Int,
    val Hourly_Consumption: Double,
    val Hourly_Production: Double,
    val EpochTime: Long,
    val Real_Time: String
)

data class SpecificMessage(
    val Message: String,
    val MessageType:Int
)
data class Recommendations(
    val day:Int,
    val specific_message:List<SpecificMessage>,
    val general_message:String,
    val Hourly_data:List<HourlyData>

)