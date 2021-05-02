package com.lehaine.kiwi.component

import com.soywiz.klock.TimeSpan

interface UpdatableComponent {
    fun update(dt: TimeSpan)
    fun fixedUpdate()
    fun postUpdate(dt: TimeSpan)
}