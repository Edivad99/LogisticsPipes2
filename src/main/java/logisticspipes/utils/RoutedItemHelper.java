package logisticspipes.utils;

import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.routing.ItemRoutingInformation;
import logisticspipes.transport.LPTravelingItem;
import logisticspipes.utils.item.ItemIdentifierStack;
import net.minecraft.world.item.ItemStack;

public class RoutedItemHelper {

  public LPTravelingItem.LPTravelingItemServer createNewTravelItem(ItemStack item) {
    return createNewTravelItem(ItemIdentifierStack.getFromStack(item));
  }

  public LPTravelingItem.LPTravelingItemServer createNewTravelItem(ItemIdentifierStack item) {
    return new LPTravelingItem.LPTravelingItemServer(item);
  }

  public LPTravelingItem.LPTravelingItemServer createNewTravelItem(ItemRoutingInformation info) {
    return new LPTravelingItem.LPTravelingItemServer(info);
  }

  public LPTravelingItem.LPTravelingItemServer getServerTravelingItem(IRoutedItem item) {
    return (LPTravelingItem.LPTravelingItemServer) item;
  }

}
