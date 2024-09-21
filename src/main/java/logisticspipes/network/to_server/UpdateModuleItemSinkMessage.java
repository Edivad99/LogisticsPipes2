package logisticspipes.network.to_server;

import logisticspipes.LogisticsPipes;
import logisticspipes.world.item.ItemSinkModule;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UpdateModuleItemSinkMessage(
    InteractionHand hand, boolean isDefault) implements CustomPacketPayload {

  public static final Type<UpdateModuleItemSinkMessage> TYPE =
      new Type<>(LogisticsPipes.rl("update_module_item_sink"));

  public static final StreamCodec<RegistryFriendlyByteBuf, UpdateModuleItemSinkMessage> STREAM_CODEC =
      StreamCodec.composite(
          NeoForgeStreamCodecs.enumCodec(InteractionHand.class), UpdateModuleItemSinkMessage::hand,
          ByteBufCodecs.BOOL, UpdateModuleItemSinkMessage::isDefault,
          UpdateModuleItemSinkMessage::new);

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  public static void handle(UpdateModuleItemSinkMessage message, IPayloadContext context) {
    var player = context.player();
    var itemstack = player.getItemInHand(message.hand);
    if (itemstack.getItem() instanceof ItemSinkModule) {
      ItemSinkModule.setDefaultRoute(itemstack, message.isDefault());
    }
  }
}
