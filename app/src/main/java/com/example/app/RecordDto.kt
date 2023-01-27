package com.example.app

import android.os.Parcel
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
    houseNumber: String,
    flatNumber: Double,
    account: Double,
    val name: String,
    val puNumber: String,
    val puType: String,
    val lastKoDate: Double,
    val lastKo_D: Double,
    val lastKo_N: Double,
    val ko_D: Double,
    val ko_N: Double,
    comments: String
) : Parcelable {


    val flatNumber: String = flatNumber.toInt().toString()
        //        get() = "кв. $field"
        get() = field

    val account: String = account.toInt().toString()
        //        get() = "л/с $field"
        get() = field

    val houseNumber: String = houseNumber.split(".")[0]
        //        get() = "д. $field"
        get() = field


    override fun toString(): String {
        return "$area | $houseNumber | $name"
    }

}