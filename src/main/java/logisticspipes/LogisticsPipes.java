package logisticspipes;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import logisticspipes.client.ClientManager;
import logisticspipes.data.LogisticsPipesBlockStateProvider;
import logisticspipes.data.LogisticsPipesItemStateProvider;
import logisticspipes.data.LogisticsPipesLanguageProvider;
import logisticspipes.data.LogisticsPipesParticleProvider;
import logisticspipes.grow.ServerTickDispatcher;
import logisticspipes.logisticspipes.LogisticsManager;
import logisticspipes.network.PacketHandler;
import logisticspipes.particle.LogisticsPipesParticleTypes;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.specialconnection.SpecialPipeConnection;
import logisticspipes.proxy.specialconnection.SpecialTileConnection;
import logisticspipes.routing.RouterManager;
import logisticspipes.routing.ServerRouter;
import logisticspipes.routing.pathfinder.PipeInformationManager;
import logisticspipes.ticks.LPTickHandler;
import logisticspipes.utils.InventoryUtilFactory;
import logisticspipes.utils.RoutedItemHelper;
import logisticspipes.world.inventory.LogisticsPipesMenuTypes;
import logisticspipes.world.item.LogisticsPipesCreativeModTabs;
import logisticspipes.world.item.LogisticsPipesItems;
import logisticspipes.world.item.component.LogisticsPipesDataComponents;
import logisticspipes.world.level.block.LogisticsPipesBlocks;
import logisticspipes.world.level.block.entity.LogisticsGenericPipeBlockEntity;
import logisticspipes.world.level.block.entity.LogisticsPipesBlockEntityTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

@Mod(LogisticsPipes.ID)
public class LogisticsPipes {

  public static final String ID = "logisticspipes";
  public static Logger LOG = LogUtils.getLogger();

  public LogisticsPipes(ModContainer modContainer, Dist dist) {
    NeoForge.EVENT_BUS.register(this);
    NeoForge.EVENT_BUS.register(new LPTickHandler());

    var modEventBus = modContainer.getEventBus();
    modEventBus.addListener(this::handleRegisterCapabilities);
    modEventBus.addListener(this::handleGatherData);

    var routerManager = new RouterManager();
    SimpleServiceLocator.setRouterManager(routerManager);
    SimpleServiceLocator.setChannelConnectionManager(routerManager);
    SimpleServiceLocator.setLogisticsManager(new LogisticsManager());
    SimpleServiceLocator.setInventoryUtilFactory(new InventoryUtilFactory());
    SimpleServiceLocator.setPipeInformationManager(new PipeInformationManager());
    SimpleServiceLocator.setSpecialConnectionHandler(new SpecialPipeConnection());
    SimpleServiceLocator.setSpecialConnectionHandler(new SpecialTileConnection());
    SimpleServiceLocator.setRoutedItemHelper(new RoutedItemHelper());

    PacketHandler.register(modEventBus);

    if (dist.isClient()) {
      ClientManager.init(modEventBus);
    }

    LogisticsPipesBlocks.register(modEventBus);
    LogisticsPipesItems.register(modEventBus);
    LogisticsPipesBlockEntityTypes.register(modEventBus);
    LogisticsPipesCreativeModTabs.register(modEventBus);
    LogisticsPipesMenuTypes.register(modEventBus);
    LogisticsPipesDataComponents.register(modEventBus);
    LogisticsPipesParticleTypes.register(modEventBus);
  }

  // Mod Events
  private void handleRegisterCapabilities(RegisterCapabilitiesEvent event) {
    event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
        LogisticsPipesBlockEntityTypes.PIPE_BASIC.get(), LogisticsGenericPipeBlockEntity::getItemHandlerCapability);
    event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
        LogisticsPipesBlockEntityTypes.CHASSI_MK2.get(), LogisticsGenericPipeBlockEntity::getItemHandlerCapability);
  }

  private void handleGatherData(GatherDataEvent event) {
    var generator = event.getGenerator();
    var packOutput = generator.getPackOutput();
    var lookupProvider = event.getLookupProvider();
    var fileHelper = event.getExistingFileHelper();

    generator.addProvider(event.includeClient(), new LogisticsPipesBlockStateProvider(packOutput, fileHelper));
    generator.addProvider(event.includeClient(), new LogisticsPipesItemStateProvider(packOutput, fileHelper));
    generator.addProvider(event.includeClient(), new LogisticsPipesLanguageProvider(packOutput));
    generator.addProvider(event.includeClient(), new LogisticsPipesParticleProvider(packOutput, fileHelper));
  }

  // NeoForge Events
  @SubscribeEvent
  public void handleServerAboutToStart(ServerAboutToStartEvent event) {
    ServerTickDispatcher.INSTANCE.serverStart();
  }

  @SubscribeEvent
  public void handleServerStopping(ServerStoppingEvent event) {
    SimpleServiceLocator.routerManager.serverStopClean();
    ServerRouter.cleanup();
    ServerTickDispatcher.INSTANCE.cleanup();
  }

  @SubscribeEvent
  public void handleWorldLoad(LevelEvent.Load event) {
    if (event.getLevel().isClientSide()) {
      SimpleServiceLocator.routerManager.clearClientRouters();
    }
  }

  @SubscribeEvent
  public void handleWorldUnload(LevelEvent.Unload event) {
    if (!event.getLevel().isClientSide()) {
      SimpleServiceLocator.routerManager.dimensionUnloaded(((Level)event.getLevel()).dimension());
    }
  }

  public static boolean isDebug() {
    return !FMLLoader.isProduction();
  }

  public static ResourceLocation rl(String path) {
    return ResourceLocation.fromNamespaceAndPath(ID, path);
  }
}
