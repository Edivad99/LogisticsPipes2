package logisticspipes.interfaces.routing;

import net.minecraft.world.item.ItemStack;

public interface IRequireReliableTransport {

  void itemLost(ItemStack itemStack, IAdditionalTargetInformation targetInfo);

  void itemArrived(ItemStack item, IAdditionalTargetInformation targetInfo);
}
