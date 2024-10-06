package logisticspipes.world.level.block;

import com.mojang.serialization.MapCodec;
import logisticspipes.world.level.block.entity.ChassiPipeBlockEntity;
import logisticspipes.world.level.block.entity.LogisticsGenericPipeBlockEntity;
import logisticspipes.world.level.block.entity.LogisticsPipesBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ChassiPipeBlock extends GenericPipeBlock {

  private static final MapCodec<ChassiPipeBlock> CODEC = simpleCodec(ChassiPipeBlock::new);

  public ChassiPipeBlock(Properties properties) {
    super(properties);
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new ChassiPipeBlockEntity(pos, state);
  }

  @Override
  protected MapCodec<? extends BaseEntityBlock> codec() {
    return CODEC;
  }

  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState,
      BlockEntityType<T> type) {
    return createTickerHelper(type, LogisticsPipesBlockEntityTypes.CHASSI_MK2.get(),
        LogisticsGenericPipeBlockEntity::commonTick);
  }
}
