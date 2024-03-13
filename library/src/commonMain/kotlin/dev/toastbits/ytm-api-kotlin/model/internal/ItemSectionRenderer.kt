package dev.toastbits.ytmapi.model.internal

data class ItemSectionRenderer(val contents: List<ItemSectionRendererContent>)
data class ItemSectionRendererContent(val didYouMeanRenderer: DidYouMeanRenderer?)
data class DidYouMeanRenderer(val correctedQuery: TextRuns)
