package dev.toastbits.ytmkt.model

interface ApiImplementable {
    fun isImplemented(): Boolean = true

    fun getIdentifier(): String =
        this::class.simpleName ?: this::class.toString()

    fun getNotImplementedMessage(): String =
        "Implementable not implemented:\n${getIdentifier()}"

    fun getNotImplementedException(): NotImplementedError =
        NotImplementedError(getNotImplementedMessage())
}

fun <T: ApiImplementable> T.implementedOrNull(): T? =
    if (isImplemented()) this else null
