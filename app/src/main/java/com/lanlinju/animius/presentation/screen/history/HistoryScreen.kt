package com.laqoome.laqoo.presentation.screen.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.laqoome.laqoo.R
import com.laqoome.laqoo.domain.model.History
import com.laqoome.laqoo.presentation.component.BackTopAppBar
import com.laqoome.laqoo.presentation.component.LoadingIndicator
import com.laqoome.laqoo.presentation.component.PopupMenuListItem
import com.laqoome.laqoo.presentation.component.SourceBadge
import com.laqoome.laqoo.presentation.component.StateHandler
import com.laqoome.laqoo.util.CROSSFADE_DURATION
import com.laqoome.laqoo.util.LOW_CONTENT_ALPHA
import com.laqoome.laqoo.util.SourceMode
import com.laqoome.laqoo.util.VIDEO_ASPECT_RATIO

@Composable
fun HistoryScreen(
    onBackClick: () -> Unit,
    onNavigateTolaqooDetail: (detailUrl: String, mode: SourceMode) -> Unit,
) {
    val viewModel: HistoryViewModel = hiltViewModel()
    val historyListState by viewModel.historyList.collectAsState()

    StateHandler(state = historyListState,
        onLoading = { LoadingIndicator() },
        onFailure = {}
    ) { resource ->
        resource.data?.let { histories ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .navigationBarsPadding()
            ) {
                BackTopAppBar(
                    title = stringResource(id = R.string.play_history),
                    onBackClick = onBackClick
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.small_padding))
                ) {
                    items(histories) { history ->
                        PopupMenuListItem(
                            menuText = stringResource(id = R.string.delete),
                            onClick = {
                                onNavigateTolaqooDetail(history.detailUrl, history.sourceMode)
                            },
                            onMenuItemClick = { viewModel.deleteHistory(history.detailUrl) }
                        ) {
                            HistoryItem(history = history)
                        }
                    }
                }
            }
        }
    }

}

@Composable
fun HistoryItem(
    modifier: Modifier = Modifier,
    history: History,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(dimensionResource(id = R.dimen.history_item_height)),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = dimensionResource(id = R.dimen.small_padding),
                vertical = 4.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.small_padding))
        ) {
            SourceBadge(
                text = history.sourceMode.name,
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(history.imgUrl)
                        .crossfade(CROSSFADE_DURATION)
                        .build(),
                    contentDescription = stringResource(id = R.string.lbl_laqoo_img),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(VIDEO_ASPECT_RATIO)
                        .clip(RoundedCornerShape(dimensionResource(id = R.dimen.history_cover_radius)))
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = history.title,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 2,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = history.lastEpisodeName,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = LOW_CONTENT_ALPHA),
                        style = MaterialTheme.typography.bodySmall,
                    )

                    Text(
                        text = history.time,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = LOW_CONTENT_ALPHA),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

            }
        }
    }

}
