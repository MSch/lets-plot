/*
 * Copyright (c) 2023. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.core.interact

import org.jetbrains.letsPlot.commons.geometry.DoubleVector
import org.jetbrains.letsPlot.datamodel.svg.dom.SvgNode

interface InteractionContext {
    val decorationsLayer: SvgNode
    val eventsManager: EventsManager

    fun findTarget(plotCoord: DoubleVector): InteractionTarget?
}