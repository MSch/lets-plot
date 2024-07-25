/*
 * Copyright (c) 2024. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.core.plot.base.scale.breaks

import org.jetbrains.letsPlot.commons.interval.DoubleSpan
import org.jetbrains.letsPlot.core.plot.base.scale.BreaksGenerator
import org.jetbrains.letsPlot.core.plot.base.scale.ScaleBreaks

class DateTimeBreaksGen(
    private val providedFormatter: ((Any) -> String)? = null
) : BreaksGenerator {
    override fun generateBreaks(domain: DoubleSpan, targetCount: Int): ScaleBreaks {
        val helper = DateTimeBreaksHelper(
            domain.lowerEnd,
            domain.upperEnd,
            targetCount,
            providedFormatter
        )

        return ScaleBreaks.ContinuousFlex.noTransform(
            domainValues = helper.breaks,
            formatter = helper.formatter
        )
    }

    override fun defaultFormatter(domain: DoubleSpan, targetCount: Int): (Any) -> String {
        return DateTimeBreaksHelper(
            domain.lowerEnd,
            domain.upperEnd,
            targetCount,
            providedFormatter = null
        ).formatter
    }
}
