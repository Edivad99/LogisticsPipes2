package logisticspipes.client;

import logisticspipes.client.renderer.blockentity.LogisticsPipesEntityRenderers;
import logisticspipes.models.obj.MyGeometryLoader;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;

public class ClientManager {

  public static void init(IEventBus modEventBus) {
    modEventBus.addListener(ClientManager::registerGeometryLoaders);
    modEventBus.addListener(ClientManager::handleRegisterRenderers);
  }

  public static void registerGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) {
    event.register(MyGeometryLoader.ID, MyGeometryLoader.INSTANCE);
  }

  private static void handleRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
    LogisticsPipesEntityRenderers.register(event);
  }
}
