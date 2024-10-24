package com.example.planperfect.data.model

import android.os.Parcel
import android.os.Parcelable

data class Recommendation(
    val title: String,
    val description: String,
    val image_url: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        title = parcel.readString() ?: "",
        description = parcel.readString() ?: "",
        image_url = parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeString(image_url)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Recommendation> {
        override fun createFromParcel(parcel: Parcel): Recommendation {
            return Recommendation(parcel)
        }

        override fun newArray(size: Int): Array<Recommendation?> {
            return arrayOfNulls(size)
        }
    }
}