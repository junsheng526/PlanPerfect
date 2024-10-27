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
    var isFavorite: Boolean = false,
    val currencyCode: String? = ""
) : Parcelable {

    // Parcelable constructor to read data
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
        longDescription = parcel.readString(),  // Read directly as String
        isFavorite = parcel.readByte() != 0.toByte(),
        currencyCode = parcel.readString()      // Read directly as String
    )

    // Write data to Parcel
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
        parcel.writeString(longDescription) // Write as String
        parcel.writeByte(if (isFavorite) 1 else 0)
        parcel.writeString(currencyCode)    // Write as String
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<TouristPlace> {
        override fun createFromParcel(parcel: Parcel): TouristPlace = TouristPlace(parcel)
        override fun newArray(size: Int): Array<TouristPlace?> = arrayOfNulls(size)
    }

    fun toMap(): Map<String, Any?> = mapOf(
        "name" to name,
        "description" to description,
        "imageUrls" to imageUrls,
        "category" to category,
        "startTime" to startTime,
        "endTime" to endTime,
        "notes" to notes,
        "latitude" to latitude,
        "longitude" to longitude,
        "longDescription" to longDescription,
        "isFavorite" to isFavorite,
        "currencyCode" to currencyCode
    )

    // No-argument constructor for Firebase
    constructor() : this("", "", "", emptyList(), "", null, null, null, null, null, "", false, "")
}
