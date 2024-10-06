package logisticspipes.data;

import logisticspipes.LogisticsPipes;
import logisticspipes.particle.LogisticsPipesParticleTypes;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.ParticleDescriptionProvider;

public class LogisticsPipesParticleProvider extends ParticleDescriptionProvider {

  public LogisticsPipesParticleProvider(PackOutput output, ExistingFileHelper fileHelper) {
    super(output, fileHelper);
  }

  @Override
  protected void addDescriptions() {
    this.spriteSet(LogisticsPipesParticleTypes.SPARKLE.get(),
        LogisticsPipes.rl("sparkle"), 4, false);
  }
}
