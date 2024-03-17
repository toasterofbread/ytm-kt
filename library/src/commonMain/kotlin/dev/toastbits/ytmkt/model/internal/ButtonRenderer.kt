package dev.toastbits.ytmkt.model.internal

import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionButton(val subscribeButtonRenderer: SubscribeButtonRenderer)
@Serializable
data class SubscribeButtonRenderer(val subscribed: Boolean, val subscriberCountText: TextRuns, val channelId: String)

@Serializable
data class MoreContentButton(val buttonRenderer: ButtonRenderer)
@Serializable
data class ButtonRenderer(val navigationEndpoint: NavigationEndpoint, val icon: ButtonRendererIcon?)
@Serializable
data class ButtonRendererIcon(val iconType: String)
