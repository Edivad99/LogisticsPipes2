package logisticspipes.fromkotlin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import logisticspipes.logisticspipes.LPNeighborBlockEntity;
import logisticspipes.logisticspipes.NeighborBlockEntity;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.utils.ConnectionType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;


class DynamicAdjacent implements Adjacent {
  private final CoreRoutedPipe parent;
  private final ConnectionType[] cache;

  public DynamicAdjacent(CoreRoutedPipe parent, ConnectionType[] cache) {
    this.parent = parent;
    this.cache = cache;
  }

  @Override
  public Map<BlockPos, ConnectionType> connectedPos() {
    Map<BlockPos, ConnectionType> result = new LinkedHashMap<>();
    for (int index = 0; index < cache.length; index++) {
      ConnectionType type = cache[index];
      if (type != null) {
        result.put(parent.getPos().relative(Direction.values()[index]), type);
      }
    }
    return result;
  }

  @Override
  public ConnectionType get(Direction direction) {
    return cache[direction.get3DDataValue()];
  }

  @Override
  public Optional<ConnectionType> optionalGet(Direction direction) {
    return Optional.ofNullable(cache[direction.get3DDataValue()]);
  }

  @Override
  public Map<NeighborBlockEntity<BlockEntity>, ConnectionType> neighbors() {
    Map<NeighborBlockEntity<BlockEntity>, ConnectionType> result = new LinkedHashMap<>();
    for (int index = 0; index < cache.length; index++) {
      ConnectionType connectionType = cache[index];
      if (connectionType != null) {
        Direction dir = Direction.values()[index];
        BlockEntity tileEntity = parent.getLevel().getBlockEntity(parent.getPos().relative(dir));
        if (tileEntity != null) {
          result.put(new LPNeighborBlockEntity<>(tileEntity, dir), connectionType);
        }
      }
    }
    return result;
  }

  @Override
  public List<NeighborBlockEntity<BlockEntity>> inventories() {
    List<NeighborBlockEntity<BlockEntity>> result = new ArrayList<>();
    for (int index = 0; index < cache.length; index++) {
      if (cache[index] != null && cache[index].isItem()) {
        Direction dir = Direction.values()[index];
        BlockEntity tileEntity = parent.getLevel().getBlockEntity(parent.getPos().relative(dir));
        if (tileEntity != null) {
          var neighbor = new LPNeighborBlockEntity<>(tileEntity, dir);
          if (neighbor.canHandleItems()) {
            result.add(neighbor);
          }
        }
      }
    }
    return result;
  }

  @Override
  public List<NeighborBlockEntity<BlockEntity>> fluidTanks() {
    List<NeighborBlockEntity<BlockEntity>> result = new ArrayList<>();
    for (int index = 0; index < cache.length; index++) {
      if (cache[index] != null && cache[index].isFluid()) {
        Direction dir = Direction.values()[index];
        BlockEntity tileEntity = parent.getLevel().getBlockEntity(parent.getPos().relative(dir));
        if (tileEntity != null) {
          var neighbor = new LPNeighborBlockEntity<>(tileEntity, dir);
          if (neighbor.canHandleFluids()) {
            result.add(neighbor);
          }
        }
      }
    }
    return result;
  }

  @Override
  public String toString() {
    return "DynamicAdjacent(" + Arrays.stream(Direction.values())
        .map(enumFacing -> "{" + enumFacing.name() + ": " + cache[enumFacing.get3DDataValue()] + "}")
        .collect(Collectors.joining(", ")) + ")";
  }
}

