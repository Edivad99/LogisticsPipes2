package logisticspipes.util

import logisticspipes.utils.SinkReply
import kotlin.math.min

fun getExtractionMax(stackCount: Int, maxExtractionCount: Int, sinkReply: SinkReply): Int {
  return min(stackCount, maxExtractionCount).let {
    if (sinkReply.maxNumberOfItems > 0) {
      min(it, sinkReply.maxNumberOfItems)
    } else it
  }
}
