package logisticspipes.fromkotlin;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;
import logisticspipes.logisticspipes.NeighborBlockEntity;
import logisticspipes.utils.ConnectionType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public class NoAdjacent implements Adjacent {

  public static final NoAdjacent INSTANCE = new NoAdjacent();

  @Override
  public Map<BlockPos, ConnectionType> connectedPos() {
    return Map.of();
  }

  @Override
  @Nullable
  public ConnectionType get(Direction direction) {
    return null;
  }

  @Override
  public Optional<ConnectionType> optionalGet(Direction direction) {
    return Optional.empty();
  }

  @Override
  public Map<NeighborBlockEntity<BlockEntity>, ConnectionType> neighbors() {
    return Map.of();
  }

  @Override
  public List<NeighborBlockEntity<BlockEntity>> inventories() {
    return List.of();
  }

  @Override
  public List<NeighborBlockEntity<BlockEntity>> fluidTanks() {
    return List.of();
  }

  @Override
  public String toString() {
    return "NoAdjacent";
  }
}
