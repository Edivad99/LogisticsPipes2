package logisticspipes.interfaces.routing;

import java.util.Collection;
import logisticspipes.logisticspipes.IRoutedItem;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface ISpecialTileConnection {

  boolean init();

  boolean isType(BlockEntity blockEntity);

  Collection<BlockEntity> getConnections(BlockEntity blockEntity);

  boolean needsInformationTransition();

  void transmit(BlockEntity blockEntity, IRoutedItem arrivingItem);
}
