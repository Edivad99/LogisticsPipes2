package logisticspipes.models.obj;

import java.io.FileNotFoundException;
import java.util.Map;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import logisticspipes.LogisticsPipes;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.obj.ObjMaterialLibrary;
import net.neoforged.neoforge.client.model.obj.ObjModel;
import net.neoforged.neoforge.client.model.obj.ObjTokenizer;

public class MyObjLoader implements IGeometryLoader<MyObjModel>, ResourceManagerReloadListener {

  public static final MyObjLoader INSTANCE = new MyObjLoader();
  public static final ResourceLocation ID = LogisticsPipes.rl("obj");

  private final Map<ObjModel.ModelSettings, MyObjModel> modelCache = Maps.newConcurrentMap();
  private final Map<ResourceLocation, ObjMaterialLibrary> materialCache = Maps.newConcurrentMap();

  private final ResourceManager manager = Minecraft.getInstance().getResourceManager();

  private MyObjLoader() {
  }

  @Override
  public void onResourceManagerReload(ResourceManager resourceManager) {
    this.modelCache.clear();
    this.materialCache.clear();
  }

  @Override
  public MyObjModel read(JsonObject jsonObject, JsonDeserializationContext deserializationContext)
      throws JsonParseException {
    if (!jsonObject.has("model"))
      throw new JsonParseException("OBJ Loader requires a 'model' key that points to a valid .OBJ model.");

    String modelLocation = jsonObject.get("model").getAsString();

    boolean automaticCulling = GsonHelper.getAsBoolean(jsonObject, "automatic_culling", true);
    boolean shadeQuads = GsonHelper.getAsBoolean(jsonObject, "shade_quads", true);
    boolean flipV = GsonHelper.getAsBoolean(jsonObject, "flip_v", false);
    boolean emissiveAmbient = GsonHelper.getAsBoolean(jsonObject, "emissive_ambient", true);
    String mtlOverride = GsonHelper.getAsString(jsonObject, "mtl_override", null);

    return loadModel(new ObjModel.ModelSettings(ResourceLocation.parse(modelLocation), automaticCulling, shadeQuads, flipV, emissiveAmbient, mtlOverride));
  }

  public MyObjModel loadModel(ObjModel.ModelSettings settings) {
    return modelCache.computeIfAbsent(settings, (data) -> {
      Resource resource = manager.getResource(settings.modelLocation()).orElseThrow();
      try (ObjTokenizer tokenizer = new ObjTokenizer(resource.open())) {
        return MyObjModel.parse(tokenizer, settings);
      } catch (FileNotFoundException e) {
        throw new RuntimeException("Could not find OBJ model", e);
      } catch (Exception e) {
        throw new RuntimeException("Could not read OBJ model", e);
      }
    });
  }
}
