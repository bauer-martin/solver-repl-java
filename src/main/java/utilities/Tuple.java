package utilities;

import javax.annotation.Nonnull;

public final class Tuple<A, B> {

  @Nonnull
  private final A first;

  @Nonnull
  private final B second;

  public Tuple(A first, B second) {
    this.first = first;
    this.second = second;
  }

  @Nonnull
  public A getFirst() {
    return first;
  }

  @Nonnull
  public B getSecond() {
    return second;
  }
}
