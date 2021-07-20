package com.lehaine.kiwi.component

/**
 * @author Colton Daily
 * @date 7/20/2021
 */
interface TargetComponent : Component {
    var tx: Int
    var ty: Int

    fun moveTo(tx: Int = -1, ty: Int = -1) {
        this.tx = tx
        this.ty = ty
    }

    fun moveToTarget(dynamicComponent: DynamicComponent, speed: Double = 0.008) {
        val cx = dynamicComponent.cx
        val cy = dynamicComponent.cy

        if (cx == tx) {
            tx = -1
        }

        if (cy == ty) {
            ty = -1
        }

        if (cx != tx && tx != -1) {
            if (tx > cx) {
                dynamicComponent.velocityX += speed
            }
            if (tx < cx) {
                dynamicComponent.velocityX -= speed
            }
        }

        if (cy != ty && ty != -1) {
            if (ty > cy) {
                dynamicComponent.velocityY += speed
            }
            if (ty < cy) {
                dynamicComponent.velocityY -= speed
            }
        }
    }


    companion object {
        operator fun invoke(): TargetComponent {
            return TargetComponentDefault()
        }
    }
}

fun <T> T.moveToTarget(speed: Double = 0.008) where T : TargetComponent, T : DynamicComponent =
    moveToTarget(this, speed)

class TargetComponentDefault() : TargetComponent {
    override var tx: Int = -1
    override var ty: Int = -1
}