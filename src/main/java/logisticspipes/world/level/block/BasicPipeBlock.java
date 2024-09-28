package logisticspipes.world.level.block;

import com.mojang.serialization.MapCodec;
import logisticspipes.world.level.block.entity.BasicPipeBlockEntity;
import logisticspipes.world.level.block.entity.LogisticsGenericPipeBlockEntity;
import logisticspipes.world.level.block.entity.LogisticsPipesBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class BasicPipeBlock extends GenericPipeBlock {

  private static final MapCodec<BasicPipeBlock> CODEC = simpleCodec(BasicPipeBlock::new);

  public BasicPipeBlock(Properties properties) {
    super(properties);
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new BasicPipeBlockEntity(pos, state);
  }

  @Override
  protected MapCodec<? extends BaseEntityBlock> codec() {
    return CODEC;
  }

  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState,
      BlockEntityType<T> type) {
    return createTickerHelper(type, LogisticsPipesBlockEntityTypes.PIPE_BASIC.get(),
        LogisticsGenericPipeBlockEntity::commonTick);
  }
}
