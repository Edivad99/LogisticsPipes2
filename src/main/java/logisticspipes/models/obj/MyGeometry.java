package logisticspipes.models.obj;

import java.util.function.Function;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import net.neoforged.neoforge.client.model.obj.ObjModel;

public class MyGeometry implements IUnbakedGeometry<MyGeometry> {
  private final ObjModel base;

  // The constructor may have any parameters you need, and store them in fields for further usage below.
  // If the constructor has parameters, the constructor call in MyGeometryLoader#read must match them.
  public MyGeometry(ObjModel base) {
    this.base = base;
  }

  // Method responsible for model baking, returning our dynamic model. Parameters in this method are:
  // - The geometry baking context. Contains many properties that we will pass into the model, e.g. light and ao values.
  // - The model baker. Can be used for baking sub-models.
  // - The sprite getter. Maps materials (= texture variables) to TextureAtlasSprites. Materials can be obtained from the context.
  //   For example, to get a model's particle texture, call spriteGetter.apply(context.getMaterial("particle"));
  // - The model state. This holds the properties from the blockstate file, e.g. rotations and the uvlock boolean.
  // - The item overrides. This is the code representation of an "overrides" block in an item model.
  @Override
  public BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
      Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides) {
    var model = this.base.bake(context, baker, spriteGetter, modelState, overrides);
    return new MyDynamicModel(model, context.useBlockLight());
  }

  // Method responsible for correctly resolving parent properties. Required if this model loads any nested models or reuses the vanilla loader on itself (see below).
  @Override
  public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, IGeometryBakingContext context) {
    // UnbakedModel#resolveParents
    this.base.resolveParents(modelGetter, context);
  }
}
