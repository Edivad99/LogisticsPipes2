package logisticspipes.client;

import logisticspipes.client.renderer.blockentity.LogisticsPipesEntityRenderers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class ClientManager {

  public static void init(IEventBus modEventBus) {
    modEventBus.addListener(ClientManager::handleRegisterRenderers);
  }

  private static void handleRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
    LogisticsPipesEntityRenderers.register(event);
  }
}
