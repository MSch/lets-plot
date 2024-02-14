/*
 * Copyright (c) 2023. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.core.spec.config

import org.jetbrains.letsPlot.core.plot.base.theme.ExponentFormat
import org.jetbrains.letsPlot.core.plot.base.theme.FontFamilyRegistry
import org.jetbrains.letsPlot.core.plot.base.theme.Theme
import org.jetbrains.letsPlot.core.plot.builder.defaultTheme.ThemeUtil
import org.jetbrains.letsPlot.core.plot.builder.defaultTheme.values.ThemeOption
import org.jetbrains.letsPlot.core.plot.builder.defaultTheme.values.ThemeOption.ELEMENT_BLANK
import org.jetbrains.letsPlot.core.plot.builder.defaultTheme.values.ThemeOption.PANEL_PADDING
import org.jetbrains.letsPlot.core.plot.builder.defaultTheme.values.ThemeOption.PLOT_MARGIN
import org.jetbrains.letsPlot.core.spec.Option

class ThemeConfig constructor(
    themeSettings: Map<String, Any> = emptyMap(),
    fontFamilyRegistry: FontFamilyRegistry
) {

    val theme: Theme

    init {

        val themeName = themeSettings.getOrElse(Option.Meta.NAME) { ThemeOption.Name.LP_MINIMAL }.toString()

        // Make sure all values are converted to proper objects.
        @Suppress("NAME_SHADOWING")
        val userOptions: Map<String, Any> = themeSettings.mapValues { (key, value) ->
            var value = convertElementBlank(value)
            value = convertMargins(key, value)
            value = convertPadding(key, value)
            value = convertExponentFormat(key, value)
            LegendThemeConfig.convertValue(key, value)
        }

        theme = ThemeUtil.buildTheme(themeName, userOptions, fontFamilyRegistry)
    }

    companion object {
        private fun convertExponentFormat(key: String, value: Any): Any {
            if (key == ThemeOption.EXPONENT_FORMAT) {
                return when (value.toString().lowercase()) {
                    "e" -> ExponentFormat.E
                    "pow" -> ExponentFormat.POW
                    else -> throw IllegalArgumentException(
                        "Illegal value: '$value'.\n${ThemeOption.EXPONENT_FORMAT} expected value is a string: e|pow."
                    )
                }
            }
            return value
        }

        /**
         * Converts a simple "blank" string to a 'blank element'.
         */
        private fun convertElementBlank(value: Any): Any {
            if (value is String && value == ThemeOption.ELEMENT_BLANK_SHORTHAND) {
                return ELEMENT_BLANK
            }
            if (value is Map<*, *> && value["name"] == "blank") {
                return ELEMENT_BLANK
            }
            return value
        }

        private fun toThickness(obj: Any?): List<Double?> {
            val thickness: List<Double?> = when (obj) {
                is Number -> listOf(obj.toDouble())
                is List<*> -> {
                    require(obj.all { it == null || it is Number }) {
                        "The option requires a list of numbers, but was: $obj."
                    }
                    obj.map { (it as? Number)?.toDouble() }
                }
                else -> error("The option should be specified using number or list of numbers, but was: $obj.")
            }

            val top: Double?
            val right: Double?
            val bottom: Double?
            val left: Double?

            when (thickness.size) {
                1 -> {
                    val value = thickness.single()
                    top = value
                    right = value
                    left = value
                    bottom = value
                }
                2 -> {
                    val (v, h) = thickness
                    top = v
                    bottom = v
                    right = h
                    left = h
                }
                3 -> {
                    top = thickness[0]
                    right = thickness[1]
                    left = thickness[1]
                    bottom = thickness[2]
                }
                4 -> {
                    top = thickness[0]
                    right = thickness[1]
                    bottom = thickness[2]
                    left = thickness[3]
                }
                else -> {
                    error("The option accept a number or a list of one, two, three or four numbers, but was: $obj.")
                }
            }

            return listOf(top, right, bottom, left)
        }

        private fun convertMargins(key: String, value: Any): Any {
            fun toMarginSpec(value: Any?): Map<String, Any> {
                val (top, right, bottom, left) = toThickness(value)

                return mapOf(
                    ThemeOption.Elem.Margin.TOP to top,
                    ThemeOption.Elem.Margin.RIGHT to right,
                    ThemeOption.Elem.Margin.BOTTOM to bottom,
                    ThemeOption.Elem.Margin.LEFT to left
                )
                    .filterValues { it != null }
                    .mapValues { (_, v) -> v as Any }
            }

            return when {
                key == PLOT_MARGIN -> toMarginSpec(value)
                value is Map<*, *> && value.containsKey(ThemeOption.Elem.MARGIN) -> {
                    val margins = toMarginSpec(value[ThemeOption.Elem.MARGIN])
                    // to keep other options
                    value - ThemeOption.Elem.MARGIN + margins
                }
                else -> {
                    value
                }
            }
        }

        private fun convertPadding(key: String, value: Any): Any {
            fun toPaddingSpec(value: Any?): Map<String, Any> {
                val (top, right, bottom, left) = toThickness(value)

                return mapOf(
                    ThemeOption.Elem.Padding.TOP to top,
                    ThemeOption.Elem.Padding.RIGHT to right,
                    ThemeOption.Elem.Padding.BOTTOM to bottom,
                    ThemeOption.Elem.Padding.LEFT to left
                )
                    .filterValues { it != null }
                    .mapValues { (_, v) -> v as Any }
            }

            return when {
                key == PANEL_PADDING -> toPaddingSpec(value)
                value is Map<*, *> && value.containsKey(ThemeOption.Elem.PADDING) -> {
                    val padding = toPaddingSpec(value[ThemeOption.Elem.PADDING])
                    // to keep other options
                    value - ThemeOption.Elem.PADDING + padding
                }
                else -> value
            }
        }
    }
}
