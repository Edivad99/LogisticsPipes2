package logisticspipes.interfaces.routing;

import net.minecraft.world.item.ItemStack;

public interface IRequestItems extends Comparable<IRequestItems>, IRequest {

  void itemCouldNotBeSend(ItemStack item, IAdditionalTargetInformation info);

  default int compareTo(IRequestItems other) {
    return Integer.compare(this.getID(), other.getID());
  }
}
