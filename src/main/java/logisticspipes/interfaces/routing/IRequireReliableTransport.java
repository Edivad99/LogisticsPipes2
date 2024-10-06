package logisticspipes.interfaces.routing;

import logisticspipes.utils.item.ItemIdentifierStack;

public interface IRequireReliableTransport {

  void itemLost(ItemIdentifierStack itemStack, IAdditionalTargetInformation targetInfo);

  void itemArrived(ItemIdentifierStack item, IAdditionalTargetInformation targetInfo);
}
