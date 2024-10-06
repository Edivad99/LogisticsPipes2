package logisticspipes.interfaces.routing;

import logisticspipes.utils.item.ItemIdentifierStack;

public interface IRequestItems extends Comparable<IRequestItems>, IRequest {

  void itemCouldNotBeSend(ItemIdentifierStack item, IAdditionalTargetInformation info);

  default int compareTo(IRequestItems other) {
    return Integer.compare(this.getID(), other.getID());
  }
}
