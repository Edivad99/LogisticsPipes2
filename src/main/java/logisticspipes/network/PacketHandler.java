package logisticspipes.network;

import logisticspipes.LogisticsPipes;
import logisticspipes.network.to_client.ItemBufferSyncPacket;
import logisticspipes.network.to_client.PipeBlockEntityStatePacket;
import logisticspipes.network.to_client.PipeContentPacket;
import logisticspipes.network.to_client.PipePositionPacket;
import logisticspipes.network.to_server.ImportItemSinkMessage;
import logisticspipes.network.to_server.PipeContentRequest;
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
    registrar.playToServer(PipeContentRequest.TYPE,
        PipeContentRequest.STREAM_CODEC, PipeContentRequest::handle);
  }

  private static void registerServerToClient(PayloadRegistrar registrar) {
    registrar.playToClient(ItemBufferSyncPacket.TYPE,
        ItemBufferSyncPacket.STREAM_CODEC, ItemBufferSyncPacket::handle);
    registrar.playToClient(PipeBlockEntityStatePacket.TYPE,
        PipeBlockEntityStatePacket.STREAM_CODEC, PipeBlockEntityStatePacket::handle);
    registrar.playToClient(PipeContentPacket.TYPE,
        PipeContentPacket.STREAM_CODEC, PipeContentPacket::handle);
    registrar.playToClient(PipePositionPacket.TYPE,
        PipePositionPacket.STREAM_CODEC, PipePositionPacket::handle);
  }
}
