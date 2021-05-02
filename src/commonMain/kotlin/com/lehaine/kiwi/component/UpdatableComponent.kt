package com.lehaine.kiwi.component

import com.soywiz.klock.TimeSpan

interface UpdatableComponent {
    fun update(tmod: Double)
    fun postUpdate(tmod: Double)
}