/*
 * Copyright (c) 2021. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.vis.canvas.awt

import jetbrains.datalore.base.geometry.Vector
import java.awt.Rectangle
import javax.swing.JPanel

class CanvasContainerPanel(size: Vector) : JPanel(null) {
    init {
        bounds = Rectangle(0, 0, size.x, size.y)
    }

    override fun isOptimizedDrawingEnabled(): Boolean = false
}