package com.example.planperfect.utils

import com.example.planperfect.data.model.Itinerary
import com.example.planperfect.data.model.TouristPlace
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.*
object  DummyDataUtil {

    suspend fun createDummyMalaysiaTouristPlaces() {
        val firestore = FirebaseFirestore.getInstance()
        val placesCollection = firestore.collection("tourist_places")

        // Define a list of dummy tourist places in Malaysia with coordinates and multiple images
        val dummyPlaces = listOf(
            TouristPlace(
                id = UUID.randomUUID().toString(),
                name = "Petronas Twin Towers",
                description = "Iconic skyscrapers in Kuala Lumpur, known for their sky bridge and observation deck.",
                imageUrls = listOf(
                    "https://www.malaysiavisa.ae/blog/wp-content/uploads/2019/06/Petronas-Twin-Towers-Malaysia.jpg",
                    "https://images.locationscout.net/2018/05/klcc-park-at-petronas-twin-towers-malaysia.webp?h=1400&q=80",
                    "https://plus.unsplash.com/premium_photo-1700955569542-735a654503bf?fm=jpg&q=60&w=3000&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MXx8cGV0cm9uYXMlMjB0d2luJTIwdG93ZXJzJTJDJTIwa3VhbGElMjBsdW1wdXIlMkMlMjBtYWxheXNpYXxlbnwwfHwwfHx8MA%3D%3D"
                ),
                category = "Landmark",
                latitude = 3.1570,
                longitude = 101.7115
            ),
            TouristPlace(
                id = UUID.randomUUID().toString(),
                name = "Batu Caves",
                description = "A limestone hill featuring a series of caves and cave temples, known for its large statue of Lord Murugan.",
                imageUrls = listOf(
                    "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRk9azULZKTyouv3nwgNOLssEuhWo4OGvE0sg&s",
                    "https://www.mps.gov.my/sites/default/files/styles/panopoly_image_original/public/dsc_2219.jpg?itok=VJbWjRH4",
                    "https://cdn4.premiumread.com/?url=https://malaymail.com/malaymail/uploads/images/2024/01/24/181607.jpg&w=800&q=100&f=jpg&t=2"
                ),
                category = "Cultural Site",
                latitude = 3.2389,
                longitude = 101.6820
            ),
            TouristPlace(
                id = UUID.randomUUID().toString(),
                name = "George Town",
                description = "Capital of the state of Penang, famous for its well-preserved colonial buildings and street food.",
                imageUrls = listOf(
                    "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQrS0sRxNuXqv4vvMbL8_ysofuLI2YYXJIPFQ&s",
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/0/03/Gurney_Drive%2C_George_Town_in_2018.jpg/800px-Gurney_Drive%2C_George_Town_in_2018.jpg",
                    "https://www.tripsavvy.com/thmb/DnTMIADvI4AZwsnKZhaAyuo9wok=/1500x0/filters:no_upscale():max_bytes(150000):strip_icc()/penang-malaysia-b40c38589e794a61ba904d64c0a02c43.jpg"
                ),
                category = "Historical Site",
                latitude = 5.4141,
                longitude = 100.3288
            ),
            TouristPlace(
                id = UUID.randomUUID().toString(),
                name = "Langkawi",
                description = "An archipelago known for its stunning beaches, clear waters, and lush rainforests.",
                imageUrls = listOf(
                    "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcT5K5KZ3svdI31zieDyvpq1GfLHT2hami9GLw&s",
                    "https://panoramalangkawi.com/wp-content/uploads/elementor/thumbs/hnI9yf1-oyv9enlwzsjzqytnos4159sj8k8i6ovad2b4l06irs.jpg",
                    "https://www.berjayahotel.com/sites/default/files/blr-800x400-01.jpg"
                ),
                category = "Nature",
                latitude = 6.3754,
                longitude = 99.6769
            ),
            TouristPlace(
                id = UUID.randomUUID().toString(),
                name = "Mount Kinabalu",
                description = "The highest peak in Southeast Asia, offering challenging hikes and breathtaking views.",
                imageUrls = listOf(
                    "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ8vNn_sXror3wQQtbKI-lPg8kxLZLp-bCagQ&s",
                    "https://cdn-v2.theculturetrip.com/1200x675/wp-content/uploads/2018/03/shutterstock_433817725.webp",
                    "https://www.malaymail.com/malaymail/uploads/images/2023/06/16/122289.jpeg?v=1726317420"
                ),
                category = "Adventure",
                latitude = 5.9751,
                longitude = 116.5583
            ),
            TouristPlace(
                id = UUID.randomUUID().toString(),
                name = "Putrajaya",
                description = "The federal administrative center of Malaysia, known for its modern architecture and beautiful gardens.",
                imageUrls = listOf(
                    "https://ik.imagekit.io/tvlk/blog/2022/07/du-lich-putrajaya-1.jpg?tr=c-at_max",
                    "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcReLW7oWdylNTOj26IgNYxUSQMGhGvNMBLT0w&s"
                ),
                category = "Government",
                latitude = 2.9252,
                longitude = 101.6760
            ),
            TouristPlace(
                id = UUID.randomUUID().toString(),
                name = "Kuala Lumpur Bird Park",
                description = "Home to a wide variety of bird species, this park is a popular attraction for nature lovers.",
                imageUrls = listOf(
                    "https://minio.havehalalwilltravel.com/hhwt-upload/original_images/16012023014457_1596689175418_DSC02427.JPG",
                    "https://media.tacdn.com/media/attractions-splice-spp-674x446/07/97/70/34.jpg"
                ),
                category = "Nature",
                latitude = 3.1561,
                longitude = 101.6869
            ),
            TouristPlace(
                id = UUID.randomUUID().toString(),
                name = "Perhentian Islands",
                description = "A group of islands known for their crystal clear waters, ideal for snorkeling and diving.",
                imageUrls = listOf(
                    "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQZgGZda5kUlFrw-DfCXOyP5krzb8jC7eYdNA&s",
                    "https://www.scubadiving.com/sites/default/files/styles/655_1x_/public/trevor/Malaysia_DSC07415_Zakh%20Hymann.jpg?itok=jie_SRJf"
                ),
                category = "Beach",
                latitude = 5.9100,
                longitude = 102.7493
            )
        )

        // Insert dummy places into Firestore
        try {
            for (place in dummyPlaces) {
                placesCollection.document(place.id).set(place).await()
                println("Added: ${place.name}")
            }
            println("Dummy Malaysia tourist places added successfully.")
        } catch (e: Exception) {
            println("Error adding dummy places: ${e.message}")
        }
    }

