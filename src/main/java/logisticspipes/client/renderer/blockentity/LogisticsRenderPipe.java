package logisticspipes.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.transport.LPTravelingItem;
import logisticspipes.transport.PipeTransportLogistics;
import logisticspipes.utils.CoordinateUtils;
import logisticspipes.utils.DoubleCoordinates;
import logisticspipes.world.level.block.entity.LogisticsGenericPipeBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class LogisticsRenderPipe implements BlockEntityRenderer<LogisticsGenericPipeBlockEntity<?>> {

  final static private int LIQUID_STAGES = 40;
  final static private int MAX_ITEMS_TO_RENDER = 10;
  /*private final ItemEntity dummyEntityItem = new ItemEntity(null, null);
  private final ItemRenderer customRenderItem;

  private final int[] angleY = { 0, 0, 270, 90, 0, 180 };
  private final int[] angleZ = { 90, 270, 0, 0, 0, 0 };

  private HashMap<Integer, DisplayFluidList> displayFluidLists = new HashMap<Integer, DisplayFluidList>();
  //private ModelSign modelSign = new ModelSign();
  //private RenderBlocks renderBlocks = new RenderBlocks();
  //private IBCRenderTESR bcRenderer = SimpleServiceLocator.buildCraftProxy.getBCRenderTESR();

  private static class DisplayFluidList {
    public int[] sideHorizontal = new int[LIQUID_STAGES];
    public int[] sideVertical = new int[LIQUID_STAGES];
    public int[] centerHorizontal = new int[LIQUID_STAGES];
    public int[] centerVertical = new int[LIQUID_STAGES];
  }*/

  public LogisticsRenderPipe() {
  }


  @Override
  public void render(LogisticsGenericPipeBlockEntity<?> blockEntity, float partialTick,
      PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
    CoreUnroutedPipe pipe = blockEntity.pipe;
    if(!pipe.isOpaque()) {
      /*if(pipe.getTransport() instanceof PipeFluidTransportLogistics) {
        renderFluids(pipe.pipe, x, y, z);
      }*/
      if(pipe.getTransport() instanceof PipeTransportLogistics) {
        renderSolids(blockEntity, partialTick, poseStack, bufferSource, packedOverlay);
      }
    }
    if(pipe instanceof CoreRoutedPipe coreRoutedPipe) {
      //renderPipeSigns(coreRoutedPipe, x, y, z);
    }
  }

  private void renderSolids(LogisticsGenericPipeBlockEntity<?> blockEntity, float partialTickTime,
      PoseStack poseStack, MultiBufferSource bufferSource, int packedOverlay) {
    final var level = blockEntity.getLevel();
    final CoreUnroutedPipe pipe = blockEntity.pipe;
    final var x = blockEntity.getX();
    final var y = blockEntity.getY();
    final var z = blockEntity.getZ();

    poseStack.pushPose();

    int light = level.getLightEmission(blockEntity.getBlockPos());

    int count = 0;
    for(LPTravelingItem item: pipe.getTransport().items) {
      CoreUnroutedPipe lPipe = pipe;
      double lX = x;
      double lY = y;
      double lZ = z;
      float lItemYaw = item.getYaw();
      if(count >= MAX_ITEMS_TO_RENDER) {
        break;
      }

      if (item.getItemIdentifierStack() == null) {
        continue;
      }

      if (!item.getContainer().getBlockPos().equals(lPipe.container.getBlockPos())) {
        continue;
      }

      if (item.getPosition() > lPipe.getTransport().getPipeLength() || item.getPosition() < 0) {
        continue;
      }

      float fPos = item.getPosition() + item.getSpeed() * partialTickTime;
      if (fPos > lPipe.getTransport().getPipeLength() && item.output != null) {
        CoreUnroutedPipe nPipe = lPipe.getTransport().getNextPipe(item.output);
        if (nPipe != null) {
          fPos -= lPipe.getTransport().getPipeLength();
          lX -= lPipe.getX() - nPipe.getX();
          lY -= lPipe.getY() - nPipe.getY();
          lZ -= lPipe.getZ() - nPipe.getZ();
          lItemYaw += lPipe.getTransport().getYawDiff(item);
          lPipe = nPipe;
          item = item.renderCopy();
          item.input = item.output;
          item.output = null;
        } else {
          continue;
        }
      }

      DoubleCoordinates pos = lPipe.getItemRenderPos(fPos, item);
      if (pos == null) {
        continue;
      }
      /*double boxScale = lPipe.getBoxRenderScale(fPos, item);
      double itemYaw = (lPipe.getItemRenderYaw(fPos, item) - lPipe.getItemRenderYaw(0, item) + lItemYaw) % 360;
      double itemPitch = lPipe.getItemRenderPitch(fPos, item);
      double itemYawForPitch = lPipe.getItemRenderYaw(fPos, item);*/

      ItemStack stack = item.getItemIdentifierStack();
      doRenderItem(poseStack, bufferSource, level, stack,
          lX + pos.getXCoord(), lY + pos.getYCoord(), lZ + pos.getZCoord(),
          light, packedOverlay, 0.75F);
      count++;
    }

    count = 0;
    float dist = 0.135F;
    DoubleCoordinates pos = new DoubleCoordinates(0.5D, 0.5D, 0.5D);
    CoordinateUtils.add(pos, Direction.SOUTH, dist);
    CoordinateUtils.add(pos, Direction.EAST, dist);
    CoordinateUtils.add(pos, Direction.UP, dist);
    for(var item: pipe.getTransport().itemBuffer) {
      if(item == null || item.getA() == null) {
        continue;
      }
      ItemStack itemstack = item.getA();
      doRenderItem(poseStack, bufferSource, level, itemstack,
          x + pos.getXDouble(), y + pos.getYDouble(), z + pos.getZDouble(),
          light, packedOverlay, 0.25F);
      count++;
      if(count >= 27) {
        break;
      } else if(count % 9 == 0) {
        CoordinateUtils.add(pos, Direction.SOUTH, dist * 2.0);
        CoordinateUtils.add(pos, Direction.EAST, dist * 2.0);
        CoordinateUtils.add(pos, Direction.DOWN, dist);
      } else if(count % 3 == 0) {
        CoordinateUtils.add(pos, Direction.SOUTH, dist * 2.0);
        CoordinateUtils.add(pos, Direction.WEST, dist);
      } else {
        CoordinateUtils.add(pos, Direction.NORTH, dist);
      }
    }
    poseStack.popPose();
  }

  public void doRenderItem(PoseStack poseStack, MultiBufferSource bufferSource, Level level,
      ItemStack itemstack, double x, double y, double z,
      int light, int packedOverlay, float renderScale) {
    /*GL11.glPushMatrix();
    GL11.glTranslatef((float)x, (float)y, (float)z);
    GL11.glScalef(renderScale, renderScale, renderScale);
    dummyEntityItem.setEntityItemStack(itemstack);
    dummyEntityItem.age = age;
    dummyEntityItem.hoverStart = hoverStart;
    customRenderItem.doRender(dummyEntityItem, 0, 0, 0, 0, 0);
    dummyEntityItem.age = 0;
    dummyEntityItem.hoverStart = 0;
    GL11.glPopMatrix();*/

    poseStack.pushPose();
    //poseStack.translate(x, y, z);
    poseStack.scale(renderScale, renderScale, renderScale);
    Minecraft.getInstance().getItemRenderer().renderStatic(
        itemstack, ItemDisplayContext.GROUND, light, packedOverlay, poseStack, bufferSource,
        level, 0);
    poseStack.popPose();
  }
}
