package option_coding;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnull;

import spl_conqueror.BinaryOption;
import spl_conqueror.VariabilityModel;

public abstract class AbstractOptionCoding implements OptionCoding {

  @Nonnull
  protected static final String OPTION_SEPARATOR = ",";

  @Nonnull
  protected static final String CONFIG_SEPARATOR = ";";

  @Nonnull
  protected final VariabilityModel vm;

  protected AbstractOptionCoding(VariabilityModel vm) {
    this.vm = vm;
  }

  @Nonnull
  @Override
  public Set<BinaryOption> decodeBinaryOptions(String str) {
    return Arrays.stream(str.split(OPTION_SEPARATOR))
                 .map(this::decodeBinaryOption)
                 .collect(Collectors.toSet());
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
