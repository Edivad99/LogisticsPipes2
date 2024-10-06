package logisticspipes.logistics

import logisticspipes.modules.LogisticsModule
import logisticspipes.particle.Particles
import logisticspipes.proxy.SimpleServiceLocator
import logisticspipes.routing.AsyncRouting
import logisticspipes.routing.ExitRoute
import logisticspipes.routing.PipeRoutingConnectionType
import logisticspipes.routing.ServerRouter
import logisticspipes.utils.SinkReply
import logisticspipes.utils.item.ItemIdentifier
import net.minecraft.world.item.ItemStack
import java.util.*
import java.util.stream.Stream

object LogisticsManager {
  fun allDestinations(stack: ItemStack, item: ItemIdentifier, canBeDefault: Boolean, sourceRouter: ServerRouter, filter: () -> Boolean): Sequence<Pair<Int, SinkReply>> {
    val jamList = LinkedList<Int>()
    return generateSequence {
      return@generateSequence if (filter()) getDestination(stack, item, canBeDefault, sourceRouter, jamList)?.also { jamList.add(it.first) } else null
    }
  }

  fun getDestination(stack: ItemStack, item: ItemIdentifier, canBeDefault: Boolean, sourceRouter: ServerRouter, routersToExclude: List<Int>): Pair<Int, SinkReply>? {
    val destinationStream = ServerRouter.getRoutersInterestedIn(item).stream()
      .mapToObj(SimpleServiceLocator.routerManager::getServerRouter)
      .flatMap {
        it?.let { router ->
          AsyncRouting.getDistance(sourceRouter, router)?.let { routes ->
            routes.stream()
              .filter { exitRoute -> exitRoute.containsFlag(PipeRoutingConnectionType.CAN_ROUTE_TO) }
          }
        } ?: Stream.empty()
      }
    return getBestReply(stack, item, sourceRouter, destinationStream, routersToExclude, canBeDefault)
  }

  private fun getBestReply(stack: ItemStack, itemid: ItemIdentifier, sourceRouter: ServerRouter, destinationStream: Stream<ExitRoute>, routersToExclude: List<Int>, canBeDefault: Boolean): Pair<Int, SinkReply>? {
    var resultRouterId: Int? = null
    var result: SinkReply? = null
    destinationStream.filter { it ->
      it.destination.id != sourceRouter.id &&
              !routersToExclude.contains(it.destination.simpleID) &&
              it.containsFlag(PipeRoutingConnectionType.CAN_ROUTE_TO) &&
              it.filters.none { filter -> filter.blockRouting() || filter.isBlocked == filter.isFilteredItem(itemid) } &&
              it.destination.logisticsModule != null &&
              it.destination.logisticsModule!!.receivePassive() &&
              it.destination.pipe != null &&
              !it.destination.pipe!!.isOnSameContainer(sourceRouter.pipe)
    }.sorted().forEachOrdered {
      val reply: SinkReply?
      val module: LogisticsModule = it.destination.logisticsModule!!
      reply = when {
        result == null -> module.sinksItem(stack, itemid, -1, 0, canBeDefault, true, true)
        result!!.maxNumberOfItems < 0 -> null
        else -> module.sinksItem(stack, itemid, result!!.fixedPriority.ordinal, result!!.customPriority, canBeDefault, true, true)
      }

      if (reply != null && (result == null || reply.fixedPriority.ordinal > result!!.fixedPriority.ordinal ||
                (reply.fixedPriority == result!!.fixedPriority && reply.customPriority > result!!.customPriority))
      ) {
        resultRouterId = it.destination.simpleID
        result = reply
      }
    }

    return result?.let { sinkReply ->
      resultRouterId?.let { destinationRouterId ->
        val pipe = SimpleServiceLocator.routerManager.getServerRouter(destinationRouterId)!!.pipe!!
        pipe.useEnergy(sinkReply.energyUse)
        pipe.spawnParticle(Particles.BLUE_SPARKLE, 10)
        Pair(destinationRouterId, sinkReply)
      }
    }
  }
}
