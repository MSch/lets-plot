/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.core.plot.builder.assemble

import org.jetbrains.letsPlot.commons.geometry.DoubleRectangle
import org.jetbrains.letsPlot.commons.geometry.DoubleVector
import org.jetbrains.letsPlot.commons.values.Font
import org.jetbrains.letsPlot.core.commons.data.SeriesUtil
import org.jetbrains.letsPlot.core.plot.base.Aesthetics
import org.jetbrains.letsPlot.core.plot.base.GeomContext
import org.jetbrains.letsPlot.core.plot.base.ScaleMapper
import org.jetbrains.letsPlot.core.plot.base.annotations.Annotations
import org.jetbrains.letsPlot.core.plot.base.tooltip.GeomTargetCollector
import org.jetbrains.letsPlot.core.plot.base.tooltip.NullGeomTargetCollector
import org.jetbrains.letsPlot.core.plot.builder.presentation.FontFamilyRegistry
import org.jetbrains.letsPlot.commons.values.Color
import org.jetbrains.letsPlot.core.plot.base.Aes
import org.jetbrains.letsPlot.core.plot.builder.presentation.PlotLabelSpec

class GeomContextBuilder : ImmutableGeomContext.Builder {
    private var flipped: Boolean = false
    private var aesthetics: Aesthetics? = null
    private var aestheticMappers: Map<Aes<*>, ScaleMapper<*>>? = null
    private var aesBounds: DoubleRectangle? = null
    private var geomTargetCollector: GeomTargetCollector = NullGeomTargetCollector()
    private var fontFamilyRegistry: FontFamilyRegistry? = null
    private var annotations: Annotations? = null
    private var plotBackground: Color = Color.WHITE

    constructor()

    private constructor(ctx: MyGeomContext) {
        flipped = ctx.flipped
        aesthetics = ctx.aesthetics
        aestheticMappers = ctx.aestheticMappers
        aesBounds = ctx._aesBounds
        geomTargetCollector = ctx.targetCollector
        annotations = ctx.annotations
        plotBackground = ctx.plotBackground
    }

    override fun flipped(flipped: Boolean): ImmutableGeomContext.Builder {
        this.flipped = flipped
        return this
    }

    override fun aesthetics(aesthetics: Aesthetics): ImmutableGeomContext.Builder {
        this.aesthetics = aesthetics
        return this
    }

    override fun aestheticMappers(aestheticMappers: Map<Aes<*>, ScaleMapper<*>>): ImmutableGeomContext.Builder {
        this.aestheticMappers = aestheticMappers
        return this
    }

    override fun aesBounds(aesBounds: DoubleRectangle): ImmutableGeomContext.Builder {
        this.aesBounds = aesBounds
        return this
    }

    override fun geomTargetCollector(geomTargetCollector: GeomTargetCollector): ImmutableGeomContext.Builder {
        this.geomTargetCollector = geomTargetCollector
        return this
    }

    override fun fontFamilyRegistry(v: FontFamilyRegistry): ImmutableGeomContext.Builder {
        fontFamilyRegistry = v
        return this
    }

    override fun annotations(annotations: Annotations?): ImmutableGeomContext.Builder {
        this.annotations = annotations
        return this
    }

    override fun plotBackground(color: Color): ImmutableGeomContext.Builder {
        this.plotBackground = color
        return this
    }

    override fun build(): ImmutableGeomContext {
        return MyGeomContext(this)
    }


    private class MyGeomContext(b: GeomContextBuilder) : ImmutableGeomContext {
        val aesthetics = b.aesthetics
        val aestheticMappers = b.aestheticMappers
        val _aesBounds: DoubleRectangle? = b.aesBounds

        override val flipped: Boolean = b.flipped
        override val targetCollector = b.geomTargetCollector
        override val annotations = b.annotations
        override val plotBackground = b.plotBackground

        private val fontFamilyRegistry: FontFamilyRegistry? = b.fontFamilyRegistry

        override fun getResolution(aes: Aes<Double>): Double {
            var resolution = 0.0
            if (aesthetics != null) {
                resolution = aesthetics.resolution(aes, 0.0)
            }
            if (resolution <= SeriesUtil.TINY) {
                resolution = 1.0
            }

            return resolution
        }

        override fun isMappedAes(aes: Aes<*>): Boolean {
            return aestheticMappers?.containsKey(aes) ?: false
        }

        override fun estimateTextSize(
            text: String,
            family: String,
            size: Double,
            isBold: Boolean,
            isItalic: Boolean
        ): DoubleVector {
            val registry = fontFamilyRegistry
            check(registry != null) { "Font-family registry is not specified." }
            @Suppress("NAME_SHADOWING")
            val family = registry.get(family)
            return PlotLabelSpec(
                Font(
                    family = family,
                    size = size.toInt(),
                    isBold = isBold,
                    isItalic = isItalic
                ),
            ).dimensions(text)
        }

        override fun getAesBounds(): DoubleRectangle {
            check(_aesBounds != null) { "GeomContext: aesthetics bounds are not defined." }
            return _aesBounds
        }

        override fun withTargetCollector(targetCollector: GeomTargetCollector): GeomContext {
            return with()
                .geomTargetCollector(targetCollector)
                .build()
        }

        override fun with(): ImmutableGeomContext.Builder {
            return GeomContextBuilder(this)
        }
    }
}
