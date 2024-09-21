package logisticspipes.data;

import logisticspipes.LogisticsPipes;
import logisticspipes.world.item.LogisticsPipesItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class LogisticsPipesItemStateProvider extends ItemModelProvider {

  public LogisticsPipesItemStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
    super(output, LogisticsPipes.ID, exFileHelper);
  }

  @Override
  protected void registerModels() {
    this.basicItem(LogisticsPipesItems.MODULE_ITEM_SINK.get());
  }
}
