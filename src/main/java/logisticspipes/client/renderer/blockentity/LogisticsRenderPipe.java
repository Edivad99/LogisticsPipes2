package logisticspipes.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import logisticspipes.world.level.block.entity.LogisticsTileGenericPipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class LogisticsRenderPipe implements BlockEntityRenderer<LogisticsTileGenericPipe> {

  @Override
  public void render(LogisticsTileGenericPipe blockEntity, float partialTick, PoseStack poseStack,
      MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
    poseStack.pushPose();
    poseStack.translate(0.5, 0.5, 0.5);
    var scale = 0.75F;
    poseStack.scale(scale, scale, scale);
    poseStack.translate(0, -0.1F, 0);
    Minecraft.getInstance().getItemRenderer()
        .renderStatic(new ItemStack(Items.DIAMOND), ItemDisplayContext.GROUND,
            packedLight, packedOverlay,
            poseStack, bufferSource, blockEntity.getLevel(), 0);
    poseStack.popPose();
  }
}
