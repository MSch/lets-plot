/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot

import jetbrains.datalore.base.geometry.DoubleRectangle
import jetbrains.datalore.base.geometry.DoubleVector
import jetbrains.datalore.base.logging.PortableLogging
import jetbrains.datalore.plot.config.BunchConfig
import jetbrains.datalore.plot.config.PlotConfig
import jetbrains.datalore.vis.svgToString.SvgToString

object PlotSvgExport {
    private val LOG = PortableLogging.logger(PlotSvgExport::class)

    /**
     * @param plotSpec Raw specification of a plot or GGBunch.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun buildSvgImageFromRawSpecs(
        plotSpec: MutableMap<String, Any>
    ): String {
        return buildSvgImageFromRawSpecs(plotSpec, null)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun buildSvgImageFromRawSpecs(
        plotSpec: MutableMap<String, Any>,
        plotSize: DoubleVector?
    ): String {
        val list = MonolithicCommon.buildSvgImagesFromRawSpecs(
            plotSpec,
            plotSize,
            SvgToString(rgbEncoder = null)          // data-frame --> rgb image is not supported
        ) { messages ->
            messages.forEach {
                LOG.info { "[when SVG generating] $it" }
            }
        }

        if (list.isEmpty()) {
            throw IllegalStateException("Nothing to save: the plot is empty.")
        }

        if (list.size == 1) {
            return list[0]
        }

        // Must be GGBunch
        if (!PlotConfig.isGGBunchSpec(plotSpec)) {
            throw IllegalStateException("Can't save multiple SVG images in one file.") // Should never happen
        }

        val bunchItemSvgList = ArrayList<String>()
        var bunchBounds = DoubleRectangle(DoubleVector.ZERO, DoubleVector.ZERO)
        val bunchItems = BunchConfig(plotSpec).bunchItems
        for ((plotSvg, bunchItem) in list.zip(bunchItems)) {
            val (itemSvg, size) = transformBunchItemSvg(
                plotSvg, bunchItem.x, bunchItem.y
            )
            bunchItemSvgList.add(itemSvg)
            bunchBounds = bunchBounds.union(DoubleRectangle(bunchItem.x, bunchItem.y, size.x, size.y))
        }

        val svgStyle = getBunchItemSvgStyle(list[0])

        return """<svg xmlns="http://www.w3.org/2000/svg" class="plt-container" width="${bunchBounds.width}" height="${bunchBounds.height}">
            |$svgStyle
            |${bunchItemSvgList.joinToString(separator = "\n")}
            |</svg>
        """.trimMargin()
    }

    private fun getBunchItemSvgStyle(svg: String): String {
        val split = svg.split("<style type=\"text/css\">")
        val styleAtTheTop = split[1]
        val style = styleAtTheTop.split("</style>")[0]
        return """ 
            |<style type="text/css">
            |${style}
            |</style>""".trimMargin()
    }

    @Suppress("MemberVisibilityCanBePrivate")
    private fun transformBunchItemSvg(
        svg: String,
        x: Double,
        y: Double
    ): Pair<String, DoubleVector> {
        val svgHead = svg.split("<style type=\"text/css\">")[0]
        val split = svg.split("</style>")
        val rootGroup = split[1].split("</svg>")[0]

        val width = extractDouble(Regex(".*width=\"(\\d+)\\.?(\\d+)?\""), svgHead)
        val height = extractDouble(Regex(".*height=\"(\\d+)\\.?(\\d+)?\""), svgHead)

        val rootGroupTranslated =
            """<g transform="translate($x $y)" ${rootGroup.substring(
                rootGroup.indexOf("<g ") + 3
            )}"""
        return Pair(rootGroupTranslated, DoubleVector(width, height))
    }

    private fun extractDouble(regex: Regex, text: String): Double {
        val matchResult = regex.find(text)!!
        val values = matchResult.groupValues
        return if (values.size < 3)
            "${values[1]}".toDouble()
        else
            "${values[1]}.${values[2]}".toDouble()
    }
}