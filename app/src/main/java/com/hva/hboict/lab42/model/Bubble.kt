package com.hva.hboict.lab42.model

data class Bubble(
    var x: Float,
    var y: Float,
    val speedY: Float,
    val speedX: Float,
    val size: Float,
    var direction: Direction,
)
