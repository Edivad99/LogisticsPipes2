package logisticspipes.world.inventory;

import logisticspipes.LogisticsPipes;
import logisticspipes.utils.item.ModuleInventory;
import logisticspipes.world.inventory.item.ItemSinkModuleMenu;
import logisticspipes.world.level.block.entity.BasicPipeBlockEntity;
import logisticspipes.world.level.block.entity.ChassiPipeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class LogisticsPipesMenuTypes {

  private static final DeferredRegister<MenuType<?>> deferredRegister =
      DeferredRegister.create(BuiltInRegistries.MENU, LogisticsPipes.ID);

  public static void register(IEventBus modEventBus) {
    deferredRegister.register(modEventBus);
  }

  public static final DeferredHolder<MenuType<?>, MenuType<BasicPipeMenu>> BASIC_PIPE =
      deferredRegister.register("basic_pipe",
          () -> blockEntityMenu(BasicPipeBlockEntity.class, BasicPipeMenu::new));

  public static final DeferredHolder<MenuType<?>, MenuType<ChassiPipeMenu>> CHASSI_PIPE =
      deferredRegister.register("chassi_pipe",
          () -> blockEntityMenu(ChassiPipeBlockEntity.class, ChassiPipeMenu::new));

  public static final DeferredHolder<MenuType<?>, MenuType<ItemSinkModuleMenu>> ITEM_SINK =
      deferredRegister.register("item_sink",
          () -> itemMenu(ItemSinkModuleMenu::new));

  private static <T extends AbstractContainerMenu> MenuType<T> itemMenu(CustomMenuFactory<T, ModuleInventory> factory) {
    IContainerFactory<T> containerFactory =  (id, inventory, packetBuffer) -> {
      var hand = packetBuffer.readEnum(InteractionHand.class);
      var player = inventory.player;
      var itemStack = player.getItemInHand(hand);
      return factory.create(id, inventory, new ModuleInventory(itemStack, hand));
    };
    return new MenuType<>(containerFactory, FeatureFlags.DEFAULT_FLAGS);
  }

  private static <T extends AbstractContainerMenu, E extends BlockEntity> MenuType<T>
  blockEntityMenu(Class<E> entityType, CustomMenuFactory<T, E> factory) {
    IContainerFactory<T> containerFactory =  (id, inventory, packetBuffer) -> {
      BlockPos blockPos = packetBuffer.readBlockPos();
      BlockEntity entity = inventory.player.level().getBlockEntity(blockPos);
      if (entityType.isInstance(entity)) {
        return factory.create(id, inventory, entityType.cast(entity));
      }
      throw new IllegalStateException(
          "Cannot find block entity of type %s at [%s]".formatted(entityType.getName(), blockPos));
    };
    return new MenuType<>(containerFactory, FeatureFlags.DEFAULT_FLAGS);
  }

  private interface CustomMenuFactory<C extends AbstractContainerMenu, T> {

    C create(int id, Inventory inventory, T data);
  }
}
