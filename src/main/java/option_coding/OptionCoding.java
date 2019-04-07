package option_coding;

import java.util.Set;

import javax.annotation.Nonnull;

import spl_conqueror.BinaryOption;

public interface OptionCoding {

  @Nonnull
  Set<BinaryOption> decodeBinaryOptions(String str);

  @Nonnull
  String encodeBinaryOptions(Iterable<BinaryOption> options);

  @Nonnull
  <E extends Iterable<BinaryOption>> String encodeBinaryOptionsIterable(Iterable<E> configs);
}
