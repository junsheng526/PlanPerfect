package com.example.planperfect.data.model

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.DocumentId

data class TouristPlace(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val imageUrls: List<String> = emptyList(),
    val category: String = "",
    var startTime: String? = "",
    var endTime: String? = "",
    var notes: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val longDescription: String? = "",
    var isFavorite: Boolean = false // New property to track favorite status
) : Parcelable {

    // Parcelable implementation
    constructor(parcel: Parcel) : this(
        id = parcel.readString() ?: "",
        name = parcel.readString() ?: "",
        description = parcel.readString() ?: "",
        imageUrls = parcel.createStringArrayList() ?: emptyList(),
        category = parcel.readString() ?: "",
        startTime = parcel.readString(),
        endTime = parcel.readString(),
        notes = parcel.readString(),
        latitude = parcel.readValue(Double::class.java.classLoader) as? Double,
        longitude = parcel.readValue(Double::class.java.classLoader) as? Double,
        longDescription = parcel.readString(),
        isFavorite = parcel.readByte() != 0.toByte() // Read the boolean value
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(description)
        parcel.writeStringList(imageUrls)
        parcel.writeString(category)
        parcel.writeString(startTime)
        parcel.writeString(endTime)
        parcel.writeString(notes)
        parcel.writeValue(latitude)
        parcel.writeValue(longitude)
        parcel.writeValue(longDescription)
        parcel.writeByte(if (isFavorite) 1 else 0) // Write the boolean value
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
            "imageUrl" to imageUrls,
            "category" to category,
            "startTime" to startTime,
            "endTime" to endTime,
            "notes" to notes,
            "latitude" to latitude,
            "longitude" to longitude,
            "longDescription" to longDescription,
            "isFavorite" to isFavorite // Add this line if you want to save favorite status in Firestore
        )
    }

    // No-argument constructor for Firebase
    constructor() : this("", "", "", emptyList(), "", null, null, null, null, null, "", false)
}
