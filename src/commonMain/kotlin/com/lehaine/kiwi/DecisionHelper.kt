package com.lehaine.kiwi

import com.soywiz.kds.iterators.fastForEach

@PublishedApi
internal data class DecisionElement<T>(val value: T) {
    var score = 0.0
    var out = false
}

class DecisionHelper<T>(initialData: ArrayList<T>) {
    @PublishedApi
    internal val all = arrayListOf<DecisionElement<T>>().apply {
        initialData.forEach {
            add(DecisionElement(it))
        }
    }

    fun reset() {
        all.fastForEach {
            it.out = false
            it.score = 0.0
        }
    }

    inline fun remove(decide: (T) -> Boolean) {
        all.fastForEach {
            if (!it.out && decide(it.value)) {
                it.out = true
            }
        }
    }

    fun removeValue(value: T) {
        all.fastForEach {
            if (!it.out && it.value == value) {
                it.out = true
            }
        }
    }

    inline fun keepOnly(decide: (T) -> Boolean) {
        all.fastForEach {
            if (!it.out && !decide(it.value)) {
                it.out = true
            }
        }
    }

    inline fun score(decideScore: (T) -> Double) {
        all.fastForEach {
            if (!it.out) {
                it.score += decideScore(it.value)
            }
        }
    }

    fun countRemaining(): Int = all.filter { !it.out }.size
    fun isEmpty(): Boolean = all.find { !it.out } != null
    fun isNotEmpty(): Boolean = !isEmpty()

    fun getBest(): T? {
        var best: DecisionElement<T>? = null
        all.fastForEach {
            if (!it.out && (best == null || it.score > best?.score ?: 0.0)) {
                best = it
            }
        }

        return best?.value
    }
}