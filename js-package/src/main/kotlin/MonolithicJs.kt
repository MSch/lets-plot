/*
 * Copyright (c) 2019. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

/* root package */

import jetbrains.datalore.base.geometry.DoubleRectangle
import jetbrains.datalore.base.geometry.DoubleVector
import jetbrains.datalore.base.js.css.*
import jetbrains.datalore.base.jsObject.dynamicObjectToMap
import jetbrains.datalore.plot.MonolithicCommon
import jetbrains.datalore.plot.MonolithicCommon.PlotsBuildResult.Error
import jetbrains.datalore.plot.MonolithicCommon.PlotsBuildResult.Success
import jetbrains.datalore.plot.builder.config.FigureBuildInfo
import jetbrains.datalore.plot.config.FailureHandler
import jetbrains.datalore.plot.config.PlotConfig
import jetbrains.datalore.plot.config.PlotConfigClientSide
import jetbrains.datalore.plot.server.config.BackendSpecTransformUtil
import kotlinx.dom.createElement
import mu.KotlinLogging
import org.w3c.dom.*

private val LOG = KotlinLogging.logger {}

// key for data attibute <body data-lets-plot-preferred-width='700'>
private const val DATALORE_PREFERRED_WIDTH = "letsPlotPreferredWidth"

/**
 * The entry point to call in JS
 * `raw specs` are plot specs not processed by datalore plot backend
 */
@Suppress("unused")
@JsName("buildPlotFromRawSpecs")
fun buildPlotFromRawSpecs(plotSpecJs: dynamic, width: Double, height: Double, parentElement: HTMLElement) {
    try {
        val plotSpec = dynamicObjectToMap(plotSpecJs)
        PlotConfig.assertFigSpecOrErrorMessage(plotSpec)
        val processedSpec = processSpecs(plotSpec, frontendOnly = false)
        buildPlotFromProcessedSpecsIntern(processedSpec, width, height, parentElement)
    } catch (e: RuntimeException) {
        handleException(e, parentElement)
    }
}

/**
 * The entry point to call in JS
 * `processed specs` are plot specs processed by datalore plot backend
 */
@Suppress("unused")
@JsName("buildPlotFromProcessedSpecs")
fun buildPlotFromProcessedSpecs(plotSpecJs: dynamic, width: Double, height: Double, parentElement: HTMLElement) {
    try {
        val plotSpec = dynamicObjectToMap(plotSpecJs)
        // "processed" here means "processed on backend" -> apply "frontend" transforms to have truly processed specs.
        val processedSpec = processSpecs(plotSpec, frontendOnly = true)
        buildPlotFromProcessedSpecsIntern(processedSpec, width, height, parentElement)
    } catch (e: RuntimeException) {
        handleException(e, parentElement)
    }
}

private fun buildPlotFromProcessedSpecsIntern(
    plotSpec: MutableMap<String, Any>,
    width: Double,
    height: Double,
    parentElement: HTMLElement
) {
    val plotSize = if (width > 0 && height > 0) DoubleVector(width, height) else null
    val preferredWidth: Double? = parentElement.ownerDocument?.body?.dataset?.get(DATALORE_PREFERRED_WIDTH)?.toDouble()
    val parentWidth = when (val w = parentElement.clientWidth) {
        0 -> null  // parent element wasn't yet layouted
        else -> w
    }
    val maxWidth = if (preferredWidth == null) parentWidth?.toDouble() else null
//    LOG.error { "plotSize=$plotSize, preferredWidth=$preferredWidth, maxWidth=$maxWidth " }
    val buildResult = MonolithicCommon.buildPlotsFromProcessedSpecs(
        plotSpec,
        plotSize,
        maxWidth,
        preferredWidth,
    )
    if (buildResult.isError) {
        val errorMessage = (buildResult as Error).error
        showError(errorMessage, parentElement)
        return
    }

    val success = buildResult as Success
    val computationMessages = success.buildInfos.flatMap { it.computationMessages }
    computationMessages.forEach {
        showInfo(it, parentElement)
    }

    if (success.buildInfos.size == 1) {
        // a single figure
        val buildInfo = success.buildInfos[0]
        FigureToHtml(buildInfo, parentElement).eval()
    } else {
        // a bunch
        buildGGBunchComponent(success.buildInfos, parentElement)
    }
}

fun buildGGBunchComponent(plotInfos: List<FigureBuildInfo>, parentElement: HTMLElement) {
    val bunchBounds = plotInfos.map { it.bounds }
        .fold(DoubleRectangle(DoubleVector.ZERO, DoubleVector.ZERO)) { acc, bounds ->
            acc.union(bounds)
        }

    FigureToHtml.setupRootHTMLElement(
        parentElement,
        bunchBounds.dimension
    )

    for (plotInfo in plotInfos) {
        val origin = plotInfo.bounds.origin
        val itemElement = parentElement.ownerDocument!!.createElement("div") {
            setAttribute(
                "style",
                "position: absolute; left: ${origin.x}px; top: ${origin.y}px;"
            )
        } as HTMLElement

        parentElement.appendChild(itemElement)
        FigureToHtml(plotInfo, itemElement).eval()

    }

//    var style = "position: relative; width: ${bunchBounds.width}px; height: ${bunchBounds.height}px;"
//
////    // 'background-color' makes livemap disappear - set only if no livemaps in the bunch.
////    if (!plotInfos.any { it.plotAssembler.containsLiveMap }) {
////        style = "$style background-color: ${Defaults.BACKDROP_COLOR};"
////    }
//    parentElement.setAttribute("style", style)

}

private fun handleException(e: RuntimeException, parentElement: HTMLElement) {
    val failureInfo = FailureHandler.failureInfo(e)
    showError(failureInfo.message, parentElement)
    if (failureInfo.isInternalError) {
        LOG.error(e) {}
    }
}

private fun showError(message: String, parentElement: HTMLElement) {
    showText(message, "lets-plot-message-error", "color:darkred;", parentElement)
}

private fun showInfo(message: String, parentElement: HTMLElement) {
    showText(message, "lets-plot-message-info", "color:darkblue;", parentElement)
}

private fun showText(message: String, className: String, style: String, parentElement: HTMLElement) {
    val paragraphElement = parentElement.ownerDocument!!.createElement("p") as HTMLParagraphElement
    if (style.isNotBlank()) {
        paragraphElement.setAttribute("style", style)
    }
    paragraphElement.textContent = message
    paragraphElement.className = className
    parentElement.appendChild(paragraphElement)
}

@Suppress("DuplicatedCode")
private fun processSpecs(plotSpec: MutableMap<String, Any>, frontendOnly: Boolean): MutableMap<String, Any> {
    PlotConfig.assertFigSpecOrErrorMessage(plotSpec)
    if (PlotConfig.isFailure(plotSpec)) {
        return plotSpec
    }

    // Backend transforms
    @Suppress("NAME_SHADOWING")
    val plotSpec =
        if (frontendOnly) {
            plotSpec
        } else {
            BackendSpecTransformUtil.processTransform(plotSpec)
        }

    if (PlotConfig.isFailure(plotSpec)) {
        return plotSpec
    }

    // Frontend transforms
    return PlotConfigClientSide.processTransform(plotSpec)
}
