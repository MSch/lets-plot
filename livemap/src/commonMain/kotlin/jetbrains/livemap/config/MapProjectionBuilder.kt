/*
 * Copyright (c) 2021. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.livemap.config

import jetbrains.datalore.base.spatial.LonLatPoint
import jetbrains.datalore.base.typedGeometry.*
import jetbrains.datalore.base.typedGeometry.Transforms.transformBBox
import jetbrains.livemap.core.projections.GeoProjection
import jetbrains.livemap.core.projections.Geographic
import jetbrains.livemap.core.projections.Projection
import jetbrains.livemap.core.projections.Projections
import jetbrains.livemap.projection.MapProjection
import jetbrains.livemap.projection.World
import jetbrains.livemap.projection.WorldPoint
import jetbrains.livemap.projection.WorldRectangle
import kotlin.math.min

internal class MapProjectionBuilder(
    private val geoProjection: GeoProjection,
    private val mapRect: WorldRectangle
) {
    var reverseX = false
    var reverseY = false

    private fun offset(offset: Double): Projection<Double, Double> {
        return object : Projection<Double, Double> {
            override fun project(v: Double): Double = v - offset
            override fun invert(v: Double): Double = v + offset
        }
    }

    private fun <InT, InterT, OutT> composite(
        t1: Projection<InT, InterT>,
        t2: Projection<InterT, OutT>
    ): Projection<InT, OutT> {
        return object : Projection<InT, OutT> {
            override fun project(v: InT): OutT = v.run(t1::project).run(t2::project)
            override fun invert(v: OutT): InT = v.run(t2::invert).run(t1::invert)
        }
    }

    private fun linear(offset: Double, scale: Double): Projection<Double, Double> {
        return composite(offset(offset), Projections.scale { scale })
    }

    fun create(): MapProjection {
        val rect = transformBBox(geoProjection.validRect(), geoProjection::project)
        val scale = min(mapRect.width / rect.width, mapRect.height / rect.height)

        @Suppress("UNCHECKED_CAST")
        val projSize = (mapRect.dimension * (1.0 / scale)) as Vec<Geographic>
        val projRect = Rect(rect.center - projSize * 0.5, projSize)

        val offsetX = if (reverseX) projRect.right else projRect.left
        val scaleX = if (reverseX) -scale else scale
        val offsetY = if (reverseY) projRect.bottom else projRect.top
        val scaleY = if (reverseY) -scale else scale

        val linearProjection =
            Projections.tuple<Geographic, World>(
                linear(offsetX, scaleX),
                linear(offsetY, scaleY)
            )

        val proj = composite(geoProjection, linearProjection)

        return object : MapProjection {
            override fun project(v: LonLatPoint): WorldPoint = proj.project(v)
            override fun invert(v: WorldPoint): LonLatPoint = proj.invert(v)

            override val mapRect: WorldRectangle
                get() = this@MapProjectionBuilder.mapRect
        }
    }
}
