package logisticspipes.world.item;

import logisticspipes.modules.ModuleItemSink;
import logisticspipes.utils.item.ModuleInventory;
import logisticspipes.world.inventory.item.ItemSinkModuleMenu;
import logisticspipes.world.item.component.LogisticsPipesDataComponents;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class ItemSinkModule extends Item implements MenuProvider {

  private final ModuleItemSink module = new ModuleItemSink();

  public ItemSinkModule(Properties properties) {
    super(properties.component(DataComponents.CONTAINER,
        ItemContainerContents.fromItems(NonNullList.withSize(9, ItemStack.EMPTY))));
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    var itemStack = player.getItemInHand(hand);
    if (player instanceof ServerPlayer serverPlayer) {
      serverPlayer.openMenu(this, buf -> buf.writeEnum(hand));
    }
    return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    return super.useOn(context);
  }

  @Override
  public Component getDisplayName() {
    //TODO: Change
    return Component.literal("Requested items");
  }

  @Override
  public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
    var hand = player.getUsedItemHand();
    var itemStack = player.getItemInHand(hand);
    return new ItemSinkModuleMenu(containerId, inventory, new ModuleInventory(itemStack, hand));
  }

  public static void setDefaultRoute(ItemStack stack, boolean defaultRoute) {
    stack.set(LogisticsPipesDataComponents.DEFAULT_ROUTE, defaultRoute);
  }

  public static boolean getDefaultRoute(ItemStack stack) {
    return stack.getOrDefault(LogisticsPipesDataComponents.DEFAULT_ROUTE, false);
  }
}
