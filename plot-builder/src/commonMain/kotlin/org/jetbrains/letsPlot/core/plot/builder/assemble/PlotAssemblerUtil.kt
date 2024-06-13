/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.core.plot.builder.assemble

import org.jetbrains.letsPlot.commons.interval.DoubleSpan
import org.jetbrains.letsPlot.commons.values.Color
import org.jetbrains.letsPlot.core.plot.base.Aes
import org.jetbrains.letsPlot.core.plot.base.PlotContext
import org.jetbrains.letsPlot.core.plot.base.ScaleMapper
import org.jetbrains.letsPlot.core.plot.base.theme.AxisTheme
import org.jetbrains.letsPlot.core.plot.base.theme.FacetsTheme
import org.jetbrains.letsPlot.core.plot.base.theme.LegendTheme
import org.jetbrains.letsPlot.core.plot.base.theme.PlotTheme
import org.jetbrains.letsPlot.core.plot.builder.assemble.PlotGuidesAssemblerUtil.checkFitsColorBar
import org.jetbrains.letsPlot.core.plot.builder.assemble.PlotGuidesAssemblerUtil.createColorBarAssembler
import org.jetbrains.letsPlot.core.plot.builder.assemble.PlotGuidesAssemblerUtil.fitsColorBar
import org.jetbrains.letsPlot.core.plot.builder.assemble.PlotGuidesAssemblerUtil.mappedRenderedAesToCreateGuides
import org.jetbrains.letsPlot.core.plot.builder.layout.*

internal object PlotAssemblerUtil {

    private fun updateAesRangeMap(
        aes: Aes<*>,
        range: DoubleSpan?,
        rangeByAes: MutableMap<Aes<*>, DoubleSpan>
    ) {
        @Suppress("NAME_SHADOWING")
        var range = range
        if (range != null) {
            val wasRange = rangeByAes[aes]
            if (wasRange != null) {
                range = wasRange.union(range)
            }
            rangeByAes[aes] = range
        }
    }

    fun createLegends(
        ctx: PlotContext,
        geomTiles: PlotGeomTiles,
        scaleMappersNP: Map<Aes<*>, ScaleMapper<*>>,
        guideOptionsMap: Map<Aes<*>, GuideOptions>,
        theme: LegendTheme
    ): List<LegendBoxInfo> {

        val legendAssemblerByTitle = LinkedHashMap<String, LegendAssembler>()
        val colorBarAssemblerByTitle = LinkedHashMap<String, ColorBarAssembler>()

//        for (layerInfo in geomTiles.coreLayerInfos()) {
        for (layerInfo in geomTiles.layerInfos()) {
            val layerConstantByAes = HashMap<Aes<*>, Any>()
            for (aes in layerInfo.renderedAes()) {
                if (layerInfo.hasConstant(aes)) {
                    layerConstantByAes[aes] = layerInfo.getConstant(aes)!!
                }
            }

            val aesListByScaleName = LinkedHashMap<String, MutableList<Aes<*>>>()
            val aesList = mappedRenderedAesToCreateGuides(layerInfo, guideOptionsMap)
            for (aes in aesList) {
                val scale = ctx.getScale(aes)
                val scaleName = guideOptionsMap[aes]?.title ?: scale.name

                val colorBarOptions: ColorBarOptions? = guideOptionsMap[aes]?.let {
                    if (it is ColorBarOptions) {
                        checkFitsColorBar(aes, scale)
                        it
                    } else {
                        null
                    }
                }

                if (colorBarOptions != null || fitsColorBar(aes, scale)) {
                    // Colorbar
                    @Suppress("UNCHECKED_CAST")
                    val colorBarAssembler = createColorBarAssembler(
                        scaleName,
                        ctx.overallTransformedDomain(aes),
                        scale,
                        scaleMappersNP.getValue(aes) as ScaleMapper<Color>,
                        colorBarOptions,
                        theme
                    )

                    val colorbarName = colorBarAssemblerByTitle[scaleName]?.let { existingAssembler ->
                        if (colorBarAssembler.equalScalesAndOptions(existingAssembler)) {
                            scaleName
                        } else {
                            // Don't just replace an existing colorbar (see LP-760: ggmarginal(): broken coloring)
                            // Add under another key
                            "$scaleName (${aes.name})"
                        }
                    } ?: scaleName

                    colorBarAssemblerByTitle[colorbarName] = colorBarAssembler.withTitle(colorbarName)

                } else {
                    // Legend
                    aesListByScaleName.getOrPut(scaleName) { ArrayList() }.add(aes)
                }
            }

            for (scaleName in aesListByScaleName.keys) {
                val legendAssembler = legendAssemblerByTitle.getOrPut(scaleName) {
                    LegendAssembler(
                        scaleName,
                        guideOptionsMap,
                        scaleMappersNP,
                        theme
                    )
                }

                val aesListForScaleName = aesListByScaleName.getValue(scaleName)
                val legendKeyFactory = layerInfo.legendKeyElementFactory
                val aestheticsDefaults = layerInfo.aestheticsDefaults

                val overrideAesValues = mutableMapOf<Aes<*>, Any>()
                aesListForScaleName.forEach { aes ->
                    val overrideOptionsForAes = (guideOptionsMap[aes] as? LegendOptions)?.overrideAesValues
                    if (!overrideOptionsForAes.isNullOrEmpty()) {
                        // ToDo Need to check for conflicting settings?
                        overrideAesValues += overrideOptionsForAes
                    }
                }

                legendAssembler.addLayer(
                    legendKeyFactory,
                    aesListForScaleName,
                    overrideAesValues,
                    layerConstantByAes,
                    aestheticsDefaults,
                    layerInfo.colorByAes,
                    layerInfo.fillByAes,
                    layerInfo.isMarginal,
                    ctx,
                )
            }
        }

        val legendBoxInfos = ArrayList<LegendBoxInfo>()
        for (legendTitle in colorBarAssemblerByTitle.keys) {
            val boxInfo = colorBarAssemblerByTitle.getValue(legendTitle).createColorBar()
            if (!boxInfo.isEmpty) {
                legendBoxInfos.add(boxInfo)
            }
        }

        for (legendTitle in legendAssemblerByTitle.keys) {
            val boxInfo = legendAssemblerByTitle.getValue(legendTitle).createLegend()
            if (!boxInfo.isEmpty) {
                legendBoxInfos.add(boxInfo)
            }
        }
        return legendBoxInfos
    }

    fun createPlotLayout(
        layoutProviderByTile: List<TileLayoutProvider>,
        insideOut: Boolean,
        facets: PlotFacets,
        facetsTheme: FacetsTheme,
        hAxisTheme: AxisTheme,
        vAxisTheme: AxisTheme,
        plotTheme: PlotTheme,
    ): PlotLayout {
        return if (facets.isDefined) {
            FacetedPlotLayout(
                facets,
                layoutProviderByTile,
                facetsTheme.showStrip(),
                hAxisTheme,
                vAxisTheme,
                plotTheme,
                facetsTheme
            )
        } else {
            val tileLayout = if (insideOut) {
                layoutProviderByTile[0].createInsideOutTileLayout()
            } else {
                layoutProviderByTile[0].createTopDownTileLayout()
            }

            SingleTilePlotLayout(
                tileLayout,
                plotTheme
            )
        }
    }
}
