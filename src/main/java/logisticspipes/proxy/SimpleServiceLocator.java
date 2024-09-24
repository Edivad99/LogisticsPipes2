package logisticspipes.proxy;

import logisticspipes.routing.pathfinder.PipeInformationManager;
import logisticspipes.utils.InventoryUtilFactory;

public final class SimpleServiceLocator {

  private SimpleServiceLocator() {
  }

  public static InventoryUtilFactory inventoryUtilFactory;

  public static void setInventoryUtilFactory(final InventoryUtilFactory invUtilFactory) {
    SimpleServiceLocator.inventoryUtilFactory = invUtilFactory;
  }

  public static PipeInformationManager pipeInformationManager;

  public static void setPipeInformationManager(PipeInformationManager manager) {
    SimpleServiceLocator.pipeInformationManager = manager;
  }

}
