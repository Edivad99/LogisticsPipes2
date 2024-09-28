package logisticspipes.fromkotlin;

import java.util.ArrayList;
import java.util.List;
import logisticspipes.LogisticsPipes;
import logisticspipes.logisticspipes.LPNeighborBlockEntity;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class WorldCoordinatesWrapper {
  private final Level level;
  private final BlockPos pos;

  public WorldCoordinatesWrapper(Level level, BlockPos pos) {
    this.level = level;
    this.pos = pos;
  }

  public WorldCoordinatesWrapper(BlockEntity blockEntity) {
    this(blockEntity.getLevel(), blockEntity.getBlockPos());
  }

  public BlockEntity getBlockEntity() {
    return this.level.getBlockEntity(pos);
  }

  public List<LPNeighborBlockEntity<BlockEntity>> allNeighborTileEntities() {
    List<LPNeighborBlockEntity<BlockEntity>> neighbors = new ArrayList<>();
    for (Direction direction : Direction.values()) {
      LPNeighborBlockEntity<BlockEntity> neighbor = getNeighbor(direction);
      if (neighbor != null) {
        neighbors.add(neighbor);
      }
    }
    return neighbors;
  }

  public List<LPNeighborBlockEntity<BlockEntity>> connectedTileEntities() {
    BlockEntity pipe = getBlockEntity();
    if (SimpleServiceLocator.pipeInformationManager.isNotAPipe(pipe)) {
      LogisticsPipes.LOG.warn("The coordinates didn't hold a pipe at all", new Throwable("Stack trace"));
      return new ArrayList<>();
    }
    List<LPNeighborBlockEntity<BlockEntity>> connected = new ArrayList<>();
    for (LPNeighborBlockEntity<BlockEntity> adjacent : allNeighborTileEntities()) {
      if (MainProxy.checkPipesConnections(pipe, adjacent.getBlockEntity(), adjacent.getDirection())) {
        connected.add(adjacent);
      }
    }
    return connected;
  }

  public LPNeighborBlockEntity<BlockEntity> getNeighbor(Direction direction) {
    BlockEntity tileEntity = level.getBlockEntity(pos.relative(direction));
    if (tileEntity == null) {
      return null;
    }
    return new LPNeighborBlockEntity<>(tileEntity, direction);
  }

  @Override
  public int hashCode() {
    int result = level.hashCode();
    result = 31 * result + pos.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) return true;
    if (!(other instanceof WorldCoordinatesWrapper that)) return false;
    return level.equals(that.level) && pos.equals(that.pos);
  }

  @Override
  public String toString() {
    return "WorldCoordinatesWrapper(world=" + level + ", pos=" + pos + ")";
  }
}
