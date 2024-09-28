package logisticspipes.proxy;

import logisticspipes.interfaces.routing.IChannelConnectionManager;
import logisticspipes.logisticspipes.ILogisticsManager;
import logisticspipes.proxy.specialconnection.SpecialPipeConnection;
import logisticspipes.proxy.specialconnection.SpecialTileConnection;
import logisticspipes.routing.RouterManager;
import logisticspipes.routing.pathfinder.PipeInformationManager;
import logisticspipes.utils.InventoryUtilFactory;
import logisticspipes.utils.RoutedItemHelper;

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

  public static RouterManager routerManager;

  public static void setRouterManager(final RouterManager routerManager) {
    SimpleServiceLocator.routerManager = routerManager;
  }

  public static ILogisticsManager logisticsManager;

  public static void setLogisticsManager(final ILogisticsManager logisticsMngr) {
    SimpleServiceLocator.logisticsManager = logisticsMngr;
  }

  public static SpecialPipeConnection specialpipeconnection;

  public static void setSpecialConnectionHandler(final SpecialPipeConnection special) {
    SimpleServiceLocator.specialpipeconnection = special;
  }

  public static IChannelConnectionManager connectionManager;

  public static void setChannelConnectionManager(final IChannelConnectionManager connectionManager) {
    SimpleServiceLocator.connectionManager = connectionManager;
  }

  public static SpecialTileConnection specialtileconnection;

  public static void setSpecialConnectionHandler(final SpecialTileConnection special) {
    SimpleServiceLocator.specialtileconnection = special;
  }

  public static RoutedItemHelper routedItemHelper;

  public static void setRoutedItemHelper(RoutedItemHelper helper) {
    SimpleServiceLocator.routedItemHelper = helper;
  }

}
