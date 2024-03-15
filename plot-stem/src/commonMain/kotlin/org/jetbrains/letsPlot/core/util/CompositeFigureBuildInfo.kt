/*
 * Copyright (c) 2023. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.core.util

import org.jetbrains.letsPlot.commons.geometry.DoubleRectangle
import org.jetbrains.letsPlot.commons.geometry.DoubleVector
import org.jetbrains.letsPlot.commons.unsupported.UNSUPPORTED
import org.jetbrains.letsPlot.core.plot.base.theme.Theme
import org.jetbrains.letsPlot.core.plot.builder.FigureBuildInfo
import org.jetbrains.letsPlot.core.plot.builder.GeomLayer
import org.jetbrains.letsPlot.core.plot.builder.layout.figure.CompositeFigureLayout
import org.jetbrains.letsPlot.core.plot.builder.layout.figure.FigureLayoutInfo
import org.jetbrains.letsPlot.core.plot.builder.subPlots.CompositeFigureSvgComponent
import org.jetbrains.letsPlot.core.plot.builder.subPlots.CompositeFigureSvgRoot

internal class CompositeFigureBuildInfo constructor(
    private val elements: List<FigureBuildInfo?>,
    private val layout: CompositeFigureLayout,
    override val bounds: DoubleRectangle,
    private val theme: Theme,
    override val computationMessages: List<String>,
) : FigureBuildInfo {

    override val isComposite: Boolean = true

    override val layoutInfo: FigureLayoutInfo
        get() = _layoutInfo

    override val containsLiveMap: Boolean
        get() = elements.filterNotNull().any { it.containsLiveMap }

    private lateinit var _layoutInfo: FigureLayoutInfo


    override fun injectLiveMapProvider(f: (tiles: List<List<GeomLayer>>, spec: Map<String, Any>) -> Any) {
        elements.filterNotNull().forEach {
            it.injectLiveMapProvider(f)
        }
    }

    override fun createSvgRoot(): CompositeFigureSvgRoot {
        check(this::_layoutInfo.isInitialized) { "Composite figure is not layouted." }
        val elementSvgRoots = elements.filterNotNull().map {
            it.createSvgRoot()
        }

        val svgComponent = CompositeFigureSvgComponent(elementSvgRoots, bounds.dimension, theme)
        return CompositeFigureSvgRoot(svgComponent, bounds)
    }

    override fun withBounds(bounds: DoubleRectangle): CompositeFigureBuildInfo {
        return if (bounds == this.bounds) {
            this
        } else {
            // this drops 'layout info' if initialized.
            CompositeFigureBuildInfo(
                elements,
                layout,
                bounds,
                theme,
                computationMessages
            )
        }
    }

    override fun layoutedByOuterSize(): CompositeFigureBuildInfo {
        val plotMargins = theme.plot().plotMargins()
        val leftTop = DoubleVector(
            plotMargins.left,
            plotMargins.top,
        )
        val marginsSize = DoubleVector(
            plotMargins.width,
            plotMargins.height,
        )
        val outerSize = bounds.dimension
        val elementsBounts = DoubleRectangle(leftTop, outerSize.subtract(marginsSize))
        val layoutedElements = layout.doLayout(elementsBounts, elements)

        val geomBounds = layoutedElements.filterNotNull().map {
            it.layoutInfo.geomAreaBounds
        }.reduce { acc, el -> acc.union(el) }

        return CompositeFigureBuildInfo(
            elements = layoutedElements,
            layout,
            bounds,
            theme,
            computationMessages
        ).apply {
            this._layoutInfo = FigureLayoutInfo(outerSize, geomBounds)
        }
    }

    override fun layoutedByGeomBounds(geomBounds: DoubleRectangle): CompositeFigureBuildInfo {
        UNSUPPORTED("Composite figure does not support layouting by \"geometry bounds\".")
    }

    override fun withPreferredSize(size: DoubleVector): FigureBuildInfo {
        return CompositeFigureBuildInfo(
            elements,
            layout,
            DoubleRectangle(DoubleVector.ZERO, size),
            theme,
            computationMessages
        )
    }
}