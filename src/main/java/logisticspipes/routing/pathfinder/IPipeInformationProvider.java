package logisticspipes.routing.pathfinder;

import logisticspipes.utils.ConnectionType;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface IPipeInformationProvider {

  boolean isCorrect(ConnectionType connectionType);

  boolean canConnect(BlockEntity to, Direction direction, boolean ignoreSystemDisconnection);

  @Deprecated
  default boolean canConnect(BlockEntity to, Direction direction) {
    return canConnect(to, direction, false);
  }
}
