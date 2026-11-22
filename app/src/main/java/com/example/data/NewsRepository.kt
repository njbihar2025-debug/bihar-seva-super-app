package com.example.data

import android.text.Html
import android.util.Xml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class NewsRepository(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()
) {
    suspend fun fetchNews(
        region: NewsRegion,
        district: String,
        preferEnglish: Boolean
    ): Result<List<NewsArticle>> = withContext(Dispatchers.IO) {
        try {
            val langParam = if (preferEnglish) "hl=en-IN&gl=IN&ceid=IN:en" else "hl=hi&gl=IN&ceid=IN:hi"
            val url = when (region) {
                NewsRegion.LOCAL -> {
                    val encoded = URLEncoder.encode("$district Bihar", "UTF-8")
                    "https://news.google.com/rss/search?q=$encoded&$langParam"
                }
                NewsRegion.BIHAR -> {
                    val encoded = URLEncoder.encode("Bihar", "UTF-8")
                    "https://news.google.com/rss/search?q=$encoded&$langParam"
                }
                NewsRegion.NATIONAL -> {
                    "https://news.google.com/rss?$langParam"
                }
            }

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "BiharSevaAndroid/1.0")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(IllegalStateException("News feed error: HTTP ${response.code}"))
                }
                val body = response.body?.string().orEmpty()
                if (body.isBlank()) {
                    return@withContext Result.failure(IllegalStateException("Empty news feed response"))
                }
                val parsed = parseRssXml(body, region)
                Result.success(parsed)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseRssXml(xmlContent: String, region: NewsRegion): List<NewsArticle> {
        val articles = mutableListOf<NewsArticle>()
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(StringReader(xmlContent))

        var insideItem = false
        var title = ""
        var link = ""
        var pubDate = ""
        var description = ""
        var source = ""
        var currentTag = ""

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT && articles.size < 25) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    currentTag = parser.name ?: ""
                    if (currentTag.equals("item", ignoreCase = true)) {
                        insideItem = true
                        title = ""
                        link = ""
                        pubDate = ""
                        description = ""
                        source = ""
                    }
                }
                XmlPullParser.TEXT -> {
                    val text = parser.text ?: ""
                    if (insideItem) {
                        when (currentTag.lowercase()) {
                            "title" -> title += text
                            "link" -> link += text
                            "pubdate" -> pubDate += text
                            "description" -> description += text
                            "source" -> source += text
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    val endTag = parser.name ?: ""
                    if (endTag.equals("item", ignoreCase = true) && insideItem) {
                        val cleanTitle = stripHtml(title).trim()
                        if (cleanTitle.isNotEmpty()) {
                            val resolvedSource = stripHtml(source).trim().ifEmpty {
                                cleanTitle.substringAfterLast(" - ", "News Desk").trim()
                            }
                            val headlineOnly = cleanTitle.substringBeforeLast(" - ").trim()
                                .ifEmpty { cleanTitle }
                            val cleanDesc = stripHtml(description).trim()
                            val cleanDate = pubDate.trim().removeSuffix(" GMT").take(25)
                            articles.add(
                                NewsArticle(
                                    id = "${region.code}_${articles.size}_${headlineOnly.hashCode()}",
                                    title = headlineOnly,
                                    description = cleanDesc.ifEmpty { headlineOnly },
                                    source = resolvedSource,
                                    pubDate = cleanDate,
                                    link = link.trim(),
                                    region = region
                                )
                            )
                        }
                        insideItem = false
                    }
                    currentTag = ""
                }
            }
            eventType = parser.next()
        }
        return articles
    }

    private fun stripHtml(raw: String): String {
        return Html.fromHtml(raw, Html.FROM_HTML_MODE_COMPACT)
            .toString()
            .replace('\u00A0', ' ')
            .replace(Regex("\\s+"), " ")
    }
}
