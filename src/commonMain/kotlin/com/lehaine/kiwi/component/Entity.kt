package com.lehaine.kiwi.component

import com.lehaine.kiwi.component.ext.castRayTo
import com.lehaine.kiwi.korge.cooldown
import com.soywiz.kds.iterators.fastForEach
import com.soywiz.klock.TimeSpan
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.collidesWithShape
import com.soywiz.korge.view.hitShape
import com.soywiz.korio.lang.Closeable
import com.soywiz.korma.geom.vector.VectorBuilder
import com.soywiz.korma.geom.vector.rect

open class Entity(
    val position: GridPositionComponent = GridPositionComponentDefault(),
    val scale: ScaleAndStretchComponent = ScaleAndStretchComponentDefault(),
    val container: Container = Container()
) : UpdatableComponent {
    private val collisionState = mutableMapOf<Entity, Boolean>()

    var enableCollisionChecks = false

    var lastEntityCollided: Entity? = null
        private set

    var collisionEntities: List<Entity> = listOf()
        set(value) {
            field = value
            collisionState.clear()
        }

    var destroyed = false
        private set

    var onDestroyedCallback: ((Entity) -> Unit)? = null

    val cooldown get() = container.cooldown
    val cd get() = cooldown

    init {
        syncViewPosition()
    }

    override var tmod: Double = 1.0

    override fun update(dt: TimeSpan) {
        position.updateGridPosition(tmod)
    }

    override fun postUpdate(dt: TimeSpan) {
        syncViewPosition()
        scale.updateStretchAndScale()

        if (enableCollisionChecks) {
            container.run {
                // TODO optimize this to prevent checks against pairs multiple times
                // TODO maybe move away from checking collision with views and use own calculations
                collisionEntities.fastForEach {
                    if (this@Entity != it) {
                        if (collidesWithShape(it.container)) {
                            if (collisionState[it] == true) {
                                onCollisionUpdate(it)
                            } else {
                                // we only need to call it once
                                onCollisionEnter(it)
                                collisionState[it] = true
                            }
                            lastEntityCollided = it
                        } else if (collisionState[it] == true) {
                            onCollisionExit(it)
                            collisionState[it] = false
                        }
                    }
                }
            }
        }
    }


    open fun destroy() {
        if (destroyed) return

        destroyed = true
        container.removeFromParent()
        onDestroyedCallback?.invoke(this)
    }

    fun onDestroy(action: (Entity) -> Unit) {
        onDestroyedCallback = action
    }

    open fun onCollisionEnter(entity: Entity) {}
    open fun onCollisionUpdate(entity: Entity) {}
    open fun onCollisionExit(entity: Entity) {}

    fun cooldown(name: String, time: TimeSpan, callback: () -> Unit = {}): Closeable =
        cooldown.timeout(name, time, callback)

    fun cd(name: String, time: TimeSpan, callback: () -> Unit = {}): Closeable =
        cooldown(name, time, callback)

    fun castRayTo(target: GridPositionComponent, canRayPass: (Int, Int) -> Boolean) =
        position.castRayTo(target, canRayPass)

    fun castRayTo(target: Entity, canRayPass: (Int, Int) -> Boolean) =
        position.castRayTo(target.position, canRayPass)

    fun castRayTo(target: GridPositionComponent, level: LevelComponent<*>) =
        position.castRayTo(target) { cx, cy ->
            !level.hasCollision(cx, cy) || position.cx == cx && position.cy == cy
        }

    protected fun syncViewPosition() {
        container.x = position.px
        container.y = position.py
    }

    protected inline fun addShape(crossinline block: (VectorBuilder.() -> Unit)) {
        container.hitShape(block)
    }

    protected fun addRectShape(
        anchorX: Double = 0.5,
        anchorY: Double = 0.5
    ) {
        addShape { rect(position.width * anchorX, position.height * anchorY, position.width, position.height) }
    }
}

open class SpriteEntity(
    val sprite: SpriteComponent = SpriteComponentDefault(),
    position: GridPositionComponent = GridPositionComponentDefault(),
    scale: ScaleAndStretchComponent = ScaleAndStretchComponentDefault(),
    container: Container = Container()
) : Entity(position, scale, container) {

    init {
        container.addChild(sprite.sprite)
    }

    override fun postUpdate(dt: TimeSpan) {
        super.postUpdate(dt)
        syncSprite()
    }

    protected fun syncSprite() {
        val sprite = sprite.sprite
        sprite.scaleX = this.sprite.dir.toDouble() * scale.scaleX * scale.stretchX
        sprite.scaleY = scale.scaleY * scale.stretchY
    }
}


open class SpriteLevelEntity(
    val level: LevelComponent<*>,
    sprite: SpriteComponent = SpriteComponentDefault(),
    position: GridPositionComponent = GridPositionComponentDefault(),
    scale: ScaleAndStretchComponent = ScaleAndStretchComponentDefault(),
    container: Container = Container()
) : SpriteEntity(sprite, position, scale, container) {

    init {
        collisionEntities = level.entities
    }

    override fun destroy() {
        super.destroy()
        level.entities.remove(this)
    }

    fun castRayTo(target: GridPositionComponent) = castRayTo(target, level)
    fun castRayTo(target: Entity) = castRayTo(target.position)
}

open class LevelEntity(
    val level: LevelComponent<*>,
    position: GridPositionComponent = GridPositionComponentDefault(),
    scale: ScaleAndStretchComponent = ScaleAndStretchComponentDefault(),
    container: Container = Container()
) : Entity(position, scale, container) {

    init {
        collisionEntities = level.entities
    }

    override fun destroy() {
        super.destroy()
        level.entities.remove(this)
    }

    fun castRayTo(target: GridPositionComponent) = castRayTo(target, level)
    fun castRayTo(target: Entity) = castRayTo(target.position)
}
