/*
 * Copyright (c) 2022. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.livemap.core.graphics

import jetbrains.datalore.base.geometry.DoubleVector
import jetbrains.datalore.vis.canvas.Context2d

class TextMeasurer(private val myContext2d: Context2d) {
    fun measure(label: String, font: Context2d.Font): DoubleVector {
        myContext2d.save()
        myContext2d.setFont(font)
        val width = myContext2d.measureText(label)
        myContext2d.restore()

        return DoubleVector(width, font.fontSize)
    }

    fun measure(label: List<String>, font: Context2d.Font, lineHeight: Double): DoubleVector {
        myContext2d.save()
        myContext2d.setFont(font)

        val width = label.maxOf(myContext2d::measureText)
        myContext2d.restore()

        val height = lineHeight * (label.size - 1) + font.fontSize
        return DoubleVector(width, height)
    }
}