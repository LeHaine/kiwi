package com.lehaine.kiwi

import com.lehaine.kiwi.component.Entity
import com.soywiz.kds.Deque
import com.soywiz.klock.TimeSpan
import com.soywiz.klock.milliseconds
import com.soywiz.korge.view.View

/**
 * @author Colton Daily
 * @date 7/20/2021
 */


@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class EntityAnimatorDslMarker

interface BaseEntityAnimatorNode {
    var time: TimeSpan
    var executed: Boolean
    val executeUntil: () -> Boolean
    fun execute(dt: TimeSpan)
    fun isDoneExecuting() = executed && executeUntil() && (time <= 0.milliseconds || time == TimeSpan.NIL)
}

open class EntityAnimator(
    val init: EntityAnimator.() -> Unit = {}
) : BaseEntityAnimatorNode {

    private var initialized = false

    @PublishedApi
    internal val nodes = Deque<BaseEntityAnimatorNode>()

    override var time: TimeSpan = TimeSpan.NIL
    override var executed: Boolean = false
    override val executeUntil: () -> Boolean = { nodes.isEmpty() }

    override fun execute(dt: TimeSpan) {
        if (!initialized) {
            init(this)
            initialized = true
            executed = true
        }
        while (nodes.isNotEmpty()) {
            val node = nodes.first
            if (!node.executed) {
                node.execute(dt)
                node.executed = true
            }
            if (node.isDoneExecuting()) {
                nodes.removeFirst()
            } else {
                if (node.time != TimeSpan.NIL) {
                    node.time -= dt
                }
                break
            }
        }
    }

    fun waitFor(condition: () -> Boolean) {
        block(untilCondition = condition)
    }

    fun wait(time: TimeSpan) {
        block(time = time)
    }

    fun block(
        untilCondition: () -> Boolean = { true },
        time: TimeSpan = TimeSpan.NIL,
        callback: () -> Unit = {}
    ) {
        nodes.add(object : BaseEntityAnimatorNode {
            override var time: TimeSpan = time
            override var executed: Boolean = false
            override val executeUntil = untilCondition
            override fun execute(dt: TimeSpan) = callback()
        })
    }
}


fun View.entityAnimator(
    block: @EntityAnimatorDslMarker EntityAnimator.() -> Unit = {}
): EntityAnimator = EntityAnimator().apply(block)

fun Entity.entityAnimator(
    block: @EntityAnimatorDslMarker EntityAnimator.() -> Unit = {}
): EntityAnimator = EntityAnimator().apply(block)
