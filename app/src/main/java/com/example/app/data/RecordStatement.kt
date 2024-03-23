package com.example.app.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class RecordStatement(
    @SerializedName("ListNumber") val listNumber: String,
    @SerializedName("ListDate") val listDate: String,
    @SerializedName("Source") val source: String,
    @SerializedName("Staff_Lnk") val staffLink: String,
    @SerializedName("Staff_Name") val staffName: String,
    @SerializedName("Company_Lnk") val companyLnk: String,
    @SerializedName("FirstAddress") val firstAddress: String?,
): Serializable