package logisticspipes.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import logisticspipes.world.level.block.entity.LogisticsTileGenericPipe;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;

public class LogisticsRenderPipe implements BlockEntityRenderer<LogisticsTileGenericPipe> {

  @Override
  public void render(LogisticsTileGenericPipe blockEntity, float partialTick, PoseStack poseStack,
      MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
  }
}
