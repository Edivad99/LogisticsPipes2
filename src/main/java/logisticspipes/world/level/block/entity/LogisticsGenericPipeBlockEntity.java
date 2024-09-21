package logisticspipes.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class LogisticsGenericPipeBlockEntity extends BlockEntity {

  public LogisticsGenericPipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
    super(type, pos, blockState);
  }

  public boolean isStillValid(Player player) {
    return isStillValid(this, player, 64);
  }

  public static boolean isStillValid(BlockEntity blockEntity, Player player, int maxDistance) {
    var pos = blockEntity.getBlockPos();
    var distance = player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ());
    return !blockEntity.isRemoved()
        && blockEntity.getLevel().getBlockEntity(pos).equals(blockEntity)
        && distance <= maxDistance;
  }
}
