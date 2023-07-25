/*
 * Copyright (c) 2019. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package demo.plot.batik.plotConfig

import demo.plot.common.model.plotConfig.Polygons
import demo.common.batik.demoUtils.PlotSpecsDemoWindowBatik

fun main() {
    with(Polygons()) {
        PlotSpecsDemoWindowBatik(
            "Polygons",
            plotSpecList()
        ).open()
    }
}
