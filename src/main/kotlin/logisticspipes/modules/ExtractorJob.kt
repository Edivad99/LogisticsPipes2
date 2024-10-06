package logisticspipes.modules

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import logisticspipes.Configs
import logisticspipes.interfaces.IInventoryUtil
import logisticspipes.logistics.LogisticsManager
import logisticspipes.particle.Particles
import logisticspipes.routing.AsyncRouting
import logisticspipes.routing.AsyncRouting.needsRoutingTableUpdate
import logisticspipes.routing.AsyncRouting.updateServerRouterLsa
import logisticspipes.routing.ServerRouter
import logisticspipes.util.getExtractionMax
import logisticspipes.utils.item.ItemIdentifier
import logisticspipes.utils.item.ItemIdentifierStack
import kotlin.math.min

class ExtractorJob(private val module: AsyncExtractorModule, private val inventoryGetter: () -> IInventoryUtil?) {
  private var inventorySize = inventoryGetter()?.sizeInventory ?: 0
  private val slotsPerTick: Int = determineSlotsPerTick(module.everyNthTick, inventorySize)
  private val slotStartIter =
    if (slotsPerTick == 0) emptyList<Int>().iterator()
    else IntProgression.fromClosedRange(0, inventorySize - 1, slotsPerTick).iterator()
  private var itemsLeft = module.itemsToExtract
  private var stacksLeft = module.stacksToExtract
  private val updateRoutingTableMsgChannel: Channel<Unit> = Channel(Channel.CONFLATED)
  private val slotItemsToExtract: MutableMap<Int, ItemIdentifierStack> = HashMap()

  fun runSyncWork() {
    try {
      val serverRouter = module.serverRouter
      val inventory = inventoryGetter()
      if (slotsPerTick == 0 || !slotStartIter.hasNext() || serverRouter == null || inventory == null) {
        updateRoutingTableMsgChannel.close()
        return
      }
      inventorySize = inventory.sizeInventory
      val startSlot = slotStartIter.next()
      val stopSlot = if (slotStartIter.hasNext()) {
        min(inventorySize, startSlot + slotsPerTick)
      } else inventorySize

      val slotRange = startSlot until stopSlot
      for (slot in slotRange) {
        val stack = inventory.getStackInSlot(slot)
        // filters the stack out by the given filter method
        if (module.inverseFilter(stack)){
          continue
        }
        val toExtract = min(itemsLeft, stack.count)
        itemsLeft -= toExtract
        --stacksLeft
        slotItemsToExtract[slot] = ItemIdentifierStack(ItemIdentifier.get(stack), toExtract)
        if (itemsLeft < 1) {
          break
        }
        if (stacksLeft < 1) {
          break
        }
      }
      serverRouter.updateServerRouterLsa()
      if (serverRouter.needsRoutingTableUpdate()) {
        updateRoutingTableMsgChannel.trySend(Unit)
      } else {
        extractAndSend(serverRouter, inventory)
      }
      if (!slotStartIter.hasNext()) {
        updateRoutingTableMsgChannel.close()
      }
    } catch (error: Exception) {
      updateRoutingTableMsgChannel.close(error)
      throw error
    }
  }

  suspend fun runAsyncWork() {
    if (!Configs.DISABLE_ASYNC_WORK) {
      updateRoutingTableMsgChannel.consumeAsFlow().collect {
        module.serverRouter?.also { serverRouter ->
          AsyncRouting.updateRoutingTable(serverRouter)
        }
      }
    }
  }

  fun extractAndSend(serverRouter: ServerRouter, inventory: IInventoryUtil) {
    slotItemsToExtract.forEach { (slot, itemIdStack) ->
      extractAndSendStack(serverRouter, inventory, slot, itemIdStack)
    }
    slotItemsToExtract.clear()
  }

  private fun extractAndSendStack(
    serverRouter: ServerRouter,
    inventory: IInventoryUtil,
    slot: Int,
    itemIdStack: ItemIdentifierStack,
  ) {
    val service = module.service ?: return
    val pointedOrientation = service.pointedOrientation ?: return
    val stack = inventory.getStackInSlot(slot)
    if (!itemIdStack.item.equalsWithNBT(stack)) return
    var sourceStackLeft = itemIdStack.stackSize
    val validDestinationSequence = LogisticsManager.allDestinations(
      stack = stack,
      item = ItemIdentifier.get(stack),
      canBeDefault = true,
      sourceRouter = serverRouter,
    ) { sourceStackLeft > 0 }

    validDestinationSequence.forEach { (destRouterId, sinkReply) ->
      var extract = getExtractionMax(stack.count, sourceStackLeft, sinkReply)
      if (extract < 1) return@forEach
      while (!service.useEnergy(module.energyPerItem * extract)) {
        service.spawnParticle(Particles.ORANGE_SPARKLE, 2)
        if (extract < 2) break
        extract /= 2
      }
      val toSend = inventory.decrStackSize(slot, extract)
      if (toSend.isEmpty) return@forEach
      service.sendStack(toSend, destRouterId, sinkReply, module.itemSendMode, pointedOrientation)
      sourceStackLeft -= toSend.count
    }
  }
}
