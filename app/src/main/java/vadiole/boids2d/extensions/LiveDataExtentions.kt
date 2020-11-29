package com.namaztime.qibla.tools.extensions

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations


fun <T> LiveData<T>.observe(lifecycleOwner: LifecycleOwner, observer: (T) -> Unit) {
    observe(lifecycleOwner, Observer(observer))
}

fun <T, S> LiveData<T>.map(map: (T) -> S): LiveData<S> = Transformations.map(this, map)

fun <T> MutableLiveData<T>.immutable(): LiveData<T> = this

