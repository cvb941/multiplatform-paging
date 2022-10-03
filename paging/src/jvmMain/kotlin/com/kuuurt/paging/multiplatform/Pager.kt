package com.kuuurt.paging.multiplatform

import androidx.paging.PagingState
import com.kuuurt.paging.multiplatform.helpers.CoroutineScopedPagingSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import androidx.paging.Pager as AndroidXPager

/**
 * Copyright 2020, Kurt Renzo Acosta, All rights reserved.
 *
 * @author Kurt Renzo Acosta
 * @since 06/11/2020
 */

@FlowPreview
@ExperimentalCoroutinesApi
actual class Pager<K : Any, V : Any> actual constructor(
    clientScope: CoroutineScope,
    config: PagingConfig,
    initialKey: K,
    refreshKey: (Int) -> K?,
    getItems: suspend GetItemsScope.(K, Int) -> PagingResult<K, V>
) {
    actual val pagingData: Flow<PagingData<V>> = AndroidXPager(
        config = config,
        pagingSourceFactory = {
            PagingSource(
                initialKey,
                refreshKey,
                getItems
            )
        }
    ).flow

    class PagingSource<K : Any, V : Any>(
        private val initialKey: K,
        private val refreshKey: (Int) -> K?,
        private val getItems: suspend GetItemsScope.(K, Int) -> PagingResult<K, V>
    ) : CoroutineScopedPagingSource<K, V>() {

        override val jumpingSupported: Boolean = true

//        override val keyReuseSupported: Boolean
//            get() = true

        private val getItemsScope = GetItemsScope(this.coroutineContext) { invalidate() }

        override fun getRefreshKey(state: PagingState<K, V>): K? {
            val position = ((state.anchorPosition ?: 0) - (state.config.initialLoadSize) / 2).coerceAtLeast(0)
            return refreshKey(position)
        }

        override suspend fun load(params: LoadParams<K>): LoadResult<K, V> {
            val currentKey = params.key ?: initialKey
            return try {
                val pagingResult = getItems(getItemsScope, currentKey, params.loadSize)
                LoadResult.Page(
                    data = pagingResult.items,
                    prevKey = pagingResult.prevKey,
                    nextKey = pagingResult.nextKey,
                    itemsBefore = pagingResult.itemsBefore,
                    itemsAfter = pagingResult.itemsAfter,
                )
            } catch (exception: Exception) {
                return LoadResult.Error(exception)
            }
        }
    }
}
