package logisticspipes.utils;

import org.jetbrains.annotations.Nullable;
import logisticspipes.world.level.block.entity.LogisticsGenericPipeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class TileBuffer {

  @Nullable
  private Block block = null;
  @Nullable
  private BlockEntity blockEntity;

  private final SafeTimeTracker tracker = new SafeTimeTracker(20, 5);
  private final Level level;
  private final int x, y, z;
  private final boolean loadUnloaded;

  public TileBuffer(Level level, int x, int y, int z, boolean loadUnloaded) {
    this.level = level;
    this.x = x;
    this.y = y;
    this.z = z;
    this.loadUnloaded = loadUnloaded;

    refresh();
  }

  public void refresh() {
    BlockPos pos = new BlockPos(x, y, z);
    BlockState blockState = level.getBlockState(pos);
    if (blockEntity instanceof LogisticsGenericPipeBlockEntity<?> pipeBlockEntity && pipeBlockEntity.pipe.preventRemove()) {
      if (blockState.isEmpty()) {
        return;
      }
    }
    blockEntity = null;
    block = null;

    if (!loadUnloaded) {
      return;
    }

    block = blockState.getBlock();

    if (blockState.hasBlockEntity()) {
      blockEntity = level.getBlockEntity(pos);
    }
  }

  public void set(Block block, BlockEntity tile) {
    this.block = block;
    this.blockEntity = tile;
    tracker.markTime(level);
  }

  @Nullable
  public Block getBlock() {
    if (blockEntity != null && !blockEntity.isRemoved()) {
      return block;
    }

    if (tracker.markTimeIfDelay(level)) {
      refresh();

      if (blockEntity != null && !blockEntity.isRemoved()) {
        return block;
      }
    }

    return null;
  }

  @Nullable
  public BlockEntity getBlockEntity() {
    if (blockEntity != null && !blockEntity.isRemoved()) {
      return blockEntity;
    }

    if (tracker.markTimeIfDelay(level)) {
      refresh();

      if (blockEntity != null && !blockEntity.isRemoved()) {
        return blockEntity;
      }
    }

    return null;
  }

  public static TileBuffer[] makeBuffer(Level world, BlockPos pos, boolean loadUnloaded) {
    TileBuffer[] buffer = new TileBuffer[6];

    for (int i = 0; i < 6; i++) {
      Direction d = Direction.from3DDataValue(i);
      var xOffset = d.getAxis() == Direction.Axis.X ? d.getStepX() : 0;
      var yOffset = d.getAxis() == Direction.Axis.Y ? d.getStepY() : 0;
      var zOffset = d.getAxis() == Direction.Axis.Z ? d.getStepZ() : 0;
      buffer[i] = new TileBuffer(world, pos.getX() + xOffset, pos.getY() + yOffset,
          pos.getZ() + zOffset, loadUnloaded);
    }

    return buffer;
  }
}
