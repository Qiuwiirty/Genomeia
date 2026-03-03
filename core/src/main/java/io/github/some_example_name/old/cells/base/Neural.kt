package io.github.some_example_name.old.cells.base

import io.github.some_example_name.old.entities.CellEntity
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin


val formulaType = arrayOf(
    "y = ax + b",
    "y = c * sin(ax + b)",
    "y = c * cos(ax + b)",
    "y = 1 / (1 + e^(-(ax + b))) + c ",
    "y = b, x <= a; y = c, x > a",
    "y = b, x < a; y = c, x >= a",
    "y = t",
    "y = impulse(a), x>=1",
    "y = x, x is in (a, b) else y = c",
    "y = x^(a)",
    "y = remember(x), 0, 1"
)

fun activation(cellEntity: CellEntity, id: Int, x: Float) = with(cellEntity) {
    when (activationFuncType[id]) {
        0 -> a[id] * x + b[id]
        1 -> c[id] * sin(a[id] * x + b[id])
        2 -> c[id] * cos(a[id] * x + b[id])
        3 -> 1f / (1f + exp(-(a[id] * x + b[id]))) + c[id]
        4 -> if (x <= a[id]) b[id] else c[id]
        5 -> if (x < a[id]) b[id] else c[id]
        6 -> getTime(id)
        7 -> {
            if (x >= 1f && getTime(id) > dTime[id]) {
                dTime[id] = getTime(id) + a[id]
            }

            if (getTime(id) < dTime[id]) {
                1f
            } else {
                dTime[id] = -1f
                0f
            }
        }

        8 -> {
            if (x > a[id] && x < b[id]) {
                x
            } else c[id]
        }

        9 -> {
            x.pow(a[id])
        }

        10 -> {
            if (x > 0) {
                remember[id] = 1.0f
            } else if (x < 0) {
                remember[id] = 0.0f
            }
            remember[id]
        }

        else -> x
    }
}
