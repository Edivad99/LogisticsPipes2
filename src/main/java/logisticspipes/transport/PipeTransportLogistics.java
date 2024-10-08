package logisticspipes.transport;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import org.jetbrains.annotations.Nullable;
import logisticspipes.Constants;
import logisticspipes.LogisticsPipes;
import logisticspipes.api.ILogisticsPowerProvider;
import logisticspipes.interfaces.IBufferItems;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.IItemAdvancedExistence;
import logisticspipes.interfaces.ISlotUpgradeManager;
import logisticspipes.interfaces.ISpecialInsertion;
import logisticspipes.interfaces.ISubSystemPowerProvider;
import logisticspipes.interfaces.routing.ITargetSlotInformation;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.network.to_client.ItemBufferSyncPacket;
import logisticspipes.network.to_client.PipeContentPacket;
import logisticspipes.network.to_client.PipePositionPacket;
import logisticspipes.network.to_server.PipeContentRequest;
import logisticspipes.pipes.PipeLogisticsChassis;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.ItemRoutingInformation;
import logisticspipes.routing.pathfinder.IPipeInformationProvider;
import logisticspipes.utils.CacheHolder;
import logisticspipes.utils.CoordinateUtils;
import logisticspipes.utils.DoubleCoordinates;
import logisticspipes.utils.InventoryHelper;
import logisticspipes.utils.OrientationsUtil;
import logisticspipes.utils.SyncList;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.tuples.Triplet;
import logisticspipes.world.level.block.entity.ChassiPipeBlockEntity;
import logisticspipes.world.level.block.entity.LogisticsGenericPipeBlockEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.network.PacketDistributor;

public class PipeTransportLogistics {

  @Data
  @AllArgsConstructor
  public static class RoutingResult {
    @Nullable
    private Direction face;
    private boolean hasRoute;
  }

  private static final int MAX_DESTINATION_UNREACHABLE_BUFFER = 30;

  private final int bufferTimeOut = 20 * 2; // 2 Seconds
  public final SyncList<Triplet<ItemIdentifierStack, Tuple<Integer /* Time */, Integer /* BufferCounter */>, LPTravelingItem.LPTravelingItemServer>> itemBuffer = new SyncList<>();
  @Nullable
  private LevelChunk chunk;
  public LPItemList items = new LPItemList(this);
  @Getter
  @Nullable
  private LogisticsGenericPipeBlockEntity<? extends CoreUnroutedPipe> container;
  public final boolean isRouted;

  public PipeTransportLogistics(boolean isRouted) {
    this.isRouted = isRouted;
  }

  public void initialize() {
    if (this.getLevel() instanceof ServerLevel serverLevel) {
      Objects.requireNonNull(this.container, "setBlockEntity must be called before initialize");
      this.chunk = this.getLevel().getChunkAt(this.container.getBlockPos());
      this.itemBuffer.setPacketType(
          new ItemBufferSyncPacket(container.getBlockPos(), new ArrayList<>()),
          serverLevel, new ChunkPos(this.container.getBlockPos()));
    }
  }

  public void setBlockEntity(LogisticsGenericPipeBlockEntity<? extends CoreUnroutedPipe> blockEntity) {
    this.container = blockEntity;
  }

  public boolean canPipeConnect(BlockEntity blockEntity, Direction side) {
    return this.canPipeConnectInternal(blockEntity, side);
  }

  public final boolean canPipeConnectInternal(BlockEntity blockEntity, Direction side) {
    if (this.isRouted) {
      if (blockEntity instanceof ILogisticsPowerProvider || blockEntity instanceof ISubSystemPowerProvider) {
        Direction ori = OrientationsUtil.getOrientationOfTilewithTile(container, blockEntity);
        if (ori != null) {
          return !((/*blockEntity instanceof LogisticsPowerJunctionTileEntity ||*/
              blockEntity instanceof ISubSystemPowerProvider) && ori.getAxis() == Direction.Axis.Y);
        }
      }
      IInventoryUtil util = SimpleServiceLocator.inventoryUtilFactory.getInventoryUtil(blockEntity, side.getOpposite());
      if (util != null) {
        return util.getSizeInventory() > 0;
      }
      return isPipeCheck(blockEntity);
    } else {
      return isPipeCheck(blockEntity);
    }
  }

