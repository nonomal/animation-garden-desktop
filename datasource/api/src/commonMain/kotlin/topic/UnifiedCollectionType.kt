package me.him188.ani.datasources.api.topic

import kotlinx.serialization.Serializable
import me.him188.ani.datasources.api.topic.UnifiedCollectionType.NOT_COLLECTED

/**
 * Unified type for all collection types, also added representation for [NOT_COLLECTED].
 */
@Serializable // so don't change field names
enum class UnifiedCollectionType {
    WISH,
    DOING,
    DONE,
    ON_HOLD,
    DROPPED,

    /**
     * The item is not collected.
     *
     * This is a dummy type that will not be returned from the server,
     * as when the item is not collected, the server simply won't return the item.
     *
     * [NOT_COLLECTED] can be useful to represent the "Delete" action to be received as an argument
     * e.g. to update the user's collection type.
     */
    NOT_COLLECTED,
}

fun UnifiedCollectionType.isDoneOrDropped(): Boolean {
    return this == UnifiedCollectionType.DONE || this == UnifiedCollectionType.DROPPED
}