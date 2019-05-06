package jetbrains.datalore.visualization.plot.gog.plot.scale.provider

import jetbrains.datalore.base.gcommon.collect.ClosedRange
import jetbrains.datalore.base.values.Color
import jetbrains.datalore.visualization.plot.gog.common.data.SeriesUtil
import jetbrains.datalore.visualization.plot.gog.core.data.DataFrame
import jetbrains.datalore.visualization.plot.gog.core.scale.MapperUtil
import jetbrains.datalore.visualization.plot.gog.core.scale.Transform
import jetbrains.datalore.visualization.plot.gog.plot.scale.GuideMapper
import jetbrains.datalore.visualization.plot.gog.plot.scale.mapper.ColorMapper
import jetbrains.datalore.visualization.plot.gog.plot.scale.mapper.GuideMappers
import kotlin.math.max
import kotlin.math.min

internal class ColorGradient2MapperProvider(low: Color?, mid: Color?, high: Color?, midpoint: Double?, naValue: Color) : MapperProviderBase<Color>(naValue) {

    private val myLow: Color
    private val myMid: Color
    private val myHigh: Color
    private val myMidpoint: Double?

    init {
        myLow = low ?: DEF_GRADIENT_LOW
        myMid = mid ?: DEF_GRADIENT_MID
        myHigh = high ?: DEF_GRADIENT_HIGH
        myMidpoint = midpoint ?: 0.0
    }

    override fun createContinuousMapper(data: DataFrame, variable: DataFrame.Variable, lowerLimit: Double?, upperLimit: Double?, trans: Transform?): GuideMapper<Color> {
        val domain = MapperUtil.rangeWithLimitsAfterTransform(data, variable, lowerLimit, upperLimit, trans)

        val lowDomain = ClosedRange.closed(domain.lowerEndpoint(), max(myMidpoint!!, domain.lowerEndpoint()))
        val highDomain = ClosedRange.closed(min(myMidpoint, domain.upperEndpoint()), domain.upperEndpoint())

        val lowMapper = ColorMapper.gradient(lowDomain, myLow, myMid, naValue)
        val highMapper = ColorMapper.gradient(highDomain, myMid, myHigh, naValue)

        val rangeMap = mapOf(
                lowDomain to lowMapper,
                highDomain to highMapper
        )

        fun getMapper(v: Double?): ((Double?) -> Color)? {
            var f_: ((Double?) -> Color)? = null
            if (SeriesUtil.isFinite(v)) {
                var f_span = Double.NaN
                for (range in rangeMap.keys) {
                    if (range.contains(v!!)) {
                        val span = range.upperEndpoint() - range.lowerEndpoint()
                        // try to avoid 0-length ranges
                        // but prefer shorter ranges
                        if (f_ == null || f_span == 0.0) {
                            f_ = rangeMap.get(range)
                            f_span = span
                        } else if (span < f_span && span > 0) {
                            f_ = rangeMap.get(range)
                            f_span = span
                        }
                    }
                }
            }
            return f_
        }

        val mapperFun: (Double?) -> Color = { input: Double? ->
            val mapper = getMapper(input)
            mapper?.invoke(input) ?: naValue
        }

        return GuideMappers.adaptContinuous(mapperFun)
    }

    companion object {
        // http://docs.ggplot2.org/current/scale_gradient.html
        private val DEF_GRADIENT_LOW = Color.parseHex("#964540") // muted("red")
        private val DEF_GRADIENT_MID = Color.WHITE
        private val DEF_GRADIENT_HIGH = Color.parseHex("#3B3D96") // muted("blue")
    }
}
