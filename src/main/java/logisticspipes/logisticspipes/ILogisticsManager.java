package logisticspipes.logisticspipes;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.IRouter;
import net.minecraft.world.item.Item;

public interface ILogisticsManager {

  IRoutedItem assignDestinationFor(IRoutedItem item, int sourceRouterId, boolean excludeSource);

  LinkedList<Item> getCraftableItems(List<ExitRoute> list);

  Map<Item, Integer> getAvailableItems(List<ExitRoute> list);

  String getBetterRouterName(IRouter r);

  int getAmountFor(Item item, List<ExitRoute> validDestinations);
  //boolean request(LogisticsTransaction transaction, List<IRouter> validDestinations, List<ItemMessage> errors, boolean realrequest, boolean denyCrafterAdding);
  //boolean request(LogisticsTransaction transaction, List<IRouter> validDestinations, List<ItemMessage> errors);
  //boolean request(LogisticsRequest originalRequest, List<IRouter> validDestinations, List<ItemMessage> errors);
}
