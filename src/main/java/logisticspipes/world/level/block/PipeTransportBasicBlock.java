package logisticspipes.world.level.block;

import com.mojang.serialization.MapCodec;
import logisticspipes.world.level.block.entity.LogisticsTileGenericPipe;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class PipeTransportBasicBlock extends BaseEntityBlock {
  private static final MapCodec<PipeTransportBasicBlock> CODEC = simpleCodec(PipeTransportBasicBlock::new);

  public PipeTransportBasicBlock(Properties properties) {
    super(properties);
  }

  @Override
  protected MapCodec<? extends BaseEntityBlock> codec() {
    return CODEC;
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new LogisticsTileGenericPipe(pos, state);
  }

  @Override
  protected RenderShape getRenderShape(BlockState state) {
    return RenderShape.MODEL;
  }
}
