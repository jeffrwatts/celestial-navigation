package com.jeffrwatts.celestialnavigation.sights

/**
 * Used with the filter spinner in the tasks list.
 */
enum class SightsFilterType {
    /**
     * Do not filter tasks.
     */
    ALL_SIGHTS,

    /**
     * Filters only the active (not completed yet) tasks.
     */
    ACTIVE_SIGHTS
}