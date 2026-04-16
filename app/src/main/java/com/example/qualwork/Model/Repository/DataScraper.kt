package com.example.qualwork.Model.Repository

import com.example.qualwork.Model.Entity.searchMedication
import com.example.qualwork.Model.Entity.Pharmacy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import kotlin.math.pow

enum class ELikyStatus {
    AVAILABLE,
    NOT_AVAILABLE,
    NOT_FOUND
}
object DataScraper{
    private const val BASE_URL = "https://tabletki.ua"
    private const val MAX_DISTANCE = 3
    private fun levenshtein(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j
        for (i in 1..a.length) {
            for (j in 1..b.length) {
                dp[i][j] = if (a[i - 1] == b[j - 1]) {
                    dp[i - 1][j - 1]
                } else {
                    1 + minOf(dp[i - 1][j], dp[i][j - 1], dp[i - 1][j - 1])
                }
            }
        }
        return dp[a.length][b.length]
    }   //"відстань" між двома рядками
    private fun minDistance(medicineName: String, query: String): Int {
        val queryLower = query.trim().lowercase()
        val nameLower = medicineName.trim().lowercase()

        if (nameLower.contains(queryLower)) return 0

        return nameLower.split(" ").minOf { word ->
            levenshtein(word, queryLower)
        }
    }   //порівняння подібності рядків

    suspend fun search(query: String): List<searchMedication> = withContext(Dispatchers.IO) {
        val url = "$BASE_URL/search/?q=${query.trim()}"
        val doc = Jsoup.connect(url)
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            .timeout(10_000)
            .get()

        doc.select("div.card.card__category").map { card ->
                val name = card.select("div.card__category--info a span").text()
                val distance = minDistance(name, query)
                val parentDiv = card.closest("[data-ga-product-stores]")
                val pharmacyCount = parentDiv?.attr("data-ga-product-stores")?.toIntOrNull() ?: 0
                Pair(
                    searchMedication(
                        name = name,
                        manufacturer = card.select("div.card__category--info-additional div").text(),
                        minPrice = card.select("div.card__category--price").text(),
                        url = BASE_URL + card.select("div.card__category--info a").attr("href"),
                        imageUrl = card.select("div.card__category--img img").attr("src"),
                        pharmacyCount = pharmacyCount,
                        isExact = distance == 0
                    ),
                    distance
                )
            }
            .filter { (_, distance) -> distance <= MAX_DISTANCE }
            .map { (medicine, _) -> medicine }
    }
    suspend fun getMedicineInfo(
        medicineUrl: String,
        userLat: Double? = null,
        userLon: Double? = null
    ): searchMedication =
        withContext(Dispatchers.IO) {
            val detailDoc = Jsoup.connect(medicineUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(10_000)
                .get()

            val name = detailDoc.select("h1").first()?.text() ?: ""
            val imageUrl = detailDoc.select("div.carousel-item.active img").attr("src")
            val manufacturer = detailDoc.select("div.card__category--info-additional div").text()
            val priceSpans = detailDoc.select("span.product-price__value--large")
            val minPrice = if (priceSpans.size >= 2) {
                "від ${priceSpans[0].text()} до ${priceSpans[1].text()} грн"
            } else if (priceSpans.size == 1) {
                "від ${priceSpans[0].text()} грн"
            } else {
                "Ціна недоступна"
            }

            // Формуємо URL аптек з координатами або fallback на Київ
            val pharmacyUrl = if (userLat != null && userLon != null) {
                val citySlug = LocationHelper.getCitySlug(userLat, userLon)
                android.util.Log.d("SCRAPER", "pharmacyUrl slug: $citySlug")
                medicineUrl.trimEnd('/') + "/pharmacy/$citySlug/"
            } else {
                medicineUrl.trimEnd('/') + "/pharmacy/kyiv/"
            }

            android.util.Log.d("SCRAPER", "pharmacyUrl: $pharmacyUrl")


            val pharmacyDoc = Jsoup.connect(pharmacyUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(10_000)
                .get()

            val pharmacies = pharmacyDoc.select("article.address-card").mapNotNull { card ->
                try {
                    val headerBlock = card.select("div.address-card__header--block").first()
                    val locationStr = headerBlock?.attr("data-location") ?: return@mapNotNull null
                    val (lat, lng) = locationStr.split(",").map { it.toDouble() }

                    val pharmacyName = card.select("div.address-card__header--name span")
                        .first()?.text() ?: return@mapNotNull null
                    val address = card.select("div.address-card__header--address span").text()
                    val price = card.select("input[type=hidden]").attr("data-price-min")

                    Pharmacy(
                        name = pharmacyName,
                        address = address,
                        price = if (price.isNotEmpty()) "$price грн" else "Ціна недоступна",
                        latitude = lat,
                        longitude = lng
                    )
                } catch (e: Exception) {
                    null
                }
            }

            searchMedication(
                name = name,
                manufacturer = manufacturer,
                minPrice = minPrice,
                imageUrl = imageUrl,
                pharmacies = pharmacies,
                url = medicineUrl
            )
        }

    suspend fun checkELiky(medicineName: String): ELikyStatus = withContext(Dispatchers.IO) {
        try {
            val searchQuery = medicineName
                .split(" ")
                .first()
                .trim()
                android.util.Log.d("ELIKY", "searchQuery: $searchQuery")

            val url = "https://likicontrol.com.ua/пошук-ліків/?$searchQuery"
                android.util.Log.d("ELIKY", "url: $url")

            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(10_000)
                .get()

            val cards = doc.select("div.likiListItem")
                android.util.Log.d("ELIKY", "Знайдено карток: ${cards.size}")

            val matchingCards = cards.filter  { card ->
                val cardName = card.select("h2, h3, strong").text().uppercase()
                android.util.Log.d("ELIKY", "cardName: $cardName") // <- додай
                cardName.contains(searchQuery.uppercase())
            }
                android.util.Log.d("ELIKY", "matchingCards count: ${matchingCards.size}")

            val hasAnyDlLogo = matchingCards.any { card ->
                val hasDl = card.select("div.dl_logo").isNotEmpty()
                android.util.Log.d("ELIKY", "card: ${card.select("h2,h3,strong").text()} -> dl_logo: $hasDl")
                hasDl
            }

            return@withContext if (matchingCards.isEmpty()) {
                ELikyStatus.NOT_FOUND
            } else if (hasAnyDlLogo) {
                ELikyStatus.AVAILABLE
            } else {
                ELikyStatus.NOT_AVAILABLE
            }
        } catch (e: Exception) {
            android.util.Log.e("ELIKY", "error: ${e.message}")
            ELikyStatus.NOT_FOUND
        }
    }
    fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2).pow(2) +
                Math.cos(Math.toRadians(lat1)) *
                Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2).pow(2)
        return r * 2 * Math.asin(Math.sqrt(a))
    }
}