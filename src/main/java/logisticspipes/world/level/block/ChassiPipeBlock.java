package logisticspipes.world.level.block;

import com.mojang.serialization.MapCodec;
import logisticspipes.world.level.block.entity.ChassiPipeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
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
}
