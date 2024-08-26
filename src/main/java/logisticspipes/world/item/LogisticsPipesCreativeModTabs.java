package logisticspipes.world.item;

import logisticspipes.LogisticsPipes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class LogisticsPipesCreativeModTabs {

  public static final DeferredRegister<CreativeModeTab> deferredRegister =
      DeferredRegister.create(Registries.CREATIVE_MODE_TAB, LogisticsPipes.ID);

  public static void register(IEventBus modEventBus) {
    deferredRegister.register(modEventBus);
  }

  public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB =
      deferredRegister.register("main_tab", () -> CreativeModeTab.builder()
          .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
          .title(Component.literal("Logistic Pipes"))
          .displayItems((parameters, output) -> {
            output.accept(LogisticsPipesItems.PIPE_TRANSPORT_BASIC.get());
          }).build());
}
