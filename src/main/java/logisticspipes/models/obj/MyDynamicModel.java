package logisticspipes.models.obj;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.Nullable;
import com.mojang.blaze3d.vertex.PoseStack;
import logisticspipes.LogisticsPipes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;

// BakedModelWrapper can be used as well to return default values for most methods, allowing you to only override what actually needs to be overridden.
public class MyDynamicModel implements IDynamicBakedModel {

  private static final Material PARTICLE_TEXTURE =
      new Material(InventoryMenu.BLOCK_ATLAS, LogisticsPipes.rl("block/blank_pipe"));

  public static final Material BASIC_PIPE_TEXTURE =
      new Material(InventoryMenu.BLOCK_ATLAS, LogisticsPipes.rl("block/pipes/pipemodel"));
  private static final Material INACTIVE_TEXTURE =
      new Material(InventoryMenu.BLOCK_ATLAS, LogisticsPipes.rl("block/pipes/pipemodel_inactive"));
  private static final Material INNER_BOX_TEXTURE =
      new Material(InventoryMenu.BLOCK_ATLAS, LogisticsPipes.rl("block/pipes/innerbox"));
  private static final Material GLASS_CENTER_TEXTURE =
      new Material(InventoryMenu.BLOCK_ATLAS, LogisticsPipes.rl("block/pipes/glass_texture_center"));
  private static final Material STATUS_TEXTURE =
      new Material(InventoryMenu.BLOCK_ATLAS, LogisticsPipes.rl("block/pipes/pipemodel_status"));

  private final BakedModel base;
  private final Map<String, LogisticsNewPipeModel.MyModelGroup> parts;
  private final IGeometryBakingContext context;
  // Attributes for use in the methods below. Optional, the methods may also use constant values if applicable.
  private final boolean usesBlockLight;

  // The constructor does not require any parameters other than the ones for instantiating the final fields.
  // It may specify any additional parameters to store in fields you deem necessary for your model to work.
  public MyDynamicModel(BakedModel base, Map<String, LogisticsNewPipeModel.MyModelGroup> parts,
      IGeometryBakingContext context) {
    this.base = base;
    this.parts = parts;
    this.context = context;
    this.usesBlockLight = context.useBlockLight();
  }

  // Use our attributes. Refer to the article on baked models for more information on the method's effects.
  @Override
  public boolean useAmbientOcclusion() {
    return false;
  }

  @Override
  public boolean isGui3d() {
    return true;
  }

  @Override
  public boolean usesBlockLight() {
    return this.usesBlockLight;
  }

  @Override
  public TextureAtlasSprite getParticleIcon() {
    return PARTICLE_TEXTURE.sprite();
  }

  @Override
  public ItemOverrides getOverrides() {
    // Return ItemOverrides.EMPTY when in a block model context.
    return ItemOverrides.EMPTY;
  }

  // Override this to true if you want to use a custom block entity renderer instead of the default renderer.
  @Override
  public boolean isCustomRenderer() {
    return false;
  }

  @Override
  public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
    return ChunkRenderTypeSet.of(RenderType.CUTOUT);
  }

  // This is where the magic happens. Return a list of the quads to render here. Parameters are:
  // - The blockstate being rendered. May be null if rendering an item.
  // - The side being culled against. May be null, which means quads that cannot be occluded should be returned.
  // - A client-bound random source you can use for randomizing stuff.
  // - The extra data to use. Originates from a block entity (if present), or from BakedModel#getModelData().
  // - The render type for which quads are being requested.
  // NOTE: This may be called many times in quick succession, up to several times per block.
  // This should be as fast as possible and use caching wherever applicable.
  @Override
  public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData extraData, @Nullable RenderType renderType) {
    List<BakedQuad> quads = new ArrayList<>();
    if (side == null && (renderType == null || renderType.equals(RenderType.cutout()))) {

      var list = parts.values().stream()
          .filter(part -> context.isComponentVisible(part.name(), true))
          .toList();
      for (var dir : Direction.values()) {
        String grp = "Side_" + switch (dir) {
          case NORTH -> "N";
          case SOUTH -> "S";
          case EAST -> "E";
          case WEST -> "W";
          case UP -> "U";
          case DOWN -> "D";
        };
        quads.addAll(base.getQuads(state, side, rand, extraData, renderType));
      }
    }
    // Add the base model's quads. Can also do something different with the quads here, depending on what you need.
    // add other elements to the quads list as needed here
    return quads;
  }

  @Override
  public BakedModel applyTransform(ItemDisplayContext transformType, PoseStack poseStack,
      boolean applyLeftHandTransform) {
    return base.applyTransform(transformType, poseStack, applyLeftHandTransform);
  }

  @Override
  public ItemTransforms getTransforms() {
    return base.getTransforms();
  }
}
