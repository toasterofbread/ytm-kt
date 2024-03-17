package dev.toastbits.ytmkt.model.internal

import kotlinx.serialization.Serializable

@Serializable
data class FlexColumn(val musicResponsiveListItemFlexColumnRenderer: MusicResponsiveListItemColumnRenderer)
@Serializable
data class FixedColumn(val musicResponsiveListItemFixedColumnRenderer: MusicResponsiveListItemColumnRenderer)
@Serializable
data class MusicResponsiveListItemColumnRenderer(val text: TextRuns)
