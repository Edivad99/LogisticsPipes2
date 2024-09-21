package logisticspipes.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import logisticspipes.world.level.block.entity.LogisticsGenericPipeBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;

public class LogisticsRenderPipe implements BlockEntityRenderer<LogisticsGenericPipeBlockEntity> {

  @Override
  public void render(LogisticsGenericPipeBlockEntity blockEntity, float partialTick, PoseStack poseStack,
      MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
    /*poseStack.pushPose();
    poseStack.translate(0.5, 0.5, 0.5);
    var scale = 0.75F;
    poseStack.scale(scale, scale, scale);
    poseStack.translate(0, -0.1F, 0);
    Minecraft.getInstance().getItemRenderer()
        .renderStatic(new ItemStack(Items.DIAMOND), ItemDisplayContext.GROUND,
            packedLight, packedOverlay,
            poseStack, bufferSource, blockEntity.getLevel(), 0);
    poseStack.popPose();*/
  }
}
