/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder

import jetbrains.datalore.base.event.MouseEvent
import jetbrains.datalore.base.event.MouseEventSpec.*
import jetbrains.datalore.base.geometry.DoubleRectangle
import jetbrains.datalore.base.geometry.DoubleVector
import jetbrains.datalore.base.observable.event.EventHandler
import jetbrains.datalore.plot.builder.interact.render.TooltipLayer
import jetbrains.datalore.vis.svg.SvgGElement

class PlotContainer(
    plot: Plot,
    plotSize: DoubleVector
) : PlotContainerPortable(plot, plotSize) {

    private val myDecorationLayer = SvgGElement()

    val mouseEventPeer: jetbrains.datalore.plot.builder.event.MouseEventPeer
        get() = plot.mouseEventPeer

    override fun buildContent() {
        super.buildContent()
        if (plot.interactionsEnabled) {
            svg.children().add(myDecorationLayer)
            hookupInteractions()
        }
    }

    override fun clearContent() {
        myDecorationLayer.children().clear()
        super.clearContent()
    }

    private fun hookupInteractions() {
        check(plot.interactionsEnabled)

        val viewport = DoubleRectangle(DoubleVector.ZERO, plot.plotSize)
        val tooltipLayer = TooltipLayer(myDecorationLayer, viewport)

        val onMouseMoved = { e: MouseEvent ->
            val coord = DoubleVector(e.x.toDouble(), e.y.toDouble())
            val tooltipSpecs = plot.createTooltipSpecs(coord)
            val tooltipBounds = plot.getTooltipBounds(coord)
            tooltipLayer.showTooltips(
                coord,
                tooltipSpecs,
                tooltipBounds
            )
        }
        reg(plot.mouseEventPeer.addEventHandler(MOUSE_MOVED, object : EventHandler<MouseEvent> {
            override fun onEvent(event: MouseEvent) {
                onMouseMoved(event)
            }
        }))
        reg(plot.mouseEventPeer.addEventHandler(MOUSE_DRAGGED, object : EventHandler<MouseEvent> {
            override fun onEvent(event: MouseEvent) {
                tooltipLayer.hideTooltip()
            }
        }))
        reg(plot.mouseEventPeer.addEventHandler(MOUSE_LEFT, object : EventHandler<MouseEvent> {
            override fun onEvent(event: MouseEvent) {
                tooltipLayer.hideTooltip()
            }
        }))
    }
}
