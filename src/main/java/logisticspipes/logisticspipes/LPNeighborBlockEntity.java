package logisticspipes.logisticspipes;

import logisticspipes.world.level.block.entity.LogisticsGenericPipeBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public class LPNeighborBlockEntity<T extends BlockEntity> extends NeighborBlockEntity<T> {

  public LPNeighborBlockEntity(T blockEntity, Direction direction) {
    super(blockEntity, direction);
  }

  @Override
  public boolean isLogisticsPipe() {
    return this.getBlockEntity() instanceof LogisticsGenericPipeBlockEntity<?>;
  }

  @Override
  public boolean canHandleItems() {
    return this.getInventoryUtil() != null;
  }

  @Override
  public boolean canHandleFluids() {
    return false;
  }

  /*@Nullable
  fun <T : TileEntity> NeighborTileEntity<T>.getTankUtil(): ITankUtil? =
      PipeFluidUtil.getTankUtilForTE(tileEntity, getOurDirection())*/
}
