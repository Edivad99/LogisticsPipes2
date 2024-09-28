package logisticspipes.network.to_client;

import logisticspipes.world.level.block.entity.LogisticsGenericPipeBlockEntity;
import lombok.AccessLevel;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class CoordinatesPacket implements CustomPacketPayload {

  @Getter(value = AccessLevel.PROTECTED)
  private final BlockPos blockPos;

  public CoordinatesPacket(BlockPos blockPos) {
    this.blockPos = blockPos;
  }

  public CoordinatesPacket(RegistryFriendlyByteBuf buffer) {
    this.blockPos = buffer.readBlockPos();
  }

  public void encode(RegistryFriendlyByteBuf buffer) {
    buffer.writeBlockPos(this.blockPos);
  }

  /**
   * Retrieves pipe at packet coordinates if any.
   */
  public LogisticsGenericPipeBlockEntity<?> getPipe(Level level) {
    return getBlockEntityAs(level, LogisticsGenericPipeBlockEntity.class);
  }

  /**
   * Retrieves tileEntity at packet coordinates if any.
   */
  public <T> T getBlockEntityAs(Level level, Class<T> clazz) {
    return getBlockEntityAs(level, this.blockPos, clazz);
  }

  public static <T> T getBlockEntityAs(Level level, BlockPos blockPos, Class<T> clazz) {
    final BlockEntity blockEntity = getBlockEntity(level, blockPos);
    if (blockEntity != null) {
      if (clazz.isAssignableFrom(blockEntity.getClass())) {
        return clazz.cast(blockEntity);
      }
      throw new IllegalStateException("Couldn't find " + clazz.getName() + ", found " + blockEntity.getClass() + " at: " + blockPos);
    } else {
      throw new IllegalStateException("Couldn't find " + clazz.getName() + " at: " + blockPos);
    }
  }

  private static BlockEntity getBlockEntity(Level level, BlockPos blockPos) {
    if (level == null) {
      throw new IllegalStateException("Level was null");
    }
    if (level.getBlockState(blockPos).isAir()) {
      throw new IllegalStateException("Only found air at: " + blockPos);
    }
    return level.getBlockEntity(blockPos);
  }
}
