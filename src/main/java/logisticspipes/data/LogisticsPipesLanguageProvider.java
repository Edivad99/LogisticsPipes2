package logisticspipes.data;

import logisticspipes.LogisticsPipes;
import logisticspipes.world.item.LogisticsPipesItems;
import logisticspipes.world.level.block.LogisticsPipesBlocks;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class LogisticsPipesLanguageProvider extends LanguageProvider {

  public LogisticsPipesLanguageProvider(PackOutput output) {
    super(output, LogisticsPipes.ID, "en_us");
  }

  @Override
  protected void addTranslations() {
    this.blockTranslations();
    this.itemTranslations();
  }

  private void itemTranslations() {
    this.add(LogisticsPipesItems.MODULE_ITEM_SINK.get(), "ItemSink Module");
  }

  private void blockTranslations() {
    this.add(LogisticsPipesBlocks.PIPE_BASIC.get(), "Basic Pipe");
  }
}
