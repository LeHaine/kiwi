package com.lehaine.kiwi.component

import com.lehaine.kiwi.component.ext.*
import com.lehaine.kiwi.korge.cooldown
import com.lehaine.kiwi.korge.view.ComponentContainer
import com.lehaine.kiwi.korge.view.Layers
import com.lehaine.kiwi.korge.view.addToLayer
import com.soywiz.kds.iterators.fastForEach
import com.soywiz.klock.TimeSpan
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.addTo

open class Entity(
    open val game: GameComponent,
    val gridPositionComponent: GridPositionComponent = GridPositionComponentDefault(),
    val scaleComponent: ScaleAndStretchComponent = ScaleAndStretchComponentDefault(),
    val container: Container = ComponentContainer(listOf(gridPositionComponent, scaleComponent))
) : UpdatableComponent {
    private val collisionState = mutableMapOf<Entity, Boolean>()
    private val collisionPairs = hashSetOf<Entity>()
    private val staticCollisionPairs = hashSetOf<Entity>()

    var enableCollisionChecks = false

    var lastEntityCollided: Entity? = null
        private set

    val collisionEntities: List<Entity> by lazy { game.entities }
    val staticCollisionEntities: List<Entity> by lazy { game.staticEntities }

    var destroyed = false
        private set

    var onDestroyedCallback: ((Entity) -> Unit)? = null

    val level get() = game.level

    val cooldown get() = container.cooldown
    val cd get() = cooldown

    var static = false

    init {
        gridPositionComponent.updateGridPosition()
        syncViewPosition()

        gridPositionComponent.preXCheck = {
            checkAndResolveStaticCollisions(true)
        }

        gridPositionComponent.preYCheck = {
            checkAndResolveStaticCollisions(false)
        }
    }

    override fun update(dt: TimeSpan) {
        gridPositionComponent.fixedProgressionRatio = game.fixedProgressionRatio
    }

    override fun fixedUpdate() {
        gridPositionComponent.updateGridPosition()
    }

    override fun postUpdate(dt: TimeSpan) {
        syncViewPosition()
        scaleComponent.updateStretchAndScale(dt)
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
                            staticCollisionPairs.add(it)
                            it.staticCollisionPairs.add(this@Entity)
                        } else if (collisionState[it] == true && !resolveXDepth) {
                            onCollisionExit(it)
                            it.onCollisionExit(this@Entity)
                            collisionState[it] = false
                            it.collisionState[this@Entity] = false
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

            // store old and restore x grid position after setting pixel position
            // to prevent moving the entity to the next grid cell if a level collision has
            // already occurred
            val oldCy = pos.cy
            val oldYr = pos.yr
            pos.toPixelPosition(
                pos.px - xDepth,
                pos.py
            )
            pos.cy = oldCy
            pos.yr = oldYr
        } else {
            val yDepth = if (pos.top < staticPos.top) {
                pos.bottom - staticPos.top
            } else {
                pos.top - staticPos.bottom
            }

            // store old and restore y grid position after setting pixel position
            // to prevent moving the entity to the next grid cell if a level collision has
            // already occurred
            val oldCx = pos.cx
            val oldXr = pos.xr
            pos.toPixelPosition(
                pos.px,
                pos.py - yDepth
            )
            pos.cx = oldCx
            pos.xr = oldXr
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
        game.entities.remove(this)
        if (static) {
            game.staticEntities.remove(this)
        }
        onDestroyedCallback?.invoke(this)


    }

    fun onDestroy(action: (Entity) -> Unit) {
        onDestroyedCallback = action
    }

    open fun onCollisionEnter(entity: Entity) {}
    open fun onCollisionUpdate(entity: Entity) {}
    open fun onCollisionExit(entity: Entity) {}

    fun cooldown(name: String, time: TimeSpan, callback: () -> Unit = {}) =
        cooldown.timeout(name, time, callback)

    fun cd(name: String, time: TimeSpan, callback: () -> Unit = {}) =
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

fun <T : Entity> T.addToLayer(parent: Layers, layer: Int): T {
    container.addToLayer(parent, layer)
    return this
}

fun <T : Entity> T.addToGame(): T {
    game.entities += this
    if (static) {
        game.staticEntities += this
    }
    return this
}

open class SpriteEntity(
    game: GameComponent,
    val spriteComponent: SpriteComponent = SpriteComponentDefault(),
    gridPositionComponent: GridPositionComponent = GridPositionComponentDefault(),
    scaleAndStretchComponent: ScaleAndStretchComponent = ScaleAndStretchComponentDefault(),
    container: Container = ComponentContainer(listOf(spriteComponent, gridPositionComponent, scaleAndStretchComponent))
) : Entity(game, gridPositionComponent, scaleAndStretchComponent, container) {

    init {
        container.addChild(spriteComponent.sprite)
    }

    override fun postUpdate(dt: TimeSpan) {
        syncViewPosition()
        syncSprite()
        scaleComponent.updateStretchAndScale(dt)
        checkCollisions()
    }

    protected fun syncSprite() {
        spriteComponent.sprite.scaleX = spriteComponent.dir.toDouble() * scaleComponent.scaleX * scaleComponent.stretchX
        spriteComponent.sprite.scaleY = scaleComponent.scaleY * scaleComponent.stretchY
    }
}

open class SpriteLevelEntity(
    game: GameComponent,
    spriteComponent: SpriteComponent = SpriteComponentDefault(),
    levelDynamicComponent: LevelDynamicComponent = LevelDynamicComponentDefault(game.level),
    scaleComponent: ScaleAndStretchComponent = ScaleAndStretchComponentDefault(),
    container: Container = ComponentContainer(listOf(spriteComponent, levelDynamicComponent, scaleComponent))
) : SpriteEntity(game, spriteComponent, levelDynamicComponent, scaleComponent, container) {


    init {
        levelDynamicComponent.onLevelCollision = ::onLevelCollision
    }


    open fun onLevelCollision(xDir: Int, yDir: Int) {}

    fun castRayTo(target: GridPositionComponent) = castRayTo(target, level)
    fun castRayTo(target: Entity) = castRayTo(target.gridPositionComponent)
}


open class LevelEntity(
    game: GameComponent,
    levelDynamicComponent: LevelDynamicComponent = LevelDynamicComponentDefault(game.level),
    scaleAndStretchComponent: ScaleAndStretchComponent = ScaleAndStretchComponentDefault(),
    container: Container = ComponentContainer(listOf(game, levelDynamicComponent, scaleAndStretchComponent))
) : Entity(game, levelDynamicComponent, scaleAndStretchComponent, container) {


    init {
        levelDynamicComponent.onLevelCollision = ::onLevelCollision
    }


    open fun onLevelCollision(xDir: Int, yDir: Int) {}

    fun castRayTo(target: GridPositionComponent) = castRayTo(target, level)
    fun castRayTo(target: Entity) = castRayTo(target.gridPositionComponent)
}

