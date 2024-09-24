package logisticspipes.pipes.basic;

import org.jetbrains.annotations.Nullable;
import logisticspipes.api.ILPPipe;
import logisticspipes.interfaces.ILevelProvider;
import logisticspipes.interfaces.IPipeServiceProvider;
import logisticspipes.transport.PipeTransportLogistics;
import logisticspipes.world.level.block.GenericPipeBlock;
import logisticspipes.world.level.block.entity.LogisticsGenericPipeBlockEntity;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class CoreUnroutedPipe implements ILPPipe, ILevelProvider, IPipeServiceProvider {

  @Nullable
  public LogisticsGenericPipeBlockEntity<? extends CoreUnroutedPipe> container;
  private final PipeTransportLogistics transport;
  private boolean initialized = false;

  public CoreUnroutedPipe(PipeTransportLogistics transport) {
    this.transport = transport;
  }

  public void setBlockEntity(LogisticsGenericPipeBlockEntity<? extends CoreUnroutedPipe> blockEntity) {
    this.container = blockEntity;
    this.transport.setTile(blockEntity);
  }

  public boolean preventRemove() {
    return false;
  }

  @Override
  public boolean isRoutedPipe() {
    return false;
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

  @Override
  public BlockPos getPos() {
    if (this.container == null) {
      return null;
    }
    return this.container.getBlockPos();
  }

  @Override
  public void setChanged() {
    if (this.container != null) {
      this.container.setChanged();
    }
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
}
