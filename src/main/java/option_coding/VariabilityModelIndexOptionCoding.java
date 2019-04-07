package option_coding;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnull;

import spl_conqueror.BinaryOption;
import spl_conqueror.VariabilityModel;

public final class VariabilityModelIndexOptionCoding extends AbstractOptionCoding {

  @Nonnull
  private final Map<String, String> encodingSubstitutions;

  @Nonnull
  private final Map<String, String> decodingSubstitutions;

  public VariabilityModelIndexOptionCoding(VariabilityModel vm) {
    super(vm);
    List<BinaryOption> binaryOptions = vm.getBinaryOptions();
    binaryOptions.sort(Comparator.comparing(BinaryOption::getName));
    encodingSubstitutions = new HashMap<>(binaryOptions.size());
    decodingSubstitutions = new HashMap<>(binaryOptions.size());
    for (int i = 0; i < binaryOptions.size(); i++) {
      String value = binaryOptions.get(i).getName();
      String substitution = String.valueOf(i);
      encodingSubstitutions.put(value, substitution);
      decodingSubstitutions.put(substitution, value);
    }
  }

  @Nonnull
  @Override
  public Set<BinaryOption> decodeBinaryOptions(String str) {
    return Arrays.stream(str.split(OPTION_SEPARATOR))
                 .map(decodingSubstitutions::get)
                 .map(vm::getBinaryOption)
                 .collect(Collectors.toSet());
  }

  @Nonnull
  @Override
  public String encodeBinaryOptions(Iterable<BinaryOption> options) {
    return StreamSupport.stream(options.spliterator(), false)
                        .map(BinaryOption::getName)
                        .map(encodingSubstitutions::get)
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
