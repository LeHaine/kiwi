package com.lehaine.kiwi.korge.filter

import com.soywiz.korag.shader.FragmentShader
import com.soywiz.korag.shader.Uniform
import com.soywiz.korag.shader.VarType
import com.soywiz.korag.shader.storageFor
import com.soywiz.korge.debug.uiEditableValue
import com.soywiz.korge.view.Views
import com.soywiz.korge.view.filter.ShaderFilter
import com.soywiz.korim.color.Colors
import com.soywiz.korim.color.RGBA
import com.soywiz.korui.UiContainer

/**
 * Outlines a view with a pixel outline.
 * @author Colton Daily
 */
class PixelOutlineFilter(color: RGBA = Colors.BLACK) : ShaderFilter() {

    companion object {
        val u_texelSize = Uniform("texelSize", VarType.Float2)
        val u_outlineColor = Uniform("outlineColor", VarType.Float4)

        private val FRAGMENT_SHADER = FragmentShader {
            val currentColor = tex(fragmentCoords)

            // outline color multiplier based on transparent surrounding pixels
            val edge = ((1f.lit - currentColor.a) * max(
                tex(vec2(fragmentCoords.x + u_texelSize.x, fragmentCoords.y)).a, // left
                max(
                    tex(vec2(fragmentCoords.x - u_texelSize.x, fragmentCoords.y)).a, // right
                    max(
                        tex(vec2(fragmentCoords.x, fragmentCoords.y + u_texelSize.y)).a, // top
                        tex(vec2(fragmentCoords.x, fragmentCoords.y - u_texelSize.y)).a // bottom
                    )
                )
            ))
            val a = max(edge * u_outlineColor.a, min(1f.lit, currentColor.a))
            out setTo vec4(mix(currentColor["rgb"], u_outlineColor["rgb"], edge) * a, a)
        }
    }

    private val texelSize = uniforms.storageFor(u_texelSize)
    private val outlineColor = uniforms.storageFor(u_outlineColor)

    var texelSizeX by texelSize.intDelegateX(default = 1)
    var texelSizeY by texelSize.intDelegateY(default = 1)

    var outlineColorR by outlineColor.floatDelegateX(default = color.rf)
    var outlineColorG by outlineColor.floatDelegateY(default = color.gf)
    var outlineColorB by outlineColor.floatDelegateZ(default = color.bf)
    var outlineColorA by outlineColor.floatDelegateW(default = color.af)

    override val fragment: FragmentShader = FRAGMENT_SHADER
    override val border: Int = 1

    init {
        filtering = false
    }

    override fun buildDebugComponent(views: Views, container: UiContainer) {
        container.uiEditableValue(::texelSizeX)
        container.uiEditableValue(::texelSizeY)
        container.uiEditableValue(::outlineColorR)
        container.uiEditableValue(::outlineColorG)
        container.uiEditableValue(::outlineColorB)
        container.uiEditableValue(::outlineColorA)
    }
}