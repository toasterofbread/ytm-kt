package dev.toastbits.ytmkt.model.internal

import kotlinx.serialization.Serializable

@Serializable
data class ItemSectionRenderer(val contents: List<ItemSectionRendererContent>)

@Serializable
data class ItemSectionRendererContent(val didYouMeanRenderer: DidYouMeanRenderer?)

@Serializable
data class DidYouMeanRenderer(val correctedQuery: TextRuns)
