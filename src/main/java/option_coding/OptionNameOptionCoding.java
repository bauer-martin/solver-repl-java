package option_coding;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnull;

import spl_conqueror.BinaryOption;
import spl_conqueror.VariabilityModel;

public final class OptionNameOptionCoding extends AbstractOptionCoding {

  public OptionNameOptionCoding(VariabilityModel vm) {
    super(vm);
  }

  @Nonnull
  @Override
  public Set<BinaryOption> decodeBinaryOptions(String str) {
    return Arrays.stream(str.split(OPTION_SEPARATOR))
                 .map(vm::getBinaryOption)
                 .collect(Collectors.toSet());
  }

  @Nonnull
  @Override
  public String encodeBinaryOptions(Iterable<BinaryOption> options) {
    return StreamSupport.stream(options.spliterator(), false)
                        .map(BinaryOption::getName)
                        .collect(Collectors.joining(OPTION_SEPARATOR));
  }

  @Nonnull
  @Override
  public <E extends Iterable<BinaryOption>> String encodeBinaryOptionsIterable(
      Iterable<E> configs) {
    return StreamSupport.stream(configs.spliterator(), false)
                        .map(this::encodeBinaryOptions)
                        .collect(Collectors.joining(CONFIG_SEPARATOR));
  }
}
