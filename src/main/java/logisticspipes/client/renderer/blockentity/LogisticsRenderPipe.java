package logisticspipes.client.renderer.blockentity;

import org.jetbrains.annotations.Nullable;
import com.mojang.blaze3d.vertex.PoseStack;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.transport.LPTravelingItem;
import logisticspipes.transport.PipeTransportLogistics;
import logisticspipes.utils.CoordinateUtils;
import logisticspipes.utils.DoubleCoordinates;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.world.level.block.entity.LogisticsGenericPipeBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class LogisticsRenderPipe implements BlockEntityRenderer<LogisticsGenericPipeBlockEntity<?>> {

  final static private int LIQUID_STAGES = 40;
  final static private int MAX_ITEMS_TO_RENDER = 10;
  private final LazyItemRenderer itemRenderer = new LazyItemRenderer();
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
    itemRenderer.init(blockEntity.getLevel(), blockEntity.getBlockPos());
    CoreUnroutedPipe pipe = blockEntity.pipe;
    if(!pipe.isOpaque()) {
      /*if(pipe.getTransport() instanceof PipeFluidTransportLogistics) {
        renderFluids(pipe.pipe, x, y, z);
      }*/
      if(pipe.getTransport() instanceof PipeTransportLogistics) {
        renderSolids(blockEntity, partialTick, poseStack, bufferSource, packedOverlay, packedLight);
      }
    }
    if(pipe instanceof CoreRoutedPipe coreRoutedPipe) {
      //renderPipeSigns(coreRoutedPipe, x, y, z);
    }
  }

  private void renderSolids(LogisticsGenericPipeBlockEntity<?> blockEntity, float partialTickTime,
      PoseStack poseStack, MultiBufferSource bufferSource, int packedOverlay, int packedLight) {
    final var level = blockEntity.getLevel();
    final CoreUnroutedPipe pipe = blockEntity.pipe;
    final var x = blockEntity.getX();
    final var y = blockEntity.getY();
    final var z = blockEntity.getZ();

    poseStack.pushPose();

    int count = 0;
    for(LPTravelingItem item: pipe.getTransport().items) {
      CoreUnroutedPipe lPipe = pipe;
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

      //TODO: Investigate this
      float fPos = item.getPosition();// + item.getSpeed() * partialTickTime;
      if (fPos > lPipe.getTransport().getPipeLength() && item.output != null) {
        CoreUnroutedPipe nPipe = lPipe.getTransport().getNextPipe(item.output);
        if (nPipe != null) {
          fPos -= lPipe.getTransport().getPipeLength();
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

      ItemIdentifierStack stack = item.getItemIdentifierStack();
      doRenderItem(poseStack, bufferSource, stack.makeNormalStack(),
          pos.getXCoord(), pos.getYCoord(), pos.getZCoord(), packedLight, 0.7F);
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
      doRenderItem(poseStack, bufferSource, item.getA().makeNormalStack(),
          x + pos.getXDouble(), y + pos.getYDouble(), z + pos.getZDouble(),
          packedLight, 0.25F);
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

  public void doRenderItem(PoseStack poseStack, MultiBufferSource bufferSource,
      ItemStack itemstack, double x, double y, double z, int packedLight, float renderScale) {
    poseStack.pushPose();
    poseStack.translate(x, y, z);
    poseStack.scale(renderScale, renderScale, renderScale);
    poseStack.translate(0, -0.25F, 0);
    itemRenderer.renderAsStack(poseStack, bufferSource, itemstack, packedLight);
    poseStack.popPose();
  }

  private static class LazyItemRenderer {

    @Nullable
    private ItemEntity entityItem;
    @Nullable
    private EntityRenderer<? super ItemEntity> renderer;

    public void init(Level level, BlockPos pos) {
      if (entityItem == null) {
        entityItem = new ItemEntity(EntityType.ITEM, level);
      } else {
        entityItem.setLevel(level);
      }
      entityItem.setPos(0, 0, 0);
      entityItem.setPos(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
      //Reset entity age to fix issues with mods like ItemPhysic
      entityItem.age = 0;
    }

    private void renderAsStack(PoseStack matrix, MultiBufferSource buffer, ItemStack stack, int light) {
      if (entityItem != null) {
        if (renderer == null) {
          renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entityItem);
        }
        entityItem.setItem(stack);
        renderer.render(entityItem, 0, 0, matrix, buffer, light);
      }
    }
  }
}
