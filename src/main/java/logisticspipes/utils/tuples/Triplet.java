package logisticspipes.utils.tuples;

import org.jetbrains.annotations.Nullable;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.Tuple;

@Getter
@Setter
public class Triplet<A, B, C> extends Tuple<A, B> {

  @Nullable
  protected C c;

  public Triplet(@Nullable A a, @Nullable B b, @Nullable C c) {
    super(a, b);
    this.c = c;
  }

  @Override
  protected Triplet<A, B, C> clone() {
    return new Triplet<>(this.getA(), this.getB(), this.c);
  }
}
