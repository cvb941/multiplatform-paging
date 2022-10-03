package com.kuuurt.paging.multiplatform

/**
 * Copyright 2021, Kurt Renzo Acosta, All rights reserved.
 *
 * @author Kurt Renzo Acosta
 * @since 02/08/2021
 */
data class PagingResult<K, V>(
    val items: List<V>,
    val currentKey: K,
    val prevKey: () -> K?,
    val nextKey: () -> K?,
    val itemsBefore: Int = COUNT_UNDEFINED,
    val itemsAfter: Int = COUNT_UNDEFINED
) {
    companion object {
        const val COUNT_UNDEFINED = Int.MIN_VALUE
    }
}
