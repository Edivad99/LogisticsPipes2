package logisticspipes.transport;

import logisticspipes.api.ILogisticsPowerProvider;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.ISubSystemPowerProvider;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.OrientationsUtil;
import logisticspipes.world.level.block.entity.LogisticsGenericPipeBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

public class PipeTransportLogistics {

  private LevelChunk chunk;
  private LogisticsGenericPipeBlockEntity<? extends CoreUnroutedPipe> container;
  public final boolean isRouted;

  public PipeTransportLogistics(boolean isRouted) {
    this.isRouted = isRouted;
  }

  public void initialize() {
    if (!this.getLevel().isClientSide) {
      this.chunk = this.getLevel().getChunkAt(this.container.getBlockPos());
    }
  }

  public void setTile(LogisticsGenericPipeBlockEntity<? extends CoreUnroutedPipe> blockEntity) {
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

  public Level getLevel() {
    return this.container.getLevel();
  }

  public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
  }

  public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
  }

  public void updateEntity() {
  }
}
