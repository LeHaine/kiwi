package com.lehaine.kiwi.component

import com.soywiz.klock.TimeSpan

interface UpdatableComponent {
    fun update(dt: TimeSpan)
    fun postUpdate(dt: TimeSpan)
}