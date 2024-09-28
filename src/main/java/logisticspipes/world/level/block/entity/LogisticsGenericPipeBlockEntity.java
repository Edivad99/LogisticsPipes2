package logisticspipes.world.level.block.entity;

import java.util.Arrays;
import java.util.EnumMap;
import org.jetbrains.annotations.Nullable;
import logisticspipes.client.renderer.LogisticsTileRenderController;
import logisticspipes.client.renderer.blockentity.state.PipeRenderState;
import logisticspipes.fromkotlin.PipeInventoryConnectionChecker;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.pipes.basic.ItemInsertionHandler;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.pathfinder.IPipeInformationProvider;
import logisticspipes.transport.LPTravelingItem;
import logisticspipes.utils.ConnectionType;
import logisticspipes.utils.TileBuffer;
import logisticspipes.world.level.block.GenericPipeBlock;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

public abstract class LogisticsGenericPipeBlockEntity<T extends CoreUnroutedPipe> extends BlockEntity implements
    IPipeInformationProvider {

  public static PipeInventoryConnectionChecker pipeInventoryConnectionChecker = new PipeInventoryConnectionChecker();

  public T pipe;
  @Getter
  private final LogisticsTileRenderController<T> renderController;
  public boolean[] pipeConnectionsBuffer = new boolean[6];
  public final PipeRenderState renderState;
  private boolean addedToNetwork = false;
  private boolean sendInitPacket = true;
  @Getter
  private boolean initialized = false;
  private boolean deletePipe = false;
  @Nullable
  private TileBuffer[] tileBuffer = null;
  private boolean sendClientUpdate = false;
  private boolean blockNeighborChange = false;
  private boolean refreshRenderState = false;
  private boolean pipeBound = false;

  private final EnumMap<Direction, ItemInsertionHandler<T>> itemInsertionHandlers;

  public LogisticsGenericPipeBlockEntity(BlockEntityType<?> type, BlockPos pos,
      BlockState blockState, T pipe) {
    super(type, pos, blockState);
    this.pipe = pipe;
    this.renderController = new LogisticsTileRenderController<>(this);
    this.renderState = new PipeRenderState();
    this.itemInsertionHandlers = new EnumMap<>(Direction.class);
    Arrays.stream(Direction.values())
        .forEach(face -> itemInsertionHandlers.put(face, new ItemInsertionHandler<>(this, face)));
  }

  @Override
  protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.saveAdditional(tag, registries);
    this.pipe.saveAdditional(tag, registries);
  }

  @Override
  protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.loadAdditional(tag, registries);
    this.pipe.loadAdditional(tag, registries);
  }

  @Override
  public boolean canConnect(BlockEntity to, Direction direction, boolean ignoreSystemDisconnection) {
    return pipe.canPipeConnect(to, direction);
  }

  public void initialize() {
    this.bindPipe();
    //this.computeConnections();
    this.scheduleRenderUpdate();

    if (this.pipe.needsInit()) {
      this.pipe.initialize();
    }
    this.initialized = true;
  }

  @Override
  public void setRemoved() {
    if (this.pipe == null) {
      this.remove = false;
      this.initialized = false;
      this.tileBuffer = null;
      super.setRemoved();
    } else if (!this.pipe.preventRemove()) {
      this.remove = true;
      this.initialized = false;
      this.tileBuffer = null;
      this.pipe.setRemoved();
      super.setRemoved();
    }
  }

  @Override
  public void clearRemoved() {
    super.clearRemoved();
    this.initialized = false;
    this.tileBuffer = null;
    this.bindPipe();
    if (this.pipe != null) {
      this.pipe.validate();
    }
  }

  @Override
  public void onChunkUnloaded() {
    super.onChunkUnloaded();
    if (this.pipe != null) {
      this.pipe.onChunkUnload();
    }
  }

  @Nullable
  public IItemHandler getItemHandlerCapability(@Nullable Direction side) {
    if (side == null) {
      return null;
    }
    var blockEntity = this.getBlockEntity(side);
    if (blockEntity != null) {
      if (pipeInventoryConnectionChecker.shouldLPProvideInventoryTo(blockEntity)) {
        return itemInsertionHandlers.get(side);
      }
    }
    return null;
  }

  @Override
  public BlockEntity getBlockEntity() {
    return this;
  }

  @Override
  public boolean isRouterInitialized() {
    return isInitialized() && (!isRoutingPipe() || !getRoutingPipe().isStillNeedReplace());
  }

  @Override
  public boolean isRoutingPipe() {
    return this.pipe instanceof CoreRoutedPipe;
  }

  @Override
  public CoreRoutedPipe getRoutingPipe() {
    if (this.pipe instanceof CoreRoutedPipe coreRoutedPipe) {
      return coreRoutedPipe;
    }
    throw new RuntimeException("This is no routing pipe");
  }

  @Override
  public BlockEntity getNextConnectedBlockEntity(Direction direction) {
    /*if (this.pipe.isMultiBlock()) {
      return ((CoreMultiBlockPipe) this.pipe).getConnectedEndTile(direction);
    }*/
    return this.getBlockEntity(direction, false);
  }

  @Nullable
  public BlockEntity getBlockEntity(Direction to) {
    return this.getBlockEntity(to, false);
  }

  public TileBuffer[] getTileCache() {
    if (this.tileBuffer == null) {
      this.tileBuffer = TileBuffer.makeBuffer(this.level, this.getBlockPos(),
          pipe.getTransport().delveIntoUnloadedChunks());
    }
    return this.tileBuffer;
  }

  @Nullable
  private BlockEntity getBlockEntity(Direction direction, boolean force) {
    TileBuffer[] cache = getTileCache();
    if (cache != null) {
      if (force) {
        cache[direction.ordinal()].refresh();
      }
      return cache[direction.ordinal()].getBlockEntity();
    } else {
      return null;
    }
  }

  @Override
  public double getDistance() {
    return this.pipe.getTransport().getPipeLength();
  }

  @Override
  public double getDistanceWeight() {
    return this.pipe.getTransport().getDistanceWeight();
  }

  public ItemStack insertItem(Direction from, ItemStack stack) {
    int used = injectItem(stack, true, from);
    if (used == stack.getCount()) {
      return ItemStack.EMPTY;
    } else {
      stack = stack.copy();
      stack.shrink(used);
      return stack;
    }
  }

  public int injectItem(ItemStack payload, boolean doAdd, Direction from) {
    if (GenericPipeBlock.isValid(pipe) && this.isPipeConnectedCached(from)) {
      if (doAdd && !this.level.isClientSide) {
        ItemStack leftStack = payload.copy();
        int lastIterLeft;
        do {
          lastIterLeft = leftStack.getCount();
          LPTravelingItem.LPTravelingItemServer travelingItem = SimpleServiceLocator.routedItemHelper.createNewTravelItem(leftStack);
          leftStack.setCount(pipe.getTransport().injectItem(travelingItem, from.getOpposite()));
        } while (leftStack.getCount() != lastIterLeft && leftStack.getCount() != 0);
        return payload.getCount() - leftStack.getCount();
      }
    }
    return 0;
  }

  public boolean isPipeConnectedCached(Direction side) {
    if (this.level.isClientSide) {
      return renderState.pipeConnectionMatrix.isConnected(side);
    } else {
      return pipeConnectionsBuffer[side.ordinal()];
    }
  }

  private void bindPipe() {
    if (!this.pipeBound && this.pipe != null) {
      //pipe.setTile(this);
      this.pipeBound = true;
    }
  }

  public boolean canPipeConnect(@Nullable BlockEntity with, Direction direction) {
    if (this.level.isClientSide) {
      return false;
    }

    if (with == null) {
      return false;
    }

    if (!GenericPipeBlock.isValid(pipe)) {
      return false;
    }

    if (with instanceof LogisticsGenericPipeBlockEntity) {
      CoreUnroutedPipe otherPipe = ((LogisticsGenericPipeBlockEntity<?>) with).pipe;

      if (!(GenericPipeBlock.isValid(otherPipe))) {
        return false;
      }

      if (!(otherPipe.canPipeConnect(this, direction.getOpposite()))) {
        return false;
      }
    }
    return pipe.canPipeConnect(with, direction);
  }

  @Override
  public boolean isCorrect(ConnectionType connectionType) {
    return true;
  }

  public boolean isStillValid(Player player) {
    return isStillValid(this, player, 64);
  }

  public static boolean isStillValid(BlockEntity blockEntity, Player player, int maxDistance) {
    var pos = blockEntity.getBlockPos();
    var distance = player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ());
    return !blockEntity.isRemoved()
        && blockEntity.getLevel().getBlockEntity(pos).equals(blockEntity)
        && distance <= maxDistance;
  }

  @Override
  public final CompoundTag getUpdateTag(HolderLookup.Provider provider) {
    return this.saveWithoutMetadata(provider);
  }

  public void scheduleRenderUpdate() {
    this.refreshRenderState = true;
  }

  public void scheduleNeighborChange() {
    if (!this.level.isClientSide) {
      this.pipe.triggerConnectionCheck();
    }
    this.blockNeighborChange = true;
  }

  @Override
  public boolean acceptItem(LPTravelingItem item, BlockEntity from) {
    if (GenericPipeBlock.isValid(pipe)) {
      pipe.getTransport().injectItem(item, item.output);
      return true;
    }
    return false;
  }

  public static void commonTick(Level level, BlockPos blockPos, BlockState blockState,
      LogisticsGenericPipeBlockEntity<?> blockEntity) {

    /*final Info superDebug =
        StackTraceUtil.addSuperTraceInformation(() -> "Time: " + getWorld().getWorldTime());
    final Info debug = StackTraceUtil.addTraceInformation(() -> "(" + getX() + ", " + getY() + ", " + getZ() + ")", superDebug);
    */
    if (blockEntity.sendInitPacket && !level.isClientSide) {
      blockEntity.sendInitPacket = false;
      blockEntity.getRenderController().sendInit();
    }
    if (level instanceof ServerLevel) {
      if (blockEntity.deletePipe) {
        level.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
      }

      if (!blockEntity.initialized) {
        blockEntity.initialize();
      }
    }

    if (!GenericPipeBlock.isValid(blockEntity.pipe)) {
      //debug.end();
      return;
    }

    blockEntity.pipe.updateEntity();

    if (level.isClientSide) {
      //debug.end();
      return;
    }

    if (blockEntity.blockNeighborChange) {
      blockEntity.computeConnections();
      blockEntity.pipe.onNeighborBlockChange();
      blockEntity.blockNeighborChange = false;
      blockEntity.refreshRenderState = true;

      if (level instanceof ServerLevel) {
        /*MainProxy.sendPacketToAllWatchingChunk(blockEntity,
            PacketHandler.getPacket(PipeSolidSideCheck.class).setTilePos(blockEntity));*/
      }
    }

    //Sideblocks need to be checked before this
    //Network needs to be after this

    if (blockEntity.refreshRenderState) {
      blockEntity.refreshRenderState();

      if (blockEntity.renderState.isDirty()) {
        blockEntity.renderState.clean();
        blockEntity.sendUpdateToClient();
      }

      blockEntity.refreshRenderState = false;
    }

    if (blockEntity.sendClientUpdate) {
      blockEntity.sendClientUpdate = false;
      //MainProxy.sendPacketToAllWatchingChunk(blockEntity, getLPDescriptionPacket());
    }

    blockEntity.getRenderController().onUpdate();
    if (!blockEntity.addedToNetwork) {
      blockEntity.addedToNetwork = true;
    }
    //debug.end();
  }

  private void computeConnections() {
    TileBuffer[] cache = getTileCache();
    if (cache == null) {
      return;
    }

    for (Direction side : Direction.values()) {
      TileBuffer t = cache[side.ordinal()];
      t.refresh();

      pipeConnectionsBuffer[side.ordinal()] = canPipeConnect(t.getBlockEntity(), side);
    }
  }

  private void refreshRenderState() {
    // Pipe connections;
    for (Direction o : Direction.values()) {
      renderState.pipeConnectionMatrix.setConnected(o, pipeConnectionsBuffer[o.ordinal()]);
    }
    // Pipe Textures
    for (int i = 0; i < 7; i++) {
      Direction o = Direction.from3DDataValue(i);
      //renderState.textureMatrix.setIconIndex(o, pipe.getIconIndex(o));
    }
    //New Pipe Texture States
    renderState.textureMatrix.refreshStates(pipe);
  }

  public void sendUpdateToClient() {
    this.sendClientUpdate = true;
  }
}
