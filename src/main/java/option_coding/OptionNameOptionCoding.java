package option_coding;

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
  public BinaryOption decodeBinaryOption(String str) {
    return vm.getBinaryOption(str);
  }

  @Nonnull
  @Override
  public String encodeBinaryOptions(Iterable<BinaryOption> options) {
    return StreamSupport.stream(options.spliterator(), false)
                        .map(BinaryOption::getName)
                        .collect(Collectors.joining(OPTION_SEPARATOR));
  }
}
