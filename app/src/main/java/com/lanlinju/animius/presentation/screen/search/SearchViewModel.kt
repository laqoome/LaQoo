package com.laqoome.laqoo.presentation.screen.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.laqoome.laqoo.domain.model.laqoo
import com.laqoome.laqoo.domain.repository.laqooRepository
import com.laqoome.laqoo.util.SourceHolder
import com.laqoome.laqoo.util.SourceMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: laqooRepository
) : ViewModel() {
    private val _laqoosState: MutableStateFlow<PagingData<laqoo>> =
        MutableStateFlow(value = PagingData.empty())
    val laqoosState: StateFlow<PagingData<laqoo>>
        get() = _laqoosState

    private val _query: MutableStateFlow<String> = MutableStateFlow(value = "")
    val query: StateFlow<String>
        get() = _query

    // 只在第一次进入时请求焦点
    var hasFocusRequest = false

    /**
     * 用于标识使用当前动漫源搜索数据
     *
     * Note: 在Compose 组合函数中，从上一个界面返当前界面时，[remember]保存的数据状态会丢失。
     */
    var currentSourceMode = SourceHolder.currentSourceMode

    fun onSearch(query: String, mode: SourceMode) {
        getSearchData(query, mode)
    }

    fun clearSearchQuery() {
        _query.value = ""
    }

    fun onQuery(query: String) {
        _query.value = query
    }

    fun getSearchData(query: String, mode: SourceMode) {
        if (query.isEmpty()) return

        viewModelScope.launch {
            repository.getSearchData(query, mode)
                .distinctUntilChanged()
                .cachedIn(viewModelScope)
                .collect {
                    _laqoosState.value = it
                }
        }
    }
}