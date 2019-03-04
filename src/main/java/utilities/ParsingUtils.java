package utilities;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import spl_conqueror.BinaryOption;
import spl_conqueror.VariabilityModel;

public final class ParsingUtils {

  private ParsingUtils() {
  }

  @Nonnull
  public static List<BinaryOption> binaryOptionsFromString(String str, VariabilityModel vm) {
    return Arrays.stream(str.split(","))
                 .map(vm::getBinaryOption)
                 .collect(Collectors.toList());
  }

  @Nonnull
  public static String binaryOptionsToString(Collection<BinaryOption> options) {
    return options.stream().map(BinaryOption::getName).collect(Collectors.joining(","));
  }

  @Nonnull
  public static <E extends Collection<BinaryOption>> String binaryConfigsToString(
      Collection<E> configs) {
    return configs.stream()
                  .map(ParsingUtils::binaryOptionsToString)
                  .collect(Collectors.joining(";"));
  }
}
