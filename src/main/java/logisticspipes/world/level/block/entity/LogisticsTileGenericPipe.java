package logisticspipes.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class LogisticsTileGenericPipe extends BlockEntity {

  public LogisticsTileGenericPipe(BlockPos pos, BlockState blockState) {
    super(LogisticsPipesBlockEntityTypes.PIPE_BASIC.get(), pos, blockState);
  }
}
