package vadiole.boids2d.global.extensions

import androidx.lifecycle.*


fun <T> LiveData<T>.observe(lifecycleOwner: LifecycleOwner, observer: (T) -> Unit) {
    observe(lifecycleOwner, Observer(observer))
}

fun <T, S> LiveData<T>.map(map: (T) -> S): LiveData<S> = Transformations.map(this, map)

fun <T> MutableLiveData<T>.immutable(): LiveData<T> = this

