package logisticspipes.client.renderer.blockentity;

import java.util.function.Supplier;
import logisticspipes.world.level.block.entity.LogisticsPipesBlockEntityTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class LogisticsPipesEntityRenderers {

  public static void register(EntityRenderersEvent.RegisterRenderers event) {
    event.registerBlockEntityRenderer(LogisticsPipesBlockEntityTypes.PIPE_TRANSPORT_BASIC.get(),
        supply(LogisticsRenderPipe::new));
  }

  private static <T extends BlockEntity> BlockEntityRendererProvider<T> supply(
      Supplier<BlockEntityRenderer<T>> supplier) {
    return __ -> supplier.get();
  }
}
