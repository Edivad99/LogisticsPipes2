package logisticspipes.fromkotlin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import logisticspipes.logisticspipes.LPNeighborBlockEntity;
import logisticspipes.logisticspipes.NeighborBlockEntity;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.utils.ConnectionType;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public class SingleAdjacent implements Adjacent {
  private final CoreRoutedPipe parent;
  @Getter
  private final Direction dir;
  private final ConnectionType adjacentType;

  public SingleAdjacent(CoreRoutedPipe parent, Direction dir, ConnectionType adjacentType) {
    this.parent = parent;
    this.dir = dir;
    this.adjacentType = adjacentType;
  }

  @Override
  public Map<BlockPos, ConnectionType> connectedPos() {
    Map<BlockPos, ConnectionType> map = new HashMap<>();
    map.put(parent.getPos().relative(dir), adjacentType);
    return map;
  }

  @Override
  public ConnectionType get(Direction direction) {
    return dir == direction ? adjacentType : null;
  }

  @Override
  public Optional<ConnectionType> optionalGet(Direction direction) {
    return dir == direction ? Optional.of(adjacentType) : Optional.empty();
  }

  @Override
  public Map<NeighborBlockEntity<BlockEntity>, ConnectionType> neighbors() {
    BlockEntity blockEntity = parent.getLevel().getBlockEntity(parent.getPos().relative(dir));
    if (blockEntity != null) {
      Map<NeighborBlockEntity<BlockEntity>, ConnectionType> map = new HashMap<>();
      map.put(new LPNeighborBlockEntity<>(blockEntity, dir), adjacentType);
      return map;
    }
    return new HashMap<>();
  }

  @Override
  public List<NeighborBlockEntity<BlockEntity>> inventories() {
    if (adjacentType.isItem()) {
      BlockEntity blockEntity = parent.getLevel().getBlockEntity(parent.getPos().relative(dir));
      if (blockEntity != null) {
        var neighbor = new LPNeighborBlockEntity<>(blockEntity, dir);
        if (neighbor.canHandleItems()) {
          return List.of(neighbor);
        }
      }
    }
    return List.of();
  }

  @Override
  public List<NeighborBlockEntity<BlockEntity>> fluidTanks() {
    if (adjacentType.isFluid()) {
      BlockEntity blockEntity = parent.getLevel().getBlockEntity(parent.getPos().relative(dir));
      if (blockEntity != null) {
        var neighbor = new LPNeighborBlockEntity<>(blockEntity, dir);
        if (neighbor.canHandleFluids()) {
          return List.of(neighbor);
        }
      }
    }
    return List.of();
  }

  @Override
  public String toString() {
    return "SingleAdjacent(" + dir.getName() + ": " + adjacentType + ")";
  }
}
