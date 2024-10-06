package logisticspipes.logisticspipes;

import org.jetbrains.annotations.Nullable;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.proxy.SimpleServiceLocator;
import lombok.Getter;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

@Getter
public abstract class NeighborBlockEntity<T extends BlockEntity> {

  private final T blockEntity;
  private final Direction direction;

  public NeighborBlockEntity(T blockEntity, Direction direction) {
    this.blockEntity = blockEntity;
    this.direction = direction;
  }

  public abstract boolean isLogisticsPipe();

  public abstract boolean canHandleItems();

  public abstract boolean canHandleFluids();

  public Direction getOurDirection() {
    return this.direction.getOpposite();
  }

  public LPNeighborBlockEntitySneakyInsertion<T> sneakyInsertion() {
    return new LPNeighborBlockEntitySneakyInsertion<>(this.blockEntity, this.direction);
  }

  @Nullable
  public IInventoryUtil getInventoryUtil() {
    return SimpleServiceLocator.inventoryUtilFactory.getInventoryUtil(blockEntity, getOurDirection());
  }
}
