package com.lehaine.kiwi.component

import com.lehaine.kiwi.component.ext.*
import com.lehaine.kiwi.korge.cooldown
import com.soywiz.kds.iterators.fastForEach
import com.soywiz.klock.TimeSpan
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.addTo
import com.soywiz.korio.lang.Closeable

open class Entity(
    val gridPositionComponent: GridPositionComponent = GridPositionComponentDefault(),
    val scaleComponent: ScaleAndStretchComponent = ScaleAndStretchComponentDefault(),
    val container: Container = Container()
) : UpdatableComponent {
    private val collisionState = mutableMapOf<Entity, Boolean>()
    private val collisionPairs = hashSetOf<Entity>()
    private val staticCollisionPairs = hashSetOf<Entity>()

    var enableCollisionChecks = false

    var lastEntityCollided: Entity? = null
        private set

    open val collisionEntities by lazy { listOf<Entity>() }
    open val staticCollisionEntities by lazy { listOf<Entity>() }

    var destroyed = false
        private set

    var onDestroyedCallback: ((Entity) -> Unit)? = null

    val cooldown get() = container.cooldown
    val cd get() = cooldown

    var static = false

    init {
        syncViewPosition()
        gridPositionComponent.preXCheck = {
            checkAndResolveStaticCollisions(true)
        }

        gridPositionComponent.preYCheck = {
            checkAndResolveStaticCollisions(false)
        }
    }

    override var tmod: Double = 1.0

    override fun update(dt: TimeSpan) {
        gridPositionComponent.updateGridPosition(tmod)
    }

    override fun postUpdate(dt: TimeSpan) {
        syncViewPosition()
        scaleComponent.updateStretchAndScale(tmod)
        checkCollisions()
    }

    protected fun checkAndResolveStaticCollisions(resolveXDepth: Boolean) {
        if (enableCollisionChecks && !static) {
            if (resolveXDepth) staticCollisionPairs.clear()
            container.run {
                staticCollisionEntities.fastForEach {
                    if (this@Entity != it
                        && it.enableCollisionChecks
                        && it.static // being redundant
                        && !staticCollisionPairs.contains(it)
                    ) {
                        if ((resolveXDepth && isCollidingWith(it)) || (!resolveXDepth && isCollidingWith(it))) {
                            // we check x first then y which allows us to determine which side the collision happened on
                            resolveCollision(this@Entity, it, resolveXDepth)
                            staticCollisionPairs.add(it)
                            it.staticCollisionPairs.add(this@Entity)
                        }
                    }
                }
            }
        }
    }

    private fun resolveCollision(a: Entity, b: Entity, resolveXDepth: Boolean) {
        if (a.static && b.static) return
        val static = if (a.static) a else b
        val noStatic = if (a.static) b else a
        val pos = noStatic.gridPositionComponent
        val staticPos = static.gridPositionComponent

        if (resolveXDepth) {
            val xDepth = if (pos.left < staticPos.left) {
                pos.right - staticPos.left
            } else {
                pos.left - staticPos.right
            }

            pos.toPixelPosition(
                pos.px - xDepth,
                pos.py
            )
        } else {
            val yDepth = if (pos.top < staticPos.top) {
                pos.bottom - staticPos.top
            } else {
                pos.top - staticPos.bottom
            }
            pos.toPixelPosition(
                pos.px,
                pos.py - yDepth
            )
        }
    }

    protected fun checkCollisions() {
        if (enableCollisionChecks) {
            container.run {
                collisionPairs.clear() // TODO fix clearing collision pairs from removing paired collision
                collisionEntities.fastForEach {
                    if (this@Entity != it
                        && it.enableCollisionChecks
                        && !collisionPairs.contains(it)
                    ) {
                        if (isCollidingWith(it)) {
                            if (collisionState[it] == true) {
                                onCollisionUpdate(it)
                                it.onCollisionUpdate(this@Entity)
                            } else {
                                // we only need to call it once
                                onCollisionEnter(it)
                                it.onCollisionEnter(this@Entity)
                                collisionState[it] = true
                                it.collisionState[this@Entity] = true
                            }
                            lastEntityCollided = it
                            it.lastEntityCollided = this@Entity
                        } else if (collisionState[it] == true) {
                            onCollisionExit(it)
                            it.onCollisionExit(this@Entity)
                            collisionState[it] = false
                            it.collisionState[this@Entity] = false
                        }

                        collisionPairs.add(it)
                        it.collisionPairs.add(this@Entity)
                    }
                }
            }
        }
    }


    /**
     * AABB check
     */
    fun isCollidingWith(from: Entity): Boolean {
        val lx = gridPositionComponent.left
        val lx2 = from.gridPositionComponent.left
        val rx = gridPositionComponent.right
        val rx2 = from.gridPositionComponent.right

        if (lx >= rx2 || lx2 >= rx) {
            return false
        }

        val ly = gridPositionComponent.top
        val ry = gridPositionComponent.bottom
        val ly2 = from.gridPositionComponent.top
        val ry2 = from.gridPositionComponent.bottom

        if (ly >= ry2 || ly2 >= ry) {
            return false
        }

        return true
    }

    fun isCollidingWithInnerCircle(from: Entity) = distPxTo(from) <= gridPositionComponent.innerRadius
    fun isCollidingWithOuterCircle(from: Entity) = distPxTo(from) <= gridPositionComponent.outerRadius

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
        syncViewPosition()
        syncSprite()
        scaleComponent.updateStretchAndScale(tmod)
        checkCollisions()
    }

    protected fun syncSprite() {
        spriteComponent.sprite.scaleX = spriteComponent.dir.toDouble() * scaleComponent.scaleX * scaleComponent.stretchX
        spriteComponent.sprite.scaleY = scaleComponent.scaleY * scaleComponent.stretchY
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
    override val staticCollisionEntities: List<Entity> by lazy { level.staticEntities }

    init {
        levelDynamicComponent.onLevelCollision = ::onLevelCollision
    }

    override fun destroy() {
        super.destroy()
        level.entities.remove(this)
        if (static) {
            level.staticEntities.remove(this)
        }
    }

    open fun onLevelCollision(xDir: Int, yDir: Int) {}

    fun castRayTo(target: GridPositionComponent) = castRayTo(target, level)
    fun castRayTo(target: Entity) = castRayTo(target.gridPositionComponent)
}

fun <T : SpriteLevelEntity> T.addToLevel(): T {
    level.entities += this
    if (static) {
        level.staticEntities += this
    }
    return this
}

open class LevelEntity(
    open val level: LevelComponent<*>,
    levelDynamicComponent: LevelDynamicComponent = LevelDynamicComponentDefault(level),
    scaleAndStretchComponent: ScaleAndStretchComponent = ScaleAndStretchComponentDefault(),
    container: Container = Container()
) : Entity(levelDynamicComponent, scaleAndStretchComponent, container) {

    override val collisionEntities: List<Entity> by lazy { level.entities }
    override val staticCollisionEntities: List<Entity> by lazy { level.staticEntities }

    init {
        levelDynamicComponent.onLevelCollision = ::onLevelCollision
    }

    override fun destroy() {
        super.destroy()
        level.entities.remove(this)
        if (static) {
            level.staticEntities.remove(this)
        }
    }

    open fun onLevelCollision(xDir: Int, yDir: Int) {}

    fun castRayTo(target: GridPositionComponent) = castRayTo(target, level)
    fun castRayTo(target: Entity) = castRayTo(target.gridPositionComponent)
}

fun <T : LevelEntity> T.addToLevel(): T {
    level.entities += this
    if (static) {
        level.staticEntities += this
    }
    return this
}

