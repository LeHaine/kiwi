package com.lehaine.kiwi.component

import com.lehaine.kiwi.component.ext.*
import com.lehaine.kiwi.korge.cooldown
import com.soywiz.kds.iterators.fastForEach
import com.soywiz.klock.TimeSpan
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.addTo
import com.soywiz.korge.view.collidesWithShape
import com.soywiz.korge.view.hitShape
import com.soywiz.korio.lang.Closeable
import com.soywiz.korma.geom.vector.VectorBuilder
import com.soywiz.korma.geom.vector.rect

open class Entity(
    val gridPositionComponent: GridPositionComponent = GridPositionComponentDefault(),
    val scaleComponent: ScaleAndStretchComponent = ScaleAndStretchComponentDefault(),
    val container: Container = Container()
) : UpdatableComponent {
    private val collisionState = mutableMapOf<Entity, Boolean>()

    var enableCollisionChecks = false

    var lastEntityCollided: Entity? = null
        private set

    open val collisionEntities by lazy { listOf<Entity>() }

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
        gridPositionComponent.updateGridPosition(tmod)
    }

    override fun postUpdate(dt: TimeSpan) {
        syncViewPosition()
        scaleComponent.updateStretchAndScale()

        if (enableCollisionChecks) {
            container.run {
                // TODO optimize this to prevent checks against pairs multiple times
                // TODO maybe move away from checking collision with views and use own calculations
                collisionEntities.fastForEach {
                    if (this@Entity != it && it.enableCollisionChecks) {
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
        gridPositionComponent.castRayTo(target, canRayPass)

    fun castRayTo(target: Entity, canRayPass: (Int, Int) -> Boolean) =
        gridPositionComponent.castRayTo(target.gridPositionComponent, canRayPass)

    fun castRayTo(target: GridPositionComponent, level: LevelComponent<*>) =
        gridPositionComponent.castRayTo(target) { cx, cy ->
            !level.hasCollision(cx, cy) || gridPositionComponent.cx == cx && gridPositionComponent.cy == cy
        }

    fun dirTo(target: Entity) = gridPositionComponent.dirTo(target.gridPositionComponent)

    fun distGridTo(target: Entity) =
        gridPositionComponent.distGridTo(
            target.gridPositionComponent.cx,
            target.gridPositionComponent.cy,
            target.gridPositionComponent.xr,
            target.gridPositionComponent.yr
        )

    fun distPxTo(target: Entity) =
        gridPositionComponent.distPxTo(target.gridPositionComponent.px, target.gridPositionComponent.py)

    fun angleTo(target: Entity) =
        gridPositionComponent.angleTo(target.gridPositionComponent.centerX, target.gridPositionComponent.centerY)

    fun toPixelPosition(target: Entity) =
        gridPositionComponent.toPixelPosition(target.gridPositionComponent.px, target.gridPositionComponent.py)

    fun toGridPosition(target: Entity) =
        gridPositionComponent.toGridPosition(
            target.gridPositionComponent.cx,
            target.gridPositionComponent.cy,
            target.gridPositionComponent.xr,
            target.gridPositionComponent.yr
        )

    protected fun syncViewPosition() {
        container.x = gridPositionComponent.px
        container.y = gridPositionComponent.py
    }

    protected inline fun addShape(crossinline block: (VectorBuilder.() -> Unit)) {
        container.hitShape(block)
    }

    protected fun addRectShape(
        anchorX: Double = 0.5,
        anchorY: Double = 0.5
    ) {
        addShape {
            rect(
                gridPositionComponent.width * anchorX,
                gridPositionComponent.height * anchorY,
                gridPositionComponent.width,
                gridPositionComponent.height
            )
        }
    }
}

fun <T : Entity> T.addTo(parent: Container): T {
    container.addTo(parent)
    return this
}

open class SpriteEntity(
    val spriteComponent: SpriteComponent = SpriteComponentDefault(),
    gridPositionComponent: GridPositionComponent = GridPositionComponentDefault(),
    scaleAndStretchComponent: ScaleAndStretchComponent = ScaleAndStretchComponentDefault(),
    container: Container = Container()
) : Entity(gridPositionComponent, scaleAndStretchComponent, container) {

    init {
        container.addChild(spriteComponent.sprite)
    }

    override fun postUpdate(dt: TimeSpan) {
        super.postUpdate(dt)
        syncSprite()
    }

    protected fun syncSprite() {
        val sprite = spriteComponent.sprite
        sprite.scaleX = spriteComponent.dir.toDouble() * scaleComponent.scaleX * scaleComponent.stretchX
        sprite.scaleY = scaleComponent.scaleY * scaleComponent.stretchY
    }
}

open class SpriteLevelEntity(
    open val level: LevelComponent<*>,
    spriteComponent: SpriteComponent = SpriteComponentDefault(),
    levelDynamicComponent: LevelDynamicComponent = LevelDynamicComponentDefault(level),
    scaleComponent: ScaleAndStretchComponent = ScaleAndStretchComponentDefault(),
    container: Container = Container()
) : SpriteEntity(spriteComponent, levelDynamicComponent, scaleComponent, container) {

    override val collisionEntities: List<Entity> by lazy { level.entities }

    init {
        levelDynamicComponent.onLevelCollision = ::onLevelCollision
    }

    override fun destroy() {
        super.destroy()
        level.entities.remove(this)
    }

    open fun onLevelCollision(xDir: Int, yDir: Int) {}

    fun castRayTo(target: GridPositionComponent) = castRayTo(target, level)
    fun castRayTo(target: Entity) = castRayTo(target.gridPositionComponent)
}

fun <T : SpriteLevelEntity> T.addToLevel(): T {
    level.entities += this
    return this
}

open class LevelEntity(
    open val level: LevelComponent<*>,
    levelDynamicComponent: LevelDynamicComponent = LevelDynamicComponentDefault(level),
    scaleAndStretchComponent: ScaleAndStretchComponent = ScaleAndStretchComponentDefault(),
    container: Container = Container()
) : Entity(levelDynamicComponent, scaleAndStretchComponent, container) {

    override val collisionEntities: List<Entity> by lazy { level.entities }

    init {
        levelDynamicComponent.onLevelCollision = ::onLevelCollision
    }

    override fun destroy() {
        super.destroy()
        level.entities.remove(this)
    }

    open fun onLevelCollision(xDir: Int, yDir: Int) {}

    fun castRayTo(target: GridPositionComponent) = castRayTo(target, level)
    fun castRayTo(target: Entity) = castRayTo(target.gridPositionComponent)
}

fun <T : LevelEntity> T.addToLevel(): T {
    level.entities += this
    return this
}

