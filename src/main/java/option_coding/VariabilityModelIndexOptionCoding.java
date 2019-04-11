package option_coding;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    binaryOptions.sort((x, y) -> x.getName().compareToIgnoreCase(y.getName()));
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
  public BinaryOption decodeBinaryOption(String str) {
    return vm.getBinaryOption(decodingSubstitutions.get(str));
  }

  @Nonnull
  @Override
  public String encodeBinaryOptions(Iterable<BinaryOption> options) {
    return StreamSupport.stream(options.spliterator(), false)
                        .map(BinaryOption::getName)
                        .map(encodingSubstitutions::get)
                        .collect(Collectors.joining(OPTION_SEPARATOR));
  }
}
