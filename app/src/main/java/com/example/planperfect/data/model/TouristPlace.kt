package com.example.planperfect.data.model

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.DocumentId

data class TouristPlace(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "", // URL
    val category: String = "",
    var startTime: String? = "",
    var endTime: String? = "",
    var notes: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
) : Parcelable {

    // Parcelable implementation
    constructor(parcel: Parcel) : this(
        id = parcel.readString() ?: "",
        name = parcel.readString() ?: "",
        description = parcel.readString() ?: "",
        imageUrl = parcel.readString() ?: "",
        category = parcel.readString() ?: "",
        startTime = parcel.readString(),
        endTime = parcel.readString(),
        notes = parcel.readString(),
        latitude = parcel.readValue(Double::class.java.classLoader) as? Double, // Correctly read as Double
        longitude = parcel.readValue(Double::class.java.classLoader) as? Double  // Correctly read as Double
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(description)
        parcel.writeString(imageUrl)
        parcel.writeString(category)
        parcel.writeString(startTime)
        parcel.writeString(endTime)
        parcel.writeString(notes)
        parcel.writeValue(latitude)  // Write latitude
        parcel.writeValue(longitude) // Write longitude
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TouristPlace> {
        override fun createFromParcel(parcel: Parcel): TouristPlace {
            return TouristPlace(parcel)
        }

        override fun newArray(size: Int): Array<TouristPlace?> {
            return arrayOfNulls(size)
        }
    }

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "name" to name,
            "description" to description,
            "imageUrl" to imageUrl,
            "category" to category,
            "startTime" to startTime,
            "endTime" to endTime,
            "notes" to notes,
            "latitude" to latitude,   // Include latitude
            "longitude" to longitude   // Include longitude
        )
    }

    // No-argument constructor for Firebase
    constructor() : this("", "", "", "", "", null, null, null, null, null)
}