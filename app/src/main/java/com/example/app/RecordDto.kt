package com.example.app

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/*
    Data Transfer Object для записи из файла
*/
// TODO: -refactor data types of cells

@Parcelize
class RecordDto(
    val area: String,
    val street: String,
    val houseNumber: String,
    val flatNumber: String,
    val account: String,
    val name: String,
    val puNumber: String,
    val puType: String,
    val lastKoDate: String,
    val lastKo_D: String,
    val lastKo_N: String,
    var ko_D: String,
    var ko_N: String,
    var comments: String
) : Parcelable {

    override fun toString(): String {
        return "$area | $houseNumber | $name"
    }

}