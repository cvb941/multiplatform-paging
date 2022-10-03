package com.kuuurt.paging.multiplatform.helpers

import androidx.paging.PagingSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext

abstract class CoroutineScopedPagingSource<Key : Any, Value : Any> : androidx.paging.PagingSource<Key, Value>(), CoroutineScope {
    override val coroutineContext: CoroutineContext = SupervisorJob()

    init {
        registerInvalidatedCallback {
            coroutineContext.cancel(CancellationException("PagingSource invalidated"))
        }
    }
}
