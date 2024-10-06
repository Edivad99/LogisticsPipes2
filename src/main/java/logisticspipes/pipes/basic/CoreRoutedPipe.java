package logisticspipes.pipes.basic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.PriorityBlockingQueue;
import org.jetbrains.annotations.Nullable;
import logisticspipes.Configs;
import logisticspipes.LogisticsPipes;
import logisticspipes.asm.te.ILPTEInformation;
import logisticspipes.fromkotlin.Adjacent;
import logisticspipes.fromkotlin.AdjacentFactory;
import logisticspipes.fromkotlin.NoAdjacent;
import logisticspipes.interfaces.IPipeServiceProvider;
import logisticspipes.interfaces.IPipeUpgradeManager;
import logisticspipes.interfaces.ISlotUpgradeManager;
import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.interfaces.routing.IRequireReliableTransport;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.logisticspipes.ITrackStatistics;
import logisticspipes.logisticspipes.PipeTransportLayer;
import logisticspipes.logisticspipes.RouteLayer;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.particle.Particles;
import logisticspipes.particle.PipeFXRenderHandler;
import logisticspipes.pipes.basic.debug.DebugLogController;
import logisticspipes.pipes.basic.debug.StatusEntry;
import logisticspipes.pipes.upgrades.UpgradeManager;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.ItemRoutingInformation;
import logisticspipes.transport.PipeTransportLogistics;
import logisticspipes.utils.CacheHolder;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.tuples.Triplet;
import lombok.Getter;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public abstract class CoreRoutedPipe extends CoreUnroutedPipe
    implements IRequestItems, ITrackStatistics, IPipeServiceProvider {

  private static int PIPE_COUNT = 0;

  private final PriorityBlockingQueue<ItemRoutingInformation> inTransitToMe =
      new PriorityBlockingQueue<>(10, new ItemRoutingInformation.DelayComparator());

  protected final LinkedList<Triplet<IRoutedItem, Direction, ItemSendMode>> sendQueue = new LinkedList<>();
  protected final Map<Item, Queue<Tuple<Integer, ItemRoutingInformation>>> queuedDataForUnroutedItems = Collections.synchronizedMap(new TreeMap<>());

  public boolean textureBufferPowered;
  public long delayTo = 0;
  public int repeatFor = 0;
  public int stat_session_sent, stat_session_received, stat_session_relayed;
  public long stat_lifetime_sent, stat_lifetime_received, stat_lifetime_relayed;
  @Getter
  protected boolean stillNeedReplace = true;
  @Nullable
  protected IRouter router;
  @Nullable
  private String routerId;
  protected final Object routerIdLock = new Object();
  protected int delayOffset;
  @Getter
  protected boolean initialInit = true;
  @Nullable
  protected RouteLayer routeLayer;
  @Nullable
  protected PipeTransportLayer transportLayer;

  final protected UpgradeManager upgradeManager = new UpgradeManager(this);
  @Nullable
  private CacheHolder cacheHolder;
  private boolean recheckConnections = false;
  private boolean destroyByPlayer = false;
  protected int throttleTime = 20;
  private int throttleTimeLeft = 20 + new Random().nextInt(Configs.LOGISTICS_DETECTION_FREQUENCY);
  private final int[] queuedParticles = new int[Particles.values().length];
  private boolean hasQueuedParticles = false;

  public CoreRoutedPipe(PipeTransportLogistics transport) {
    super(transport);
  }

  public CoreRoutedPipe() {
    this(new PipeTransportLogistics(true));
  }

  @Override
  public final void updateEntity() {
    this.debug.tick();
    this.spawnParticleTick();
    if (stillNeedReplace) {
      stillNeedReplace = false;
      //first tick just create a router and do nothing.
      this.firstInitialiseTick();
      return;
    }
    if (this.repeatFor > 0) {
      if (this.delayTo < System.currentTimeMillis()) {
        this.delayTo = System.currentTimeMillis() + 200;
        this.repeatFor--;
        this.getLevel().updateNeighborsAt(getPos(), this.getLevel().getBlockState(getPos()).getBlock());
      }
    }

    // remove old items _inTransit -- these should have arrived, but have probably been lost instead. In either case, it will allow a re-send so that another attempt to re-fill the inventory can be made.
    while (this.inTransitToMe.peek() != null && this.inTransitToMe.peek().getTickToTimeOut() <= 0) {
      final ItemRoutingInformation polledInfo = this.inTransitToMe.poll();
      if (polledInfo != null) {
        if (LogisticsPipes.isDebug()) {
          LogisticsPipes.LOG.info("Timed Out: " + polledInfo.getItem().getFriendlyName() + " (" + polledInfo.hashCode() + ")");
        }
        debug.log("Timed Out: " + polledInfo.getItem().getFriendlyName() + " (" + polledInfo.hashCode() + ")");
      }
    }
    //update router before ticking logic/transport
    final boolean doFullRefresh =
        this.getLevel().getGameTime() % Configs.LOGISTICS_DETECTION_FREQUENCY == this.delayOffset
            || this.initialInit || this.recheckConnections;
    if (doFullRefresh) {
      // update adjacent cache first, so interests can be gathered correctly
      // in getRouter().update(...) below
      this.updateAdjacentCache();
    }
    this.getRouter().update(doFullRefresh, this);
    this.recheckConnections = false;
    this.getOriginalUpgradeManager().securityTick();
    super.updateEntity();

    if (this.isNthTick(200)) {
      getCacheHolder().trigger(null);
    }

    // from BaseRoutingLogic
    if (--this.throttleTimeLeft <= 0) {
      this.throttledUpdateEntity();
      this.throttleTimeLeft = throttleTime;
    }

    this.ignoreDisableUpdateEntity();
    this.initialInit = false;
    if (!this.sendQueue.isEmpty()) {
      switch (this.getItemSendMode()) {
        case NORMAL:
          var itemToSend = this.sendQueue.getFirst();
          this.sendRoutedItem(itemToSend.getA(), itemToSend.getB());
          this.sendQueue.removeFirst();
          for (int i = 0; i < 16 && !this.sendQueue.isEmpty() && this.sendQueue.getFirst().getC() == ItemSendMode.FAST; i++) {
            if (!this.sendQueue.isEmpty()) {
              itemToSend = this.sendQueue.getFirst();
              this.sendRoutedItem(itemToSend.getA(), itemToSend.getB());
              this.sendQueue.removeFirst();
            }
          }
          this.sendQueueChanged(false);
          break;
        case FAST:
          for (int i = 0; i < 16; i++) {
            if (!this.sendQueue.isEmpty()) {
              var itemToSend2 = this.sendQueue.getFirst();
              this.sendRoutedItem(itemToSend2.getA(), itemToSend2.getB());
              this.sendQueue.removeFirst();
            }
          }
          this.sendQueueChanged(false);
          break;
        default:
          throw new UnsupportedOperationException(
              "getItemSendMode() returned unhandled value. %s in %s"
                  .formatted(getItemSendMode().name(), this.getClass().getName()));
      }
    }
    if (getLevel().isClientSide) {
      return;
    }
    this.checkTexturePowered();
    this.enabledUpdateEntity();
    if (this.getLogisticsModule() != null) {
      this.getLogisticsModule().tick();
    }
  }

  public abstract ItemSendMode getItemSendMode();

  private void sendRoutedItem(IRoutedItem routedItem, Direction from) {
    this.transport.injectItem(routedItem, from.getOpposite());

    IRouter r = SimpleServiceLocator.routerManager.getServerRouter(routedItem.getDestination());
    if (r != null) {
      CoreRoutedPipe pipe = r.getCachedPipe();
      if (pipe != null) {
        pipe.notifyOfSend(routedItem.getInfo());
      } // else TODO: handle sending items to known chunk-unloaded destination?
    } // should not be able to send to a non-existing router
    // router.startTrackingRoutedItem((RoutedEntityItem) routedItem.getTravelingItem());

    //TODO: implement
    this.spawnParticle(Particles.ORANGE_SPARKLE, 2);
    this.stat_lifetime_sent++;
    this.stat_session_sent++;
    this.updateStats();
  }

  /**
   * first tick just create a router and do nothing.
   */
  public void firstInitialiseTick() {
    this.getRouter();
    if (this.getLevel().isClientSide) {
      //MainProxy.sendPacketToServer(PacketHandler.getPacket(RequestSignPacket.class).setTilePos(container));
    }
  }

  /**
   * Only Called Server Side Only Called when the pipe is enabled
   */
  public void enabledUpdateEntity() {
    /*powerHandler.update();
    for (int i = 0; i < 6; i++) {
      if (signItem[i] != null) {
        signItem[i].updateServerSide();
      }
    }*/
  }

  /**
   * Called Server and Client Side Called every tick
   */
  public void ignoreDisableUpdateEntity() {}

  // From BaseRoutingLogic
  public void throttledUpdateEntity() {}

  public void checkTexturePowered() {
    if (Configs.LOGISTICS_POWER_USAGE_DISABLED) {
      return;
    }
    if (!isNthTick(10)) {
      return;
    }
    if (stillNeedReplace || initialInit || router == null) {
      return;
    }
    boolean flag;
    if ((flag = canUseEnergy(1)) != textureBufferPowered) {
      textureBufferPowered = flag;
      refreshRender(false);
      spawnParticle(Particles.RED_SPARKLE, 3);
    }
  }

  @Override
  public boolean isNthTick(int n) {
    return (getLevel().getGameTime() + this.delayOffset) % n == 0;
  }

  @Override
  public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.saveAdditional(tag, registries);

    tag.putLong("stat_lifetime_sent", this.stat_lifetime_sent);
    tag.putLong("stat_lifetime_received", this.stat_lifetime_received);
    tag.putLong("stat_lifetime_relayed", this.stat_lifetime_relayed);

    if (this.getLogisticsModule() != null) {
      tag.put("module", getLogisticsModule().serializeNBT(registries));
    }
  }

  @Override
  public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.loadAdditional(tag, registries);

    this.stat_lifetime_sent = tag.getLong("stat_lifetime_sent");
    this.stat_lifetime_received = tag.getLong("stat_session_received");
    this.stat_lifetime_relayed = tag.getLong("stat_lifetime_relayed");

    if (this.getLogisticsModule() != null) {
      getLogisticsModule().deserializeNBT(registries, tag.getCompound("module"));
    }
  }

  public abstract LogisticsModule getLogisticsModule();

  @Override
  public void addStatusInformation(List<StatusEntry> status) {
    StatusEntry entry = new StatusEntry();
    entry.name = "Send Queue";
    entry.subEntry = new ArrayList<>();
    for (Triplet<IRoutedItem, Direction, ItemSendMode> part : this.sendQueue) {
      StatusEntry subEntry = new StatusEntry();
      subEntry.name = part.toString();
      entry.subEntry.add(subEntry);
    }
    status.add(entry);
    entry = new StatusEntry();
    entry.name = "In Transit To Me";
    entry.subEntry = new ArrayList<>();
    for (ItemRoutingInformation part : this.inTransitToMe) {
      StatusEntry subEntry = new StatusEntry();
      subEntry.name = part.toString();
      entry.subEntry.add(subEntry);
    }
    status.add(entry);
  }

  @Override
  public boolean isRoutedPipe() {
    return true;
  }

  @Override
  public DebugLogController getDebug() {
    return this.debug;
  }

  public void collectSpecificInterests(Collection<ItemIdentifier> itemIdentifiers) {}

  public boolean hasGenericInterests() {
    return false;
  }

  @Nullable
  public ItemRoutingInformation getQueuedForItemStack(ItemIdentifierStack item) {
    synchronized (queuedDataForUnroutedItems) {
      Queue<Tuple<Integer, ItemRoutingInformation>> queue = queuedDataForUnroutedItems.get(item.getItem());
      if (queue == null || queue.isEmpty()) {
        return null;
      }

      var pair = queue.peek();
      int wantItem = pair.getA();

      if (wantItem <= item.getStackSize()) {
        if (queue.remove() != pair) {
          LogisticsPipes.LOG.error("Item queue mismatch");
          return null;
        }
        if (queue.isEmpty()) {
          queuedDataForUnroutedItems.remove(item.getItem());
        }
        item.setStackSize(wantItem);
        return pair.getB();
      }
    }
    return null;
  }

  private void notifyOfSend(ItemRoutingInformation routedItem) {
    this.inTransitToMe.add(routedItem);
    //LogisticsPipes.log.info("Sending: "+routedItem.getIDStack().getItem().getFriendlyName());
  }

  public void notifyOfReroute(ItemRoutingInformation routedItem) {
    this.inTransitToMe.remove(routedItem);
  }

  /* ITrackStatistics */

  @Override
  public void receivedItem(int count) {
    this.stat_session_received += count;
    this.stat_lifetime_received += count;
    this.updateStats();
  }

  @Override
  public void relayedItem(int count) {
    this.stat_session_relayed += count;
    this.stat_lifetime_relayed += count;
    this.updateStats();
  }

  private void updateStats() {
  }

  @Override
  public void itemCouldNotBeSend(ItemIdentifierStack item, IAdditionalTargetInformation info) {
    if (this instanceof IRequireReliableTransport requireReliableTransport) {
      requireReliableTransport.itemLost(item, info);
    }
  }

  /* Power System */
  @Override
  public boolean useEnergy(int amount) {
    return true;
  }

  @Override
  public boolean canUseEnergy(int amount) {
    return canUseEnergy(amount, null);
  }

  @Override
  public boolean canUseEnergy(int amount, @Nullable List<Object> providersToIgnore) {
    return true;
  }

  @Override
  public boolean useEnergy(int amount, List<Object> providersToIgnore) {
    return true;
  }

  @Override
  public IRouter getRouter() {
    if (this.stillNeedReplace) {
      LogisticsPipes.LOG.error("Hey, don't get routers for pipes that aren't ready ({}, {}, {})",
          this.getX(), this.getY(), this.getZ(), new Throwable());
    }
    if (this.router == null) {
      synchronized (this.routerIdLock) {
        UUID routerIntId = null;
        if (this.routerId != null && !this.routerId.isEmpty()) {
          routerIntId = UUID.fromString(this.routerId);
        }
        this.router = SimpleServiceLocator.routerManager
            .getOrCreateRouter(routerIntId, this.getLevel(), getX(), getY(), getZ());
      }
    }
    return this.router;
  }

  public RouteLayer getRouteLayer() {
    if (this.routeLayer == null) {
      this.routeLayer = new RouteLayer(getRouter(), this.getTransportLayer(), this);
    }
    return this.routeLayer;
  }

  public PipeTransportLayer getTransportLayer() {
    if (this.transportLayer == null) {
      this.transportLayer = new PipeTransportLayer(this, this, getRouter());
    }
    return this.transportLayer;
  }

  @Override
  public int getID() {
    return this.getRouter().getSimpleID();
  }

  public void refreshRender(boolean spawnPart) {
    if (spawnPart) {
      this.spawnParticle(Particles.GREEN_SPARKLE, 3);
    }
  }

  public void refreshConnectionAndRender(boolean spawnPart) {
    container.scheduleNeighborChange();
    if (spawnPart) {
      this.spawnParticle(Particles.GREEN_SPARKLE, 3);
    }
  }

  public UpgradeManager getOriginalUpgradeManager() {
    return this.upgradeManager;
  }

  @Nullable
  public UUID getSecurityID() {
    return this.getOriginalUpgradeManager().getSecurityID();
  }

  /** Caches adjacent state, only on Side.SERVER */
  private Adjacent adjacent = NoAdjacent.INSTANCE;

  /**
   * @return the adjacent cache directly.
   */
  protected Adjacent getAdjacent() {
    return adjacent;
  }

  /**
   * Returns all adjacents on a regular routed pipe.
   */
  @Override
  public Adjacent getAvailableAdjacent() {
    return getAdjacent();
  }

  /**
   * Re-creates adjacent cache.
   */
  protected void updateAdjacentCache() {
    adjacent = AdjacentFactory.INSTANCE.createAdjacentCache(this);
  }

  /**
   * Designed to help protect against routing loops - if both pipes are on the same block
   *
   * @return boolean indicating if other and this are attached to the same inventory.
   */
  public boolean isOnSameContainer(CoreRoutedPipe other) {
    // FIXME: Same TileEntity? Same Inventory view?
    return this.adjacent.connectedPos().keySet().stream()
        .anyMatch(other.adjacent.connectedPos().keySet()::contains);
  }


  public boolean isLockedExit(Direction orientation) {
    return false;
  }

  @Override
  public void triggerConnectionCheck() {
    this.recheckConnections = true;
  }

  @Override
  public boolean isSideBlocked(Direction direction, boolean ignoreSystemDisconnection) {
    if (getUpgradeManager().isSideDisconnected(direction)) {
      return true;
    }
    return !stillNeedReplace && getRouter().isSideDisconnected(direction) && !ignoreSystemDisconnection;
  }

  public void connectionUpdate() {
    if (container != null && !stillNeedReplace) {
      if (getLevel().isClientSide) {
        throw new IllegalStateException("Wont do connectionUpdate on client-side");
      }
      container.scheduleNeighborChange();
      var state = getLevel().getBlockState(getPos());
      getLevel().updateNeighborsAt(getPos(), state.getBlock());
    }
  }

  @Override
  public CacheHolder getCacheHolder() {
    if (cacheHolder == null) {
      if (container instanceof ILPTEInformation ilpteInformation
          && ilpteInformation.getLPTileEntityObject() != null) {
        cacheHolder = ilpteInformation.getLPTileEntityObject().getCacheHolder();
      } else {
        cacheHolder = new CacheHolder();
      }
    }
    return cacheHolder;
  }

  @Override
  public IPipeUpgradeManager getUpgradeManager() {
    return this.upgradeManager;
  }

  @Override
  public ISlotUpgradeManager getUpgradeManager(LogisticsModule.ModulePositionType slot, int positionInt) {
    return this.upgradeManager;
  }

  public void notifyOfItemArrival(ItemRoutingInformation information) {
    this.inTransitToMe.remove(information);
    if (this instanceof IRequireReliableTransport) {
      ((IRequireReliableTransport) this).itemArrived(information.getItem(), information.targetInfo);
    }
    /*if (this instanceof IRequireReliableFluidTransport) {
      ItemIdentifierStack stack = information.getItem();
      if (stack.getItem().isFluidContainer()) {
        FluidIdentifierStack liquid = SimpleServiceLocator.logisticsFluidManager.getFluidFromContainer(stack);
        if (liquid != null) {
          ((IRequireReliableFluidTransport) this).liquidArrived(liquid.getFluid(), liquid.getAmount());
        }
      }
    }*/
  }

  @Override
  public int countOnRoute(Item item) {
    int count = 0;
    for (ItemRoutingInformation next : this.inTransitToMe) {
      if (next.getItem().getItem().equals(item)) {
        count += next.getItem().getStackSize();
      }
    }
    return count;
  }

  @Override
  public void queueRoutedItem(IRoutedItem routedItem, @Nullable Direction from) {
    if (from == null) {
      throw new NullPointerException();
    }
    this.sendQueue.addLast(new Triplet<>(routedItem, from, ItemSendMode.NORMAL));
    this.sendQueueChanged(false);
  }

  public void queueRoutedItem(IRoutedItem routedItem, @Nullable Direction from, ItemSendMode mode) {
    if (from == null) {
      throw new NullPointerException();
    }
    this.sendQueue.addLast(new Triplet<>(routedItem, from, mode));
    this.sendQueueChanged(false);
  }

  /* ISendRoutedItem */

  @Override
  public IRoutedItem sendStack(ItemStack stack, Tuple<Integer, SinkReply> reply, ItemSendMode mode, Direction direction) {
    IRoutedItem itemToSend = SimpleServiceLocator.routedItemHelper.createNewTravelItem(stack);
    itemToSend.setDestination(reply.getA());
    if (reply.getB().isPassive) {
      itemToSend.setTransportMode(reply.getB().isDefault
          ? IRoutedItem.TransportMode.DEFAULT
          : IRoutedItem.TransportMode.PASSIVE);
    }
    itemToSend.setAdditionalTargetInformation(reply.getB().addInfo);
    queueRoutedItem(itemToSend, direction, mode);
    return itemToSend;
  }

  @Override
  public IRoutedItem sendStack(ItemStack stack, int destination, ItemSendMode mode, IAdditionalTargetInformation info, Direction direction) {
    IRoutedItem itemToSend = SimpleServiceLocator.routedItemHelper.createNewTravelItem(stack);
    itemToSend.setDestination(destination);
    itemToSend.setTransportMode(IRoutedItem.TransportMode.ACTIVE);
    itemToSend.setAdditionalTargetInformation(info);
    queueRoutedItem(itemToSend, direction, mode);
    return itemToSend;
  }

  /**
   * @param force == true never delegates to a thread
   * @return number of things sent.
   */
  public int sendQueueChanged(boolean force) {
    return 0;
  }

  public void setDestroyByPlayer() {
    this.destroyByPlayer = true;
  }

  @Override
  public boolean destroyByPlayer() {
    return this.destroyByPlayer;
  }

  @Override
  public void onBlockRemoval() {
    try {
      this.onAllowedRemoval();
      super.onBlockRemoval();
      //Just in case
      CoreRoutedPipe.PIPE_COUNT = Math.max(CoreRoutedPipe.PIPE_COUNT - 1, 0);

      transport.dropBuffer();
      getOriginalUpgradeManager().dropUpgrades();
    } catch (Exception e) {
      LogisticsPipes.LOG.error("onBlockRemoval", e);
    }
  }

  @Override
  public void writeData(FriendlyByteBuf buf) {
  }

  @Override
  public void readData(FriendlyByteBuf buf) {
  }

  @Override
  public void spawnParticle(Particles particle, int amount) {
    if (!Configs.ENABLE_PARTICLE_FX) {
      return;
    }
    this.queuedParticles[particle.ordinal()] += amount;
    this.hasQueuedParticles = true;
  }

  private void spawnParticleTick() {
    if (!this.hasQueuedParticles) {
      return;
    }
    if (getLevel() instanceof ServerLevel serverLevel) {
      for (int i = 0; i < this.queuedParticles.length; i++) {
        if (this.queuedParticles[i] > 0) {
          var amount = this.queuedParticles[i];
          serverLevel.sendParticles(Particles.values()[i].getSparkleFXParticleOptions(amount),
              getX(), getY(), getZ(), amount, 0, 0, 0, 1);
        }
      }
    } else if (getLevel() instanceof ClientLevel clientLevel) {
      System.out.println("DEBUG: Added particle client side");
      if (!Minecraft.getInstance().options.graphicsMode().get().equals(GraphicsStatus.FAST)) {
        for (int i = 0; i < queuedParticles.length; i++) {
          if (this.queuedParticles[i] > 0) {
            PipeFXRenderHandler.spawnGenericParticle(clientLevel, Particles.values()[i],
                getX(), getY(), getZ(), queuedParticles[i]);
          }
        }
      }
    }
    Arrays.fill(this.queuedParticles, 0);
    this.hasQueuedParticles = false;
  }

  public enum ItemSendMode {
    NORMAL,
    FAST
  }
}
