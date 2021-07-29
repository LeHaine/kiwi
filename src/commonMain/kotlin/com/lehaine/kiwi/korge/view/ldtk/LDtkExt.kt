package com.lehaine.kiwi.korge.view.ldtk

import com.lehaine.kiwi.korge.view.Layers
import com.lehaine.kiwi.korge.view.addToLayer
import com.lehaine.ldtk.LayerAutoLayer
import com.lehaine.ldtk.LayerIntGridAutoLayer
import com.lehaine.ldtk.LayerTiles
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.addTo

/**
 * @author Colton Daily
 * @date 7/29/2021
 */


inline fun LayerTiles.toLDtkLayer(
    container: Container,
    level: LDtkLevel,
    callback: LDtkLayerView.() -> Unit = {}
) =
    LDtkLayerView(this, level.tileSets[this.tileset.json.uid]).addTo(container, callback)

inline fun LayerTiles.toLDtkLayer(
    layers: Layers,
    level: LDtkLevel,
    layerIdx: Int = 0,
    callback: LDtkLayerView.() -> Unit = {}
) =
    LDtkLayerView(this, level.tileSets[this.tileset.json.uid]).addToLayer(layers, layerIdx, callback)

inline fun LayerAutoLayer.toLDtkLayer(
    container: Container,
    level: LDtkLevel,
    callback: LDtkLayerView.() -> Unit = {}
) =
    LDtkLayerView(this, level.tileSets[this.tileset.json.uid]).addTo(container, callback)

inline fun LayerAutoLayer.toLDtkLayer(
    layers: Layers,
    level: LDtkLevel,
    layerIdx: Int = 0,
    callback: LDtkLayerView.() -> Unit = {}
) =
    LDtkLayerView(this, level.tileSets[this.tileset.json.uid]).addToLayer(layers, layerIdx, callback)

inline fun LayerIntGridAutoLayer.toLDtkLayer(
    container: Container,
    level: LDtkLevel,
    callback: LDtkLayerView.() -> Unit = {}
) =
    LDtkLayerView(this, level.tileSets[this.tileset.json.uid]).addTo(container, callback)

inline fun LayerIntGridAutoLayer.toLDtkLayer(
    layers: Layers,
    level: LDtkLevel,
    layerIdx: Int = 0,
    callback: LDtkLayerView.() -> Unit = {}
) =
    LDtkLayerView(this, level.tileSets[this.tileset.json.uid]).addToLayer(layers, layerIdx, callback)