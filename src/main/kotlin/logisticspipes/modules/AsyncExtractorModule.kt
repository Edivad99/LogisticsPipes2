package logisticspipes.modules

import kotlinx.coroutines.Deferred
import logisticspipes.Configs
import logisticspipes.interfaces.IInventoryUtil
import logisticspipes.pipes.basic.CoreRoutedPipe
import logisticspipes.routing.ServerRouter
import logisticspipes.utils.PlayerCollectionList
import net.minecraft.core.Direction
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack
import kotlin.math.pow

class AsyncExtractorModule(
  val inverseFilter: (ItemStack) -> Boolean = { stack -> stack.isEmpty },
) : AsyncModule<ExtractorJob, Unit>(), SneakyDirection {

  companion object {
    @JvmStatic
    val name: String = "extractor"
  }

  val localModeWatchers = PlayerCollectionList()
  private var currentJob: ExtractorJob? = null
  

  override var sneakyDirection: Direction?
    get() = Direction.DOWN //sneakyDirectionProp.value
    set(value) {
      //sneakyDirectionProp.value = value
      /*MainProxy.sendToPlayerList(
        PacketHandler.getPacket(SneakyModuleDirectionUpdate::class.java)
          .setDirection(sneakyDirection)
          .setModulePos(this),
        localModeWatchers,
      )*/
    }

  internal val serverRouter: ServerRouter?
    get() = service?.router as? ServerRouter

  override val everyNthTick: Int
    get() = (80 / upgradeManager.let { 2.0.pow(it.actionSpeedUpgrade) }).toInt() + Configs.MINIMUM_JOB_TICK_LENGTH

  val stacksToExtract: Int
    get() = 1 + upgradeManager.itemStackExtractionUpgrade

  val itemsToExtract: Int
    get() = upgradeManager.let { 4 * it.itemExtractionUpgrade + 64 * upgradeManager.itemStackExtractionUpgrade }
      .coerceAtLeast(1)

  internal val energyPerItem: Int
    get() = upgradeManager.let {
      5 * 1.1.pow(it.itemExtractionUpgrade) * 1.2.pow(it.itemStackExtractionUpgrade)
    }.toInt()

  internal val itemSendMode: CoreRoutedPipe.ItemSendMode
    get() = upgradeManager.let { um ->
      CoreRoutedPipe.ItemSendMode.FAST.takeIf { um.itemExtractionUpgrade > 0 }
    } ?: CoreRoutedPipe.ItemSendMode.NORMAL

  private val connectedInventory: IInventoryUtil?
    get() = this.service?.availableSneakyInventories(sneakyDirection)?.firstOrNull()

  override fun jobSetup(): ExtractorJob = ExtractorJob(this) { connectedInventory }.also {
    currentJob = it
    it.runSyncWork()
  }

  override fun runSyncWork() {
    currentJob?.runSyncWork()
  }

  override fun serializeNBT(provider: HolderLookup.Provider): CompoundTag {
    return CompoundTag()
  }

  override fun deserializeNBT(provider: HolderLookup.Provider, nbt: CompoundTag) {

  }

  override suspend fun tickAsync(setupObject: ExtractorJob) {
    setupObject.runAsyncWork()
  }

  override fun completeJob(deferred: Deferred<Unit?>) {
    val serverRouter = this.serverRouter ?: return
    val inventory = connectedInventory ?: return
    serverRouter.ensureLatestRoutingTable()
    currentJob?.extractAndSend(serverRouter, inventory)
  }

  override fun receivePassive(): Boolean = false

  override fun hasGenericInterests(): Boolean = false
}

