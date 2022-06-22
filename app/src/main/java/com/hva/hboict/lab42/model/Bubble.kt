package com.hva.hboict.lab42.model

import android.graphics.Paint

data class Bubble(
    var x: Float,
    var y: Float,
    val speedY: Float,
    val speedX: Float,
    var size: Float,
    var direction: Direction,
    var paint: Paint
)
