package logisticspipes.routing

object AsyncRouting {
  fun getDistance(sourceRouter: ServerRouter, destinationRouter: IRouter): List<ExitRoute>? {
    return if (sourceRouter.routeTable.size <= destinationRouter.simpleID) {
      null
    } else {
      sourceRouter.routeTable[destinationRouter.simpleID]
    }
  }

  fun ServerRouter.updateServerRouterLsa() {
    if (connectionNeedsChecking != 0 && checkAdjacentUpdate()) {
      updateLsa()
    }
  }

  fun ServerRouter.needsRoutingTableUpdate(): Boolean = LSAVersion > ServerRouter.lastLSAVersion[simpleID]

  fun updateRoutingTable(serverRouter: ServerRouter) {
    if (serverRouter.LSAVersion > ServerRouter.lastLSAVersion[serverRouter.simpleID]) {
      serverRouter.CreateRouteTable(serverRouter.LSAVersion)
    }
  }
}
