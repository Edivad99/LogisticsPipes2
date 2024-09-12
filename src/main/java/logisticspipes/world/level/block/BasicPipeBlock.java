package logisticspipes.world.level.block;

import com.mojang.serialization.MapCodec;
import logisticspipes.tags.LogisticsPipesTags;
import logisticspipes.world.level.block.entity.LogisticsPipesBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class BasicPipeBlock extends GenericPipeBlock {

  private static final MapCodec<BasicPipeBlock> CODEC = simpleCodec(BasicPipeBlock::new);

  public BasicPipeBlock(Properties properties) {
    super(properties);
  }

  @Override
  protected MapCodec<? extends BaseEntityBlock> codec() {
    return CODEC;
  }

  @Override
  protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
      BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
    if (player instanceof ServerPlayer serverPlayer) {
      if (stack.is(LogisticsPipesTags.Items.WRENCH)) {
        level.getBlockEntity(pos, LogisticsPipesBlockEntityTypes.PIPE_BASIC.get())
            .ifPresent(blockEntity -> serverPlayer.openMenu(blockEntity, pos));
      }
    }
    return ItemInteractionResult.sidedSuccess(level.isClientSide());
  }
}
