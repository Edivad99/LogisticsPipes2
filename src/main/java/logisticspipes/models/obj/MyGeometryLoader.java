package logisticspipes.models.obj;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import logisticspipes.LogisticsPipes;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.obj.ObjLoader;
import net.neoforged.neoforge.client.model.obj.ObjModel;

public class MyGeometryLoader implements IGeometryLoader<MyGeometry> {

  // It is highly recommended to use a singleton pattern for geometry loaders, as all models can be loaded through one loader.
  public static final MyGeometryLoader INSTANCE = new MyGeometryLoader();
  // The id we will use to register this loader. Also used in the loader datagen class.
  public static final ResourceLocation ID = LogisticsPipes.rl("obj");

  // In accordance with the singleton pattern, make the constructor private.
  private MyGeometryLoader() {
  }

  @Override
  public MyGeometry read(JsonObject jsonObject, JsonDeserializationContext context) throws JsonParseException {
    // Use the given JsonObject and, if needed, the JsonDeserializationContext to get properties from the model JSON.
    // The MyGeometry constructor may have constructor parameters (see below).
    ObjModel model = ObjLoader.INSTANCE.read(jsonObject, context);
    return new MyGeometry(model);
  }
}