  protected boolean isPipeCheck(BlockEntity blockEntity) {
    return SimpleServiceLocator.pipeInformationManager.isItemPipe(blockEntity);
  }

  @Nullable
  public Level getLevel() {
    Objects.requireNonNull(this.container, "setBlockEntity must be called before getLevel");
    return this.container.getLevel();
  }

  public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    var tagsList1 = new ListTag();
    for (var item : this.items) {
      if (item instanceof LPTravelingItem.LPTravelingItemServer itemServer) {
        var dataTag = new CompoundTag();
        tagsList1.add(dataTag);
        itemServer.writeToNBT(registries, dataTag);
      }
    }
    tag.put("travelingEntities", tagsList1);

    var tagList2 = new ListTag();
    for (var stack : this.itemBuffer) {
      var dataTag = new CompoundTag();
      stack.getA().makeNormalStack().save(registries, dataTag);
      tagList2.add(dataTag);
    }
    tag.put("buffercontents", tagList2);
  }

  public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    var tagsList1 = tag.getList("travelingEntities", Tag.TAG_COMPOUND);
    for (int i = 0; i < tagsList1.size(); i++) {
      var dataTag = tagsList1.getCompound(i);
      var item = new LPTravelingItem.LPTravelingItemServer(registries, dataTag);
      if (item.isCorrupted()) {
        continue;
      }
      this.items.scheduleLoad(item);
    }

    this.itemBuffer.clear();

    var tagList2 = tag.getList("buffercontents", Tag.TAG_COMPOUND);
    for (int i = 0; i < tagList2.size(); i++) {
      var dataTag = tagList2.getCompound(i);
      var stack = ItemIdentifierStack.parse(registries, dataTag);
      this.itemBuffer.add(new Triplet<>(stack, new Tuple<>(this.bufferTimeOut, 0), null));
    }
  }

  public void readjustSpeed(LPTravelingItem.LPTravelingItemServer item) {
    float defaultBoost = switch (item.getTransportMode()) {
      case DEFAULT, UNKNOWN -> 20F;
      case PASSIVE -> 25F;
      case ACTIVE -> 30F;
    };

    if (isRouted) {
      var speedUpgradeCount = getRoutedPipe().getUpgradeManager().getSpeedUpgradeCount();
      float multiplayerPower = 1.0F + (0.03F * speedUpgradeCount);
      float multiplayerSpeed = 1.0F + (0.02F * speedUpgradeCount);

      float add = Math.max(item.getSpeed(), Constants.PIPE_NORMAL_SPEED * defaultBoost * multiplayerPower) - item.getSpeed();
      if (getRoutedPipe().useEnergy((int) (add * 50 + 0.5))) {
        item.setSpeed(Math.min(Math.max(item.getSpeed(), Constants.PIPE_NORMAL_SPEED * defaultBoost * multiplayerSpeed), 1.0F));
      }
    }
  }

  protected CoreUnroutedPipe getPipe() {
    Objects.requireNonNull(this.container, "setBlockEntity must be called before getPipe");
    return this.container.pipe;
  }

  protected CoreRoutedPipe getRoutedPipe() {
    if (!isRouted) {
      throw new UnsupportedOperationException("Can't use a Transport pipe as a routing pipe");
    }
    return (CoreRoutedPipe) this.getPipe();
  }

  public void updateEntity() {
    moveSolids();
    if (getLevel() instanceof ServerLevel) {
      if (!this.itemBuffer.isEmpty()) {
        List<LPTravelingItem> toAdd = new LinkedList<>();
        var iterator = this.itemBuffer.iterator();
        while (iterator.hasNext()) {
          var next = iterator.next();
          int currentTimeOut = next.getB().getA();
          if (currentTimeOut > 0) {
            next.getB().setA(currentTimeOut - 1);
          } else if (next.getC() != null) {
            if (getRoutedPipe().getRouter().hasRoute(next.getC().getDestination(),
                next.getC().getTransportMode() == IRoutedItem.TransportMode.ACTIVE,
                next.getC().getItemIdentifierStack().getItem()) || next.getB().getB() > MAX_DESTINATION_UNREACHABLE_BUFFER) {
              next.getC().setBufferCounter(next.getB().getB() + 1);
              toAdd.add(next.getC());
              iterator.remove();
            } else {
              next.getB().setB(next.getB().getB() + 1);
              next.getB().setA(bufferTimeOut);
            }
          } else {
            LPTravelingItem.LPTravelingItemServer item = SimpleServiceLocator.routedItemHelper.createNewTravelItem(next.getA());
            item.setDoNotBuffer(true);
            item.setBufferCounter(next.getB().getB() + 1);
            toAdd.add(item);
            iterator.remove();
          }
        }
        for (LPTravelingItem item : toAdd) {
          this.injectItem(item, Direction.UP);
        }
      }
      this.itemBuffer.sendUpdateToWaters();
    }
  }

  private void moveSolids() {
    items.flush();
    items.scheduleAdd();
    for (LPTravelingItem item : items) {
      if (item.lastTicked >= MainProxy.getGlobalTick()) {
        continue;
      }
      item.lastTicked = MainProxy.getGlobalTick();
      item.addAge();
      item.setPosition(item.getPosition() + item.getSpeed());
      if (this.hasReachedEnd(item)) {
        if (item.output == null) {
          if (getLevel() instanceof ServerLevel) {
            dropItem((LPTravelingItem.LPTravelingItemServer) item);
          }
          items.scheduleRemoval(item);
          continue;
        }
        reachedEnd(item);
      }
    }
    items.removeScheduledItems();
    items.addScheduledItems();
  }

  protected boolean hasReachedEnd(LPTravelingItem item) {
    return item.getPosition() >= ((item.output == null) ? 0.75F : getPipeLength());
  }

  public void dropBuffer() {
    var iterator = this.itemBuffer.iterator();
    while (iterator.hasNext()) {
      var next = iterator.next().getA();
      var item = new ItemEntity(getLevel(), getPipe().getX(), getPipe().getY(), getPipe().getZ(),
          next.makeNormalStack());
      getLevel().addFreshEntity(item);
      iterator.remove();
    }
  }

  public int injectItem(LPTravelingItem.LPTravelingItemServer item, @Nullable Direction inputOrientation) {
    return this.injectItem((LPTravelingItem) item, inputOrientation);
  }

  public int injectItem(LPTravelingItem item, @Nullable Direction inputOrientation) {
    if(item.isCorrupted()) {
      // Safe guard - if for any reason the item is corrupted at this
      // stage, avoid adding it to the pipe to avoid further exceptions.
      return 0;
    }
    this.getPipe().triggerDebug();

    int originalCount = item.getItemIdentifierStack().getStackSize();

    item.input = inputOrientation;

    if (!container.getLevel().isClientSide) {
      var serverItem = (LPTravelingItem.LPTravelingItemServer) item;
      readjustSpeed(serverItem);
      ItemRoutingInformation info1 = serverItem.getInfo().clone();
      RoutingResult result = resolveDestination(serverItem);
      item.output = result.getFace();
      if (!result.hasRoute) {
        return 0;
      }
      //getPipe().debug.log("Injected Item: [" + item.input + ", " + item.output + "] (" + info1);
    } else {
      item.output = null;
    }

    if (item.getPosition() >= this.getPipeLength()) {
      this.reachedEnd(item);
    } else {
      this.items.add(item);

      if (!container.getLevel().isClientSide && !item.getItemIdentifierStack().isEmpty()) {
        this.sendItemPacket((LPTravelingItem.LPTravelingItemServer) item);
      }
    }
    return originalCount - item.getItemIdentifierStack().getStackSize();
  }

  public int injectItem(IRoutedItem item, Direction inputOrientation) {
    return injectItem((LPTravelingItem) SimpleServiceLocator.routedItemHelper.getServerTravelingItem(item), inputOrientation);
  }

  protected void reachedEnd(LPTravelingItem item) {
    var blockEntity = container.getBlockEntity(item.output);
    if (items.scheduleRemoval(item)) {
      if (container.getLevel().isClientSide) {
        if (item instanceof LPTravelingItem.LPTravelingItemClient itemClient) {
          handleTileReachedClient(itemClient, blockEntity, item.output);
        } else {
          LogisticsPipes.LOG.error("Tried to handle a server item as client item");
        }
      } else {
        if (item instanceof LPTravelingItem.LPTravelingItemServer itemServer) {
          handleTileReachedServer(itemServer, blockEntity, item.output);
        } else {
          LogisticsPipes.LOG.error("Tried to handle a client item as server item");
        }
      }
    }
  }

  protected void handleTileReachedClient(LPTravelingItem.LPTravelingItemClient arrivingItem,
      BlockEntity blockEntity, Direction dir) {
    if (SimpleServiceLocator.pipeInformationManager.isItemPipe(blockEntity)) {
      passToNextPipe(arrivingItem, blockEntity);
    }
    // Just ignore any other case
  }

  protected boolean passToNextPipe(LPTravelingItem item, BlockEntity blockEntity) {
    IPipeInformationProvider information = SimpleServiceLocator.pipeInformationManager.getInformationProviderFor(blockEntity);
    if (information != null) {
      item.setPosition(item.getPosition() - getPipeLength());
      item.setYaw(item.getYaw() + getYawDiff(item));
      return information.acceptItem(item, container);
    }
    return false;
  }

  protected void handleTileReachedServer(LPTravelingItem.LPTravelingItemServer arrivingItem,
      BlockEntity blockEntity, Direction dir) {
    handleTileReachedServer_internal(arrivingItem, blockEntity, dir);
  }

  protected final void handleTileReachedServer_internal(
      LPTravelingItem.LPTravelingItemServer arrivingItem, BlockEntity tile, Direction dir) {
    /*if (getPipe() instanceof PipeItemsFluidSupplier supplier) {
      supplier.endReached(arrivingItem, tile);
      if (arrivingItem.getItemIdentifierStack().isEmpty()) {
        return;
      }
    }*/

    markChunkModified(tile);
    if (arrivingItem.getInfo() != null && arrivingItem.getArrived() && isRouted) {
      getRoutedPipe().notifyOfItemArrival(arrivingItem.getInfo());
    }
    /*if (getPipe() instanceof FluidRoutedPipe fluidRoutedPipe) {
      if (fluidRoutedPipe.endReached(arrivingItem, tile)) {
        return;
      }
    }*/
    boolean isSpecialConnectionInformationTransition = false;
    if (SimpleServiceLocator.specialtileconnection.needsInformationTransition(tile)) {
      isSpecialConnectionInformationTransition = true;
      SimpleServiceLocator.specialtileconnection.transmit(tile, arrivingItem);
    }
    if (SimpleServiceLocator.pipeInformationManager.isItemPipe(tile)) {
      if (passToNextPipe(arrivingItem, tile)) {
        return;
      }
    } else {
      IInventoryUtil util = SimpleServiceLocator.inventoryUtilFactory.getInventoryUtil(tile, dir.getOpposite());
      if (util != null && isRouted) {
        getRoutedPipe().getCacheHolder().trigger(CacheHolder.CacheTypes.INVENTORY);

        // items.scheduleRemoval(arrivingItem);
        // destroy the item on exit if it isn't exitable
        if (!isSpecialConnectionInformationTransition && isItemUnwanted(arrivingItem.getItemIdentifierStack())) {
          return;
        }
        // last chance for chassi to back out
        if (arrivingItem.getTransportMode() != IRoutedItem.TransportMode.ACTIVE && !getRoutedPipe().getTransportLayer().stillWantItem(arrivingItem)) {
          reverseItem(arrivingItem);
          return;
        }
        final ISlotUpgradeManager slotManager;
        {
          LogisticsModule.ModulePositionType slot = null;
          int positionInt = -1;
          if (arrivingItem.getInfo().targetInfo instanceof ChassiPipeBlockEntity.ChassiTargetInformation) {
            positionInt = ((ChassiPipeBlockEntity.ChassiTargetInformation) arrivingItem.getInfo().targetInfo).getModuleSlot();
            slot = LogisticsModule.ModulePositionType.SLOT;
          } else if (LogisticsPipes.isDebug() && container.pipe instanceof PipeLogisticsChassis) {
            System.out.println(arrivingItem);
            new RuntimeException("[ItemInsertion] Information weren't ment for a chassi pipe").printStackTrace();
          }
          slotManager = getRoutedPipe().getUpgradeManager(slot, positionInt);
        }
        if (arrivingItem.getAdditionalTargetInformation() instanceof ITargetSlotInformation information) {
          if (util instanceof ISpecialInsertion) {
            int slot = information.getTargetSlot();
            int amount = information.getAmount();
            if (util.getSizeInventory() > slot) {
              ItemStack content = util.getStackInSlot(slot);
              ItemStack toAdd = arrivingItem.getItemIdentifierStack().makeNormalStack();
              final int amountLeft = Math.max(0, amount - content.getCount());
              toAdd.setCount(Math.min(toAdd.getCount(), amountLeft));
              if (toAdd.getCount() > 0) {
                if (util.getSizeInventory() > slot) {
                  int added = ((ISpecialInsertion) util).addToSlot(toAdd, slot);
                  arrivingItem.getItemIdentifierStack().lowerStackSize(added);
                }
              }
            }
            if (information.isLimited()) {
              if (arrivingItem.getItemIdentifierStack().getStackSize() > 0) {
                reverseItem(arrivingItem);
              }
              return;
            }
          }
        }
        // sneaky insertion
        if (!getRoutedPipe().getUpgradeManager().hasCombinedSneakyUpgrade() || slotManager.hasOwnSneakyUpgrade()) {
          Direction insertion = arrivingItem.output.getOpposite();
          if (slotManager.hasSneakyUpgrade()) {
            insertion = slotManager.getSneakyOrientation();
          }
          if (insertArrivingItem(arrivingItem, tile, insertion)) return;
        } else {
          Direction[] dirs = getRoutedPipe().getUpgradeManager().getCombinedSneakyOrientation();
          for (Direction insertion : dirs) {
            if (insertion == null) {
              continue;
            }
            if (insertArrivingItem(arrivingItem, tile, insertion)) return;
          }
        }

        if (arrivingItem.getItemIdentifierStack().getStackSize() > 0) {
          reverseItem(arrivingItem);
        }
        return;// the item is handled
      }// end of insert into IInventory
    }
    dropItem(arrivingItem);
  }

  private void dropItem(LPTravelingItem.LPTravelingItemServer item) {
    if (!(container.getLevel() instanceof ServerLevel serverLevel)) {
      return;
    }
    item.setSpeed(0.05F);
    item.setContainer(container);
    var entity = item.toEntityItem();
    if (entity != null) {
      serverLevel.addFreshEntity(entity);
    }
  }

  /**
   * @return true, if every item has been inserted and otherwise false.
   */
  private boolean insertArrivingItem(LPTravelingItem.LPTravelingItemServer arrivingItem,
      BlockEntity tile, Direction insertion) {
    ItemStack added = InventoryHelper.getTransactorFor(tile, insertion)
        .add(arrivingItem.getItemIdentifierStack().makeNormalStack(), insertion, true);

    if (!added.isEmpty()) {
      arrivingItem.getItemIdentifierStack().lowerStackSize(added.getCount());
      arrivingItem.setBufferCounter(0);
    }

    ItemRoutingInformation info = arrivingItem.getInfo();
    final boolean isSplitStack = !arrivingItem.getItemIdentifierStack().isEmpty();
    if (isSplitStack) {
      // we have some leftovers, we are splitting the stack, we need to clone the info
      info = info.clone();
    }
    if(added.isEmpty()){
      info.getItem().setStackSize(0);
    } else {
      info.getItem().setStackSize(added.getCount());
    }

    inventorySystemConnectorHook(info, tile);

    // back to normal code, break if we've inserted everything, all items disposed of.
    return !isSplitStack;
  }

  /**
   * emit the supplied item. This function assumes ownershop of the item, and
   * you may assume that it is now either buffered by the pipe or moving
   * through the pipe.
   *
   * @param item
   *            the item that just bounced off an inventory. In the case of a
   *            pipe with a buffer, this function will alter item.
   */
  protected void reverseItem(LPTravelingItem.LPTravelingItemServer item) {
    if (item.isCorrupted()) {
      // Safe guard - if for any reason the item is corrupted at this
      // stage, avoid adding it to the pipe to avoid further exceptions.
      return;
    }

    if (getPipe() instanceof IBufferItems buffer) {
      item.getItemIdentifierStack().setStackSize(buffer.addToBuffer(item.getItemIdentifierStack(), item.getAdditionalTargetInformation()));
      if (item.getItemIdentifierStack().isEmpty()) {
        return;
      }
    }

    // Assign new ID to update ItemStack content
    item.id = item.getNextId();

    if (item.getPosition() >= getPipeLength()) {
      item.setPosition(item.getPosition() - getPipeLength());
    }

    item.input = item.output.getOpposite();

    readjustSpeed(item);
    RoutingResult result = resolveDestination(item);
    item.output = result.getFace();
    if (!result.hasRoute) {
      return;
    } else if (item.output == null) {
      dropItem(item);
      return;
    }

    items.unscheduleRemoval(item);
    if (!getPipe().isOpaque()) {
      sendItemPacket(item);
    }
  }

  public void markChunkModified(@Nullable BlockEntity blockEntity) {
    if (blockEntity != null && chunk != null) {
      // items are crossing a chunk boundary, mark both chunks modified
      var containerPos = container.getBlockPos();
      var tilePos = blockEntity.getBlockPos();
      if (containerPos.getX() >> 4 != tilePos.getX() >> 4 || containerPos.getZ() >> 4 != tilePos.getZ() >> 4) {
        chunk.setUnsaved(true);
        if (blockEntity instanceof LogisticsGenericPipeBlockEntity<?> pipeBlockEntity
            && pipeBlockEntity.pipe.getTransport().chunk != null) {
          pipeBlockEntity.pipe.getTransport().chunk.setUnsaved(true);
        } else {
          this.getLevel().getChunk(tilePos).setUnsaved(true);
        }
      }
    }
  }

  public float getPipeLength() {
    return 1;
  }

  public double getDistanceWeight() {
    return 1;
  }

  public float getYawDiff(LPTravelingItem item) {
    return 0;
  }

  protected void inventorySystemConnectorHook(ItemRoutingInformation info, BlockEntity blockEntity) {

  }


  public RoutingResult resolveDestination(LPTravelingItem.LPTravelingItemServer data) {
    if (isRouted) {
      return resolveRoutedDestination(data);
    } else {
      return resolveUnroutedDestination(data);
    }
  }

  public RoutingResult resolveUnroutedDestination(LPTravelingItem.LPTravelingItemServer data) {
    List<Direction> dirs = new ArrayList<>(Arrays.asList(Direction.values()));
    dirs.remove(data.input.getOpposite());
    Iterator<Direction> iter = dirs.iterator();
    while (iter.hasNext()) {
      Direction dir = iter.next();
      DoubleCoordinates pos = CoordinateUtils.add(getPipe().getLPPosition(), dir);
      BlockEntity blockEntity = pos.getBlockEntity(getLevel());
      if (!SimpleServiceLocator.pipeInformationManager.isItemPipe(blockEntity)) {
        iter.remove();
      } else if (!canPipeConnect(blockEntity, dir)) {
        iter.remove();
      } else if (blockEntity instanceof LogisticsGenericPipeBlockEntity<?> pipeBlockEntity &&
          !pipeBlockEntity.canConnect(container, dir.getOpposite(), false)) {
        iter.remove();
      }
    }
    if (dirs.isEmpty()) {
      return new RoutingResult(null, false);
    }
    int num = new Random().nextInt(dirs.size());
    return new RoutingResult(dirs.get(num), true);
  }

  public RoutingResult resolveRoutedDestination(LPTravelingItem.LPTravelingItemServer data) {

    Direction blocked = null;

    if (data.getDestinationUUID() == null) {
      ItemIdentifierStack stack = data.getItemIdentifierStack();
      ItemRoutingInformation result = getRoutedPipe().getQueuedForItemStack(stack);
      if (result != null) {
        data.setInformation(result);
        data.getInfo().setItem(stack);
        blocked = data.input.getOpposite();
      }
    }

    if (data.getItemIdentifierStack() != null) {
      getRoutedPipe().relayedItem(data.getItemIdentifierStack().getStackSize());
    }

    if (data.getDestination() >= 0 &&
        !getRoutedPipe().getRouter().hasRoute(data.getDestination(),
            data.getTransportMode() == IRoutedItem.TransportMode.ACTIVE,
            data.getItemIdentifierStack().getItem())
        && data.getBufferCounter() < MAX_DESTINATION_UNREACHABLE_BUFFER) {
      itemBuffer.add(new Triplet<>(data.getItemIdentifierStack(),
          new Tuple<>(bufferTimeOut, data.getBufferCounter()), data));
      return new RoutingResult(null, false);
    }

    Direction value;
    if (getRoutedPipe().isStillNeedReplace() || getRoutedPipe().isInitialInit()) {
      data.setDoNotBuffer(false);
      value = null;
    } else {
      value = getRoutedPipe().getRouteLayer().getOrientationForItem(data, blocked);
    }
    if (value == null && this.getLevel().isClientSide) {
      return new RoutingResult(null, true);
    }

    if (value == null && !data.getDoNotBuffer() && data.getBufferCounter() < 5) {
      itemBuffer.add(new Triplet<>(data.getItemIdentifierStack(),
          new Tuple<>(bufferTimeOut, data.getBufferCounter()), null));
      return new RoutingResult(null, false);
    }

    if (value != null && !getRoutedPipe().getRouter().isRoutedExit(value)) {
      if (isItemUnwanted(data.getItemIdentifierStack())) {
        return new RoutingResult(null, false);
      }
    }

    data.resetDelay();

    return new RoutingResult(value, true);
  }

  protected boolean isItemUnwanted(@Nullable ItemIdentifierStack itemIdentifierStack) {
    if (itemIdentifierStack != null &&
        itemIdentifierStack.makeNormalStack().getItem() instanceof IItemAdvancedExistence itemAdvancedExistence) {
      return !itemAdvancedExistence.canExistInNormalInventory(itemIdentifierStack.makeNormalStack());
    }
    return false;
  }

  public boolean delveIntoUnloadedChunks() {
    return true;
  }

  private void sendItemPacket(LPTravelingItem.LPTravelingItemServer item) {
    if (!LPTravelingItem.CLIENT_SIDE_KNOWN_IDS.get(item.getId())) {
      PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) container.getLevel(),
          new ChunkPos(container.getBlockPos()),
          new PipeContentPacket(item.getItemIdentifierStack(), item.getId()));
      LPTravelingItem.CLIENT_SIDE_KNOWN_IDS.set(item.getId());
    }
    PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) container.getLevel(),
        new ChunkPos(container.getBlockPos()),
        new PipePositionPacket(container.getBlockPos(),
            item.getId(), item.getSpeed(), item.getPosition(),
            item.input, item.output, item.getYaw()));
    /*if (MainProxy.isAnyoneWatching(container.getPos(), getWorld().provider.getDimension())) {
      if (!LPTravelingItem.clientSideKnownIDs.get(item.getId())) {
        MainProxy.sendPacketToAllWatchingChunk(container, (PacketHandler.getPacket(PipeContentPacket.class).setItem(item.getItemIdentifierStack()).setTravelId(item.getId())));
        LPTravelingItem.clientSideKnownIDs.set(item.getId());
      }
      MainProxy.sendPacketToAllWatchingChunk(container, (PacketHandler.getPacket(PipePositionPacket.class).setSpeed(item.getSpeed()).setPosition(item.getPosition()).setInput(item.input).setOutput(item.output).setTravelId(item.getId()).setYaw(item.getYaw()).setTilePos(container)));
    }*/
  }

  public void handleItemPositionPacket(int travelId, @Nullable Direction input,
      @Nullable Direction output, float speed, float position, float yaw) {
    var ref = LPTravelingItem.CLIENT_LIST.get(travelId);
    LPTravelingItem.LPTravelingItemClient item = null;
    if (ref != null) {
      item = ref.get();
    }
    if (item == null) {
      sendItemContentRequest(travelId);
      item = new LPTravelingItem.LPTravelingItemClient(travelId, position, input, output, yaw);
      item.setSpeed(speed);
      LPTravelingItem.CLIENT_LIST.put(travelId, new WeakReference<>(item));
    } else {
      if (item.getContainer() instanceof LogisticsGenericPipeBlockEntity<?> pipeBlockEntity) {
        pipeBlockEntity.pipe.getTransport().items.scheduleRemoval(item);
        pipeBlockEntity.pipe.getTransport().items.removeScheduledItems();
      }
      item.updateInformation(input, output, speed, position, yaw);
    }
    //update lastTicked so we don't double-move items
    item.lastTicked = MainProxy.getGlobalTick();
    if (items.get(travelId) == null) {
      items.add(item);
    }
    //getPipe().spawnParticle(Particles.OrangeParticle, 1);
  }

  private void sendItemContentRequest(int travelId) {
    PacketDistributor.sendToServer(new PipeContentRequest(travelId));
  }

  @Nullable
  public CoreUnroutedPipe getNextPipe(Direction output) {
    var tile = container.getBlockEntity(output);
    if (tile instanceof LogisticsGenericPipeBlockEntity<?> pipeBlockEntity) {
      return pipeBlockEntity.pipe;
    }
    return null;
  }

  public void onNeighborBlockChange() {
  }

  public void onBlockPlaced() {
  }
}
