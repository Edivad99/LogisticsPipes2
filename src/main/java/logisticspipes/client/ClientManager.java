package logisticspipes.client;

import logisticspipes.client.gui.screen.inventory.BasicPipeScreen;
import logisticspipes.client.gui.screen.inventory.ChassiPipeScreen;
import logisticspipes.client.gui.screen.inventory.ItemSinkModuleScreen;
import logisticspipes.client.particle.SparkParticle;
import logisticspipes.client.renderer.blockentity.LogisticsPipesEntityRenderers;
import logisticspipes.particle.LogisticsPipesParticleTypes;
import logisticspipes.world.inventory.LogisticsPipesMenuTypes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

public class ClientManager {

  public static void init(IEventBus modEventBus) {
    modEventBus.addListener(ClientManager::handleRegisterMenuScreens);
    modEventBus.addListener(ClientManager::handleRegisterRenderers);
    modEventBus.addListener(ClientManager::handleParticleRegistration);
  }

  private static void handleRegisterMenuScreens(RegisterMenuScreensEvent event) {
    event.register(LogisticsPipesMenuTypes.BASIC_PIPE.get(), BasicPipeScreen::new);
    event.register(LogisticsPipesMenuTypes.CHASSI_PIPE.get(), ChassiPipeScreen::new);
    event.register(LogisticsPipesMenuTypes.ITEM_SINK.get(), ItemSinkModuleScreen::new);
  }

  private static void handleRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
    LogisticsPipesEntityRenderers.register(event);
  }

  private static void handleParticleRegistration(RegisterParticleProvidersEvent event) {
    event.registerSpriteSet(LogisticsPipesParticleTypes.SPARKLE.get(), SparkParticle.Provider::new);
  }
}
