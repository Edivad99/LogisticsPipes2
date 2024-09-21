package logisticspipes.network;

import logisticspipes.LogisticsPipes;
import logisticspipes.network.to_server.ImportItemSinkMessage;
import logisticspipes.network.to_server.SetDefaultRouteItemSinkMessage;
import logisticspipes.network.to_server.UpdateModuleItemSinkMessage;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class PacketHandler {

  private PacketHandler() {}

  public static void register(IEventBus modEventBus) {
    modEventBus.addListener(RegisterPayloadHandlersEvent.class, event -> {
      var registrar = event.registrar(LogisticsPipes.ID).versioned("1");
      registerClientToServer(registrar);
      registerServerToClient(registrar);
    });
  }

  private static void registerClientToServer(PayloadRegistrar registrar) {
    registrar.playToServer(ImportItemSinkMessage.TYPE,
        ImportItemSinkMessage.STREAM_CODEC, ImportItemSinkMessage::handle);
    registrar.playToServer(SetDefaultRouteItemSinkMessage.TYPE,
        SetDefaultRouteItemSinkMessage.STREAM_CODEC, SetDefaultRouteItemSinkMessage::handle);
    registrar.playToServer(UpdateModuleItemSinkMessage.TYPE,
        UpdateModuleItemSinkMessage.STREAM_CODEC, UpdateModuleItemSinkMessage::handle);
  }

  private static void registerServerToClient(PayloadRegistrar registrar) {

  }
}
