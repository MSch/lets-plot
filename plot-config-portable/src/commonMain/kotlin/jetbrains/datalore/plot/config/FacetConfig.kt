/*
 * Copyright (c) 2019. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.config

import jetbrains.datalore.plot.base.DataFrame
import jetbrains.datalore.plot.base.data.DataFrameUtil
import jetbrains.datalore.plot.builder.assemble.PlotFacets
import jetbrains.datalore.plot.builder.assemble.PlotFacets.Companion.DEF_ORDER_DIR
import jetbrains.datalore.plot.builder.assemble.facet.FacetGrid
import jetbrains.datalore.plot.builder.assemble.facet.FacetWrap
import jetbrains.datalore.plot.config.Option.Facet
import jetbrains.datalore.plot.config.Option.Facet.FACETS_FILL_DIR
import jetbrains.datalore.plot.config.Option.Facet.X_ORDER
import jetbrains.datalore.plot.config.Option.Facet.Y_ORDER

internal class FacetConfig(options: Map<String, Any>) : OptionsAccessor(options) {

    fun createFacets(dataByLayer: List<DataFrame>): PlotFacets {
        return when (val name = getStringSafe(Facet.NAME)) {
            Facet.NAME_GRID -> createGrid(dataByLayer)
            Facet.NAME_WRAP -> createWrap(dataByLayer)
            else -> throw IllegalArgumentException("Facet 'grid' or 'wrap' expected but was: `$name`")
        }
    }

    private fun createGrid(
        dataByLayer: List<DataFrame>
    ): FacetGrid {
        var nameX: String? = null
        val levelsX = LinkedHashSet<Any>()
        if (has(Facet.X)) {
            nameX = getStringSafe(Facet.X)
            for (data in dataByLayer) {
                if (DataFrameUtil.hasVariable(data, nameX)) {
                    val variable = DataFrameUtil.findVariableOrFail(data, nameX)
                    levelsX.addAll(data.distinctValues(variable))
                }
            }
        }

        var nameY: String? = null
        val levelsY = LinkedHashSet<Any>()
        if (has(Facet.Y)) {
            nameY = getStringSafe(Facet.Y)
            for (data in dataByLayer) {
                if (DataFrameUtil.hasVariable(data, nameY)) {
                    val variable = DataFrameUtil.findVariableOrFail(data, nameY)
                    levelsY.addAll(data.distinctValues(variable))
                }
            }
        }

        return FacetGrid(
            nameX, nameY, ArrayList(levelsX), ArrayList(levelsY),
            getOrderOptionDef(X_ORDER),
            getOrderOptionDef(Y_ORDER)
        )
    }

    private fun createWrap(
        dataByLayer: List<DataFrame>
    ): FacetWrap {
        // 'facets' cal be just one name or a list of names.
        val facets = getAsStringList(Facet.FACETS)

        val ncol = getInteger(Facet.NCOL)
        val nrow = getInteger(Facet.NROW)

        val facetLevels = ArrayList<List<Any>>()
        for (name in facets) {
            val levels = HashSet<Any>()
            for (data in dataByLayer) {
                if (DataFrameUtil.hasVariable(data, name)) {
                    val variable = DataFrameUtil.findVariableOrFail(data, name)
                    levels.addAll(data.get(variable).filterNotNull())
                }
            }
            facetLevels.add(levels.toList())
        }

        val orderList = if (has(Facet.FACETS_ORDER)) {
            // The 'order' option can be a list or just one (Int) value.
            when (val orderOption = get(Facet.FACETS_ORDER)) {
                is List<*> -> orderOption.map { toOrderVal(it) }
                else -> listOf(toOrderVal(orderOption))
            }
        } else {
            emptyList()
        }

        // Add missing elements to match the factes count
        val orderListFinal = ArrayList<Int>()
        for (i in facets.indices) {
            orderListFinal.add(
                if (i < orderList.size) orderList[i]
                else DEF_ORDER_DIR
            )
        }
        return FacetWrap(facets, facetLevels, nrow, ncol, getDirOption(), orderListFinal)
    }


    private fun getOrderOptionDef(optionName: String): Int {
        return toOrderVal(get(optionName))
    }

    private fun toOrderVal(orderOption: Any?): Int {
        return when (orderOption) {
            null -> DEF_ORDER_DIR
            is Number -> orderOption.toInt()
            else -> throw IllegalArgumentException(
                "Unsupported `order` value: $orderOption.\n" +
                        "Use: 1 (natural) or -1 (descending)."
            )
        }
    }

    private fun getDirOption(): FacetWrap.Direction {
        return when (val opt = get(FACETS_FILL_DIR)) {
            null -> FacetWrap.Direction.H
            else -> when (opt.toString().toUpperCase()) {
                "V" -> FacetWrap.Direction.V
                "H" -> FacetWrap.Direction.H
                else -> throw IllegalArgumentException(
                    "Unsupported `dir` value: $opt.\n" +
                            "Use: 'H' (horizontal) or 'V' (vertical)."
                )
            }
        }
    }
}
