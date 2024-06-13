/*
 * Copyright (c) 2023. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package demo.common.jfx.demoUtils

import org.jetbrains.letsPlot.commons.geometry.DoubleVector
import org.jetbrains.letsPlot.commons.registration.DisposingHub
import org.jetbrains.letsPlot.core.plot.builder.PlotContainer
import org.jetbrains.letsPlot.core.plot.builder.PlotSvgRoot
import org.jetbrains.letsPlot.core.plot.builder.assemble.PlotAssembler
import demo.common.util.demoUtils.swing.PlotResizableDemoWindowBase
import org.jetbrains.letsPlot.awt.plot.component.PlotComponentProvider
import org.jetbrains.letsPlot.awt.plot.component.PlotPanel
import org.jetbrains.letsPlot.jfx.plot.util.SceneMapperJfxPanel
import org.jetbrains.letsPlot.jfx.plot.component.DefaultSwingContextJfx
import java.awt.Dimension
import javax.swing.JComponent

class PlotResizableDemoWindowJfx(
    title: String,
    private val plotAssembler: PlotAssembler,
    plotSize: Dimension = Dimension(500, 350)
) : PlotResizableDemoWindowBase(
    title,
    plotSize = plotSize
) {

    override fun createPlotComponent(plotSize: Dimension): JComponent {
        @Suppress("NAME_SHADOWING")
        val plotSize = DoubleVector(
            plotSize.getWidth(),
            plotSize.getHeight(),
        )

        return PlotPanel(
            plotComponentProvider = MyPlotComponentProvider(plotAssembler, plotSize),
            preferredSizeFromPlot = true,
            repaintDelay = 100,
            applicationContext = DefaultSwingContextJfx()
        )
    }

    private class MyPlotComponentProvider(
        private val plotAssembler: PlotAssembler,
        private val plotInitialSize: DoubleVector,
    ) : PlotComponentProvider {
        override fun getPreferredSize(containerSize: Dimension): Dimension {
            return containerSize
        }

        override fun createComponent(containerSize: Dimension?, specOverride: Map<String, Any>?): JComponent {
            val plotSize = if (containerSize != null) {
                DoubleVector(
                    containerSize.getWidth(),
                    containerSize.getHeight()
                )
            } else {
                plotInitialSize
            }

            val layoutInfo = plotAssembler.layoutByOuterSize(plotSize)

            val plotSvgComponent = plotAssembler.createPlot(layoutInfo)
            val plotContainer = PlotContainer(
                PlotSvgRoot(
                    plotSvgComponent,
                    liveMapCursorServiceConfig = null,
                    DoubleVector.ZERO
                )
            )

            val component = SceneMapperJfxPanel(plotContainer.svg, stylesheets = emptyList())
            (component as DisposingHub).registerDisposable(plotContainer)
            return component
        }
    }
}