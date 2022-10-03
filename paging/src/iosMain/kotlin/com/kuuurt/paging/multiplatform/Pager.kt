package com.kuuurt.paging.multiplatform

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull

/**
 * Copyright 2020, Kurt Renzo Acosta, All rights reserved.
 *
 * @author Kurt Renzo Acosta
 * @since 06/11/2020
 */

@OptIn(ExperimentalCoroutinesApi::class)
actual class Pager<K : Any, V : Any> actual constructor(
    private val clientScope: CoroutineScope,
    private val config: PagingConfig,
    private val initialKey: K,
    private val refreshKey: (Int) -> K?,
    private val getItems: suspend GetItemsScope.(K, Int) -> PagingResult<K, V>
) {

    private val _pagingData = MutableStateFlow<PagingData<V>?>(null)
    actual val pagingData: Flow<PagingData<V>> = _pagingData.filterNotNull()

    private val _pagingState = MutableStateFlow<PagingState>(PagingState.Success)
    val pagingState: Flow<PagingState> = _pagingState.filterNotNull()

    private val _hasNextPage = MutableStateFlow(true)
    val hasNextPage: Boolean
        get() = _hasNextPage.value

    private val currentPagingResult: MutableStateFlow<PagingResult<K, V>?> = MutableStateFlow(null)

    private var job = SupervisorJob()

    init {
        loadNext()
    }

    fun refresh() {
        currentPagingResult.value = null
        _hasNextPage.value = true
        job.cancel()
        job = SupervisorJob()
        loadNext()
    }

    fun loadPrevious() {
        loadItems(LoadDirection.PREVIOUS)
    }

    fun loadNext() {
        loadItems(LoadDirection.NEXT)
    }

    private fun loadItems(loadDirection: LoadDirection) {
        val pagingResult = currentPagingResult.value
        val key = if (pagingResult == null) {
            initialKey
        } else {
            when (loadDirection) {
                LoadDirection.NEXT -> pagingResult.nextKey
                LoadDirection.PREVIOUS -> pagingResult.prevKey
            }
        }

        if (key != null && hasNextPage) {
            clientScope.launch(CoroutineExceptionHandler { _, throwable ->
                _pagingState.value = PagingState.Error(throwable)
            }) {
                _pagingState.value = if (currentPagingResult.value?.items?.isEmpty() == true) {
                    PagingState.LoadingInitial
                } else {
                    PagingState.LoadingMore
                }
                val getItemsScope = GetItemsScope(job) {
                    refresh()
                }
                val newPagingResult = getItems(getItemsScope, key, config.pageSize)
                _pagingData.value = _pagingData.value?.toMutableList()?.apply {
                    addAll(newPagingResult.items)
                }?.toPagingData() ?: newPagingResult.items.toPagingData()
                _hasNextPage.value = newPagingResult.items.size >= config.pageSize
                currentPagingResult.value = newPagingResult
                _pagingState.value = PagingState.Success
            }
        }
    }

    enum class LoadDirection {
        PREVIOUS,
        NEXT
    }
}

sealed interface PagingState {
    object Success : PagingState
    object LoadingMore : PagingState
    object LoadingInitial : PagingState
    data class Error(val error: Throwable) : PagingState
}
