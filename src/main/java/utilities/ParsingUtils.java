package utilities;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import spl_conqueror.BinaryOption;
import spl_conqueror.VariabilityModel;

public final class ParsingUtils {

  @Nonnull
  private static final String OPTION_SEPARATOR = ",";

  @Nonnull
  private static final String CONFIG_SEPARATOR = ";";

  private ParsingUtils() {
  }

  @Nonnull
  public static List<BinaryOption> decodedBinaryOptions(String str, VariabilityModel vm) {
    return Arrays.stream(str.split(OPTION_SEPARATOR))
                 .map(vm::getBinaryOption)
                 .collect(Collectors.toList());
  }

  @Nonnull
  public static String encodedBinaryOptions(Collection<BinaryOption> options) {
    return options.stream()
                  .map(BinaryOption::getName)
                  .collect(Collectors.joining(OPTION_SEPARATOR));
  }

  @Nonnull
  public static <E extends Collection<BinaryOption>> String encodedBinaryOptionsCollection(
      Collection<E> configs) {
    return configs.stream()
                  .map(ParsingUtils::encodedBinaryOptions)
                  .collect(Collectors.joining(CONFIG_SEPARATOR));
  }

}
