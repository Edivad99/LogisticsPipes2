package logisticspipes.particle;

import org.jetbrains.annotations.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;

public class PipeFXRenderHandler {

  public static void spawnGenericParticle(@Nullable ClientLevel level, Particles particle,
      double x, double y, double z, int amount) {
    if (level == null) {
      return;
    }
    try {
      Minecraft mc = Minecraft.getInstance();
      boolean isMinimal = mc.options.particles().get().equals(ParticleStatus.MINIMAL);
      double distance = 16.0D;
      if (mc.getCameraEntity().distanceToSqr(x, y, z) > distance * distance) {
        return;
      } else if (isMinimal) {
        return;
      }

      for (int i = 0; i < Math.sqrt(amount); i++) {
        level.addParticle(particle.getSparkleFXParticleOptions(amount), x, y, z, 0, 0, 0);
      }
    } catch (NullPointerException ignored) {}
  }
}
