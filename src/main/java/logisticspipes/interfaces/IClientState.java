package logisticspipes.interfaces;

import net.minecraft.network.FriendlyByteBuf;

public interface IClientState {

  void writeData(FriendlyByteBuf buf);

  void readData(FriendlyByteBuf buf);
}
