package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Newspaper
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.BiharDistricts
import com.example.data.NewsArticle
import com.example.data.NewsRegion
import com.example.data.SavedArticle
import com.example.data.UiState
import com.example.data.UserSettings
import com.example.data.localizedText
import com.example.ui.components.CategoryBadge
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.ErrorStateCard
import com.example.ui.components.LoadingStateView
import com.example.ui.theme.MarigoldSaffron
import com.example.ui.theme.PeacockTeal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    selectedRegion: NewsRegion,
    newsState: UiState<List<NewsArticle>>,
    savedArticlesState: UiState<List<SavedArticle>>,
    settings: UserSettings,
    onSelectRegion: (NewsRegion) -> Unit,
    onRefresh: () -> Unit,
    onChangeDistrict: (String) -> Unit,
    onBookmarkArticle: (NewsArticle) -> Unit,
    onRemoveBookmark: (String) -> Unit
) {
    val context = LocalContext.current
    var showSavedOnly by remember { mutableStateOf(false) }
    var selectedArticle by remember { mutableStateOf<NewsArticle?>(null) }

    val savedArticles = (savedArticlesState as? UiState.Success)?.data.orEmpty()
    val savedTitles = remember(savedArticles) { savedArticles.map { it.title }.toSet() }

    Column(modifier = Modifier.fillMaxSize()) {
        // Editorial News Header Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .height(126.dp)
                .clip(RoundedCornerShape(22.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_news_bihar_1795339172770),
                contentDescription = "Bihar & National News banner",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.15f),
                                PeacockTeal.copy(alpha = 0.88f)
                            )
                        )
                    )
            )
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = localizedText(settings.language, "ताज़ा समाचार", "Live News Desk"),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "लोकल (${settings.defaultDistrict}) • बिहार • देश",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (showSavedOnly) MarigoldSaffron else Color.White.copy(alpha = 0.22f),
                        modifier = Modifier
                            .clickable { showSavedOnly = !showSavedOnly }
                            .testTag("news_saved_toggle")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Bookmark,
                                contentDescription = "Saved news",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "${savedArticles.size}",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.22f),
                        modifier = Modifier
                            .clickable {
                                showSavedOnly = false
                                onRefresh()
                            }
                            .testTag("news_refresh_button")
                    ) {
                        Box(modifier = Modifier.padding(8.dp)) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = "Refresh news",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        if (!showSavedOnly) {
            val regions = NewsRegion.entries
            val selectedTabIndex = regions.indexOf(selectedRegion).coerceAtLeast(0)
            PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
                regions.forEach { region ->
                    Tab(
                        selected = region == selectedRegion,
                        onClick = { onSelectRegion(region) },
                        modifier = Modifier.testTag("news_tab_${region.code.lowercase()}"),
                        text = {
                            Text(
                                text = localizedText(settings.language, region.hiLabel, region.enLabel),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }

            if (selectedRegion == NewsRegion.LOCAL) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(BiharDistricts) { district ->
                        FilterChip(
                            selected = district == settings.defaultDistrict,
                            onClick = { onChangeDistrict(district) },
                            label = { Text(district) },
                            leadingIcon = if (district == settings.defaultDistrict) {
                                { Icon(Icons.Rounded.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                }
            }

            when (newsState) {
                is UiState.Loading -> LoadingStateView("लाइव समाचार लोड हो रहे हैं...")
                is UiState.Error -> {
                    Box(modifier = Modifier.padding(16.dp)) {
                        ErrorStateCard(
                            message = newsState.message,
                            onRetry = onRefresh
                        )
                    }
                }
                is UiState.Success -> {
                    if (newsState.data.isEmpty()) {
                        Box(modifier = Modifier.padding(16.dp)) {
                            EmptyStateCard(
                                icon = Icons.Rounded.Newspaper,
                                title = "अभी कोई समाचार नहीं मिला",
                                subtitle = "कृपया रिफ्रेश करें या दूसरा जिला/सेक्शन चुनें।"
                            )
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(newsState.data, key = { it.id }) { article ->
                                val isSaved = savedTitles.contains(article.title)
                                NewsArticleCard(
                                    article = article,
                                    isSaved = isSaved,
                                    onClick = { selectedArticle = article },
                                    onBookmark = {
                                        if (!isSaved) onBookmarkArticle(article)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Bookmarked / Saved Articles from Firestore
            if (savedArticles.isEmpty()) {
                Box(modifier = Modifier.padding(16.dp)) {
                    EmptyStateCard(
                        icon = Icons.Rounded.BookmarkBorder,
                        title = "कोई सेव समाचार नहीं • No Saved News",
                        subtitle = "किसी भी खबर पर बुकमार्क दबाकर बाद में पढ़ने के लिए सेव करें।"
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(savedArticles, key = { it.id }) { saved ->
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (saved.link.startsWith("http")) {
                                            runCatching {
                                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(saved.link)))
                                            }
                                        }
                                    }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    CategoryBadge(
                                        label = saved.region,
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = saved.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "${saved.source} • ${saved.pubDate}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { onRemoveBookmark(saved.id) }) {
                                    Icon(
                                        imageVector = Icons.Rounded.DeleteOutline,
                                        contentDescription = "Remove bookmark",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val activeArticle = selectedArticle
    if (activeArticle != null) {
        ModalBottomSheet(onDismissRequest = { selectedArticle = null }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                CategoryBadge(
                    label = "${activeArticle.region.hiLabel} • ${activeArticle.source}",
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = activeArticle.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (activeArticle.pubDate.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = activeArticle.pubDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = activeArticle.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (activeArticle.link.startsWith("http")) {
                        Button(
                            onClick = {
                                runCatching {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse(activeArticle.link))
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("पूरा समाचार पढ़ें")
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun NewsArticleCard(
    article: NewsArticle,
    isSaved: Boolean,
    onClick: () -> Unit,
    onBookmark: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryBadge(
                    label = article.source.take(28),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
                IconButton(
                    onClick = onBookmark,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                        contentDescription = "Save article",
                        tint = if (isSaved) MarigoldSaffron else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = article.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (article.pubDate.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = article.pubDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
