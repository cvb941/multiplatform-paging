package com.kuuurt.paging.multiplatform

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

/**
 *  Coroutine scope valid until the next refresh/invalidation.
 */
class GetItemsScope(
    override val coroutineContext: CoroutineContext,
    /**
     *  Call in order to trigger data refresh/invalidation.
     */
    val refresh: () -> Unit,
) : CoroutineScope
