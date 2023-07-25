/*
 * Copyright (c) 2022. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package demo.plot.batik.plotConfig

import demo.plot.common.model.plotConfig.Dotplot
import demo.common.batik.demoUtils.PlotSpecsDemoWindowBatik

fun main() {
    with(Dotplot()) {
        PlotSpecsDemoWindowBatik(
            "Dot plot",
            plotSpecList()
        ).open()
    }
}