    suspend fun createDummyItineraries() {
        val firestore = FirebaseFirestore.getInstance()
        val itineraryCollection = firestore.collection("itinerary")

        // Define a list of dummy itineraries
        val dummyItineraries = listOf(
            Itinerary(
                id = UUID.randomUUID().toString(),
                name = "Paris Adventure",
                startDate = "2024-07-01",
                endDate = "2024-07-10",
                homeCity = "New York",
                countryToVisit = "France",
                imageUrl = "https://example.com/sydney_opera_house.jpg",
            ),
            Itinerary(
                id = UUID.randomUUID().toString(),
                name = "Tokyo Excursion",
                startDate = "2024-09-15",
                endDate = "2024-09-25",
                homeCity = "Los Angeles",
                countryToVisit = "Japan",
                imageUrl = "https://example.com/sydney_opera_house.jpg",
            ),
            Itinerary(
                id = UUID.randomUUID().toString(),
                name = "Sydney Explorer",
                startDate = "2024-11-05",
                endDate = "2024-11-15",
                homeCity = "London",
                countryToVisit = "Australia",
                imageUrl = "https://example.com/sydney_opera_house.jpg",
            ),
            Itinerary(
                id = UUID.randomUUID().toString(),
                name = "Rome Getaway",
                startDate = "2024-05-20",
                endDate = "2024-05-30",
                homeCity = "Toronto",
                countryToVisit = "Italy",
                imageUrl = "https://example.com/sydney_opera_house.jpg",
            ),
            Itinerary(
                id = UUID.randomUUID().toString(),
                name = "Beijing Historical Tour",
                startDate = "2024-06-01",
                endDate = "2024-06-10",
                homeCity = "Vancouver",
                countryToVisit = "China",
                imageUrl = "https://example.com/sydney_opera_house.jpg",
            )
        )

        // Insert dummy itineraries into Firestore
        try {
            for (itinerary in dummyItineraries) {
                itineraryCollection.document(itinerary.id).set(itinerary).await()
                println("Added: ${itinerary.name}")
            }
            println("Dummy itineraries added successfully.")
        } catch (e: Exception) {
            println("Error adding dummy itineraries: ${e.message}")
        }
    }
}