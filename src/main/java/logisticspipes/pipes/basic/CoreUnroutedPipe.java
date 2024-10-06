package logisticspipes.pipes.basic;

import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;
import logisticspipes.Configs;
import logisticspipes.api.ILPPipe;
import logisticspipes.interfaces.IClientState;
import logisticspipes.interfaces.ILevelProvider;
import logisticspipes.interfaces.IPipeUpgradeManager;
import logisticspipes.pipes.basic.debug.DebugLogController;
import logisticspipes.pipes.basic.debug.StatusEntry;
import logisticspipes.transport.LPTravelingItem;
import logisticspipes.transport.PipeTransportLogistics;
import logisticspipes.utils.CoordinateUtils;
import logisticspipes.utils.DoubleCoordinates;
import logisticspipes.world.level.block.GenericPipeBlock;
import logisticspipes.world.level.block.entity.LogisticsGenericPipeBlockEntity;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class CoreUnroutedPipe implements ILPPipe, IClientState, ILevelProvider {

  @Nullable
  public LogisticsGenericPipeBlockEntity<? extends CoreUnroutedPipe> container;
  @Getter
  protected final PipeTransportLogistics transport;
  public DebugLogController debug = new DebugLogController(this);
  private boolean initialized = false;

  public CoreUnroutedPipe(PipeTransportLogistics transport) {
    this.transport = transport;
  }

  public void setBlockEntity(LogisticsGenericPipeBlockEntity<? extends CoreUnroutedPipe> blockEntity) {
    this.container = blockEntity;
    this.transport.setBlockEntity(blockEntity);
  }

  public void onBlockPlaced() {
    this.transport.onBlockPlaced();
  }

  public void onBlockPlacedBy(@Nullable LivingEntity placer) {}

  public void onNeighborBlockChange() {
    this.transport.onNeighborBlockChange();
  }

  public boolean canPipeConnect(BlockEntity blockEntity, Direction side) {
    if (blockEntity instanceof LogisticsGenericPipeBlockEntity) {
      CoreUnroutedPipe otherPipe = ((LogisticsGenericPipeBlockEntity<?>) blockEntity).pipe;
      if (!GenericPipeBlock.isFullyDefined(otherPipe)) {
        return false;
      }
    }
    return transport.canPipeConnect(blockEntity, side);
  }

  public void updateEntity() {
    this.transport.updateEntity();
  }

  public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    this.transport.saveAdditional(tag, registries);
  }

  public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    this.transport.loadAdditional(tag, registries);
  }

  public boolean needsInit() {
    return !this.initialized;
  }

  public void initialize() {
    this.transport.initialize();
    this.initialized = true;
  }

  public void onBlockRemoval() {
  }

  /**
   * Called when LogisticsGenericPipeBlockEntity.setRemoved() is called
   */
  public void setRemoved() {
  }

  /**
   * Called when LogisticsGenericPipeBlockEntity.clearRemoved() is called
   */
  public void validate() {
  }

  /**
   * Called when LogisticsGenericPipeBlockEntity.onChunkUnloaded() is called
   */
  public void onChunkUnload() {
  }

  @Nullable
  public Level getLevel() {
    if (this.container == null) {
      return null;
    }
    return container.getLevel();
  }

  public boolean isSideBlocked(Direction direction, boolean ignoreSystemDisconnection) {
    return false;
  }

  public final int getX() {
    return this.getPos().getX();
  }

  public final int getY() {
    return this.getPos().getY();
  }

  public final int getZ() {
    return this.getPos().getZ();
  }

  public BlockPos getPos() {
    Objects.requireNonNull(this.container, "setBlockEntity must be called before initialize");
    return this.container.getBlockPos();
  }

  public boolean canBeDestroyed() {
    return true;
  }

  public boolean destroyByPlayer() {
    return false;
  }

  public void setPreventRemove(boolean preventRemove) {
  }

  public boolean preventRemove() {
    return false;
  }

  @Override
  public boolean isRoutedPipe() {
    return false;
  }

  public boolean isFluidPipe() {
    return false;
  }

  public void triggerDebug() {
    if (this.debug.debugThisPipe) {
      System.out.print("");
    }
  }

  public void addStatusInformation(List<StatusEntry> status) {
  }

  public boolean isOpaque() {
    return Configs.OPAQUE;
  }

  public DoubleCoordinates getLPPosition() {
    return new DoubleCoordinates(this);
  }

  public IPipeUpgradeManager getUpgradeManager() {
    return new UnroutedPipeUpgradeManager();
  }

  public boolean isMultiBlock() {
    return false;
  }


  public boolean actAsNormalPipe() {
    return true;
  }

  @Nullable
  public DoubleCoordinates getItemRenderPos(float fPos, LPTravelingItem travelItem) {
    DoubleCoordinates pos = new DoubleCoordinates(0.5, 0.5, 0.5);
    if (fPos < 0.5) {
      if (travelItem.input == null) {
        return null;
      }
      if (!container.isPipeConnectedCached(travelItem.input.getOpposite())) {
        return null;
      }
      CoordinateUtils.add(pos, travelItem.input.getOpposite(), 0.5 - fPos);
    } else {
      if (travelItem.output == null) {
        return null;
      }
      if (!container.isPipeConnectedCached(travelItem.output)) {
        return null;
      }
      CoordinateUtils.add(pos, travelItem.output, fPos - 0.5);
    }
    return pos;
  }

  public void setChanged() {
    if (this.container != null) {
      this.container.setChanged();
    }
  }

  /**
   * Triggers connection checks for routing.
   */
  public void triggerConnectionCheck() {
  }

  protected void onAllowedRemoval() {
  }

  /**
   * Called after reading data from NBT.
   */
  public void finishInit() {
  }
}
