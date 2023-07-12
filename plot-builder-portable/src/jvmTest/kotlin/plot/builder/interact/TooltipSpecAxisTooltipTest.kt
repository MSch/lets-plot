/*
 * Copyright (c) 2019. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.interact

import org.jetbrains.letsPlot.core.plot.base.Aes
import org.jetbrains.letsPlot.core.plot.base.interact.TipLayoutHint
import org.jetbrains.letsPlot.core.plot.base.interact.TipLayoutHint.Kind.X_AXIS_TOOLTIP
import jetbrains.datalore.plot.builder.interact.MappedDataAccessMock.Companion.variable
import jetbrains.datalore.plot.builder.presentation.Defaults.Common.Tooltip.AXIS_TOOLTIP_COLOR
import kotlin.test.BeforeTest
import kotlin.test.Test

class TooltipSpecAxisTooltipTest : TooltipSpecTestHelper() {

    @BeforeTest
    fun setUp() {
        init()
        setAxisTooltipEnabled(true)
    }

    @Test
    fun whenXIsNotMapped_ShouldNotThrowException() {
        createTooltipSpecs(geomTargetBuilder.withPointHitShape(TARGET_HIT_COORD, 0.0).build())
    }

    @Test
    fun shouldNotAddLabel_WhenMappedToYAxisVar() {
        val v = variable().name("var_for_y").value("sedan")

        val fillMapping = addMappedData(v.mapping(org.jetbrains.letsPlot.core.plot.base.Aes.FILL))
        val yMapping = addMappedData(v.mapping(org.jetbrains.letsPlot.core.plot.base.Aes.Y))

        createTooltipSpecs(
            geomTargetBuilder.withPathHitShape()
                .withLayoutHint(
                    org.jetbrains.letsPlot.core.plot.base.Aes.FILL,
                    TipLayoutHint.verticalTooltip(
                        TARGET_HIT_COORD,
                        OBJECT_RADIUS,
                        markerColors = emptyList()
                    )
                )
                .build()
        )

        assertLines(0, fillMapping.shortTooltipText())
        assertLines(1, yMapping.shortTooltipText())
    }

    @Test
    fun whenXIsMapped_AndAxisTooltipEnabled_ShouldAddTooltipSpec() {
        val variable = variable().name("some label").value("some value").isContinuous(true)
        val xMapping = addMappedData(variable.mapping(org.jetbrains.letsPlot.core.plot.base.Aes.X))

        buildTooltipSpecs()

        assertHint(
            expectedHintKind = X_AXIS_TOOLTIP,
            expectedHintCoord = TARGET_X_AXIS_COORD,
            expectedObjectRadius = 1.5
        )
        assertFill(AXIS_TOOLTIP_COLOR)
        assertLines(0, xMapping.shortTooltipText())
    }


    @Test
    fun shouldNotAddLabel_When_MappedToYAxisVar_And_OneLineTooltip() {
        val v = variable().name("var_for_y").value("sedan")
        val yMapping = addMappedData(v.mapping(org.jetbrains.letsPlot.core.plot.base.Aes.Y))

        buildTooltipSpecs()
        assertLines(0, yMapping.shortTooltipText())
    }

    @Test
    fun multilineTooltip_shouldAddLabels() {
        val v = variable().name("var_for_y").value("sedan")
        val fillMapping = addMappedData(v.mapping(org.jetbrains.letsPlot.core.plot.base.Aes.FILL))
        val yMapping = addMappedData(v.mapping(org.jetbrains.letsPlot.core.plot.base.Aes.Y))

        buildTooltipSpecs()
        assertLines(0, fillMapping.longTooltipText(), yMapping.longTooltipText())
    }
}
