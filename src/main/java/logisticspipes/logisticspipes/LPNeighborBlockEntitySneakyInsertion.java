package logisticspipes.logisticspipes;

import org.jetbrains.annotations.Nullable;
import logisticspipes.interfaces.ISlotUpgradeManager;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public class LPNeighborBlockEntitySneakyInsertion<T extends BlockEntity>
    extends LPNeighborBlockEntity<T>{

  private Direction sneakyDirection;

  public LPNeighborBlockEntitySneakyInsertion(T blockEntity, Direction direction) {
    super(blockEntity, direction);
    this.sneakyDirection = super.getOurDirection();
  }

  @Override
  public Direction getOurDirection() {
    return this.sneakyDirection;
  }

  public LPNeighborBlockEntitySneakyInsertion<T> from(Direction direction) {
    this.sneakyDirection = direction;
    return this;
  }

  public LPNeighborBlockEntitySneakyInsertion<T> from(@Nullable ISlotUpgradeManager upgradeManager) {
    if (upgradeManager != null && upgradeManager.hasSneakyUpgrade()) {
      this.sneakyDirection = upgradeManager.getSneakyOrientation();
    }
    return this;
  }
}
