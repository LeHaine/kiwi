package com.lehaine.kiwi.component

interface GameComponent : Component {
    val entities: ArrayList<Entity>
    val staticEntities: ArrayList<Entity>
}