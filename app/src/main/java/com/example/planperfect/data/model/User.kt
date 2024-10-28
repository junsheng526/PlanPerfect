package com.example.planperfect.data.model

import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.DocumentId
import java.util.Date

data class User(
    @DocumentId
    var id: String = "",
    var name: String = "",
    var email: String = "",
    var phoneNumber: String = "",
    var dateOfBirth: Date? = null,
    var country: String = "",
    val currencyCode: String? = "",
    var photo: Blob = Blob.fromBytes(ByteArray(0)),
)