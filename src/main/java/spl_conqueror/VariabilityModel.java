package spl_conqueror;

import org.dom4j.Element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

/**
 * Central model to store all configuration options and their constraints.
 */
public final class VariabilityModel {

  @Nonnull
  private final String name;

  @Nonnull
  private final Map<String, BinaryOption> binaryOptions;

  @Nonnull
  private final List<String> binaryConstraints;

  @Nonnull
  private final String rootName;

  public VariabilityModel(Element element) {
    name = element.attributeValue("name");
    binaryOptions = new HashMap<>();
    Iterator<Element> it = element.element("binaryOptions").elementIterator();
    Collection<BinaryOption> topLevelOptions = new ArrayList<>();
    while (it.hasNext()) {
      BinaryOption option = new BinaryOption(it.next());
      if (option.isTopLevel()) {
        topLevelOptions.add(option);
      }
      binaryOptions.put(option.getName(), option);
    }
    List<BinaryOption> possibleRootOptions = topLevelOptions.stream()
                                                            .filter(BinaryOption::isRoot)
                                                            .collect(Collectors.toList());
    if (possibleRootOptions.size() == 1) {
      rootName = possibleRootOptions.get(0).getName();
    } else {
      BinaryOption root = BinaryOption.createRoot();
      binaryOptions.put(BinaryOption.ROOT_NAME, root);
      rootName = BinaryOption.ROOT_NAME;
    }
    binaryConstraints = new ArrayList<>();
    it = element.element("booleanConstraints").elementIterator();
    while (it.hasNext()) {
      binaryConstraints.add(it.next().getStringValue());
    }
    if (topLevelOptions.size() > 1) {
      for (BinaryOption option : topLevelOptions) {
        if (!option.getName().equals(rootName)) {
          option.setParentName(rootName);
        }
      }
    }
    binaryOptions.values().forEach(opt -> opt.finalizeInitialization(this));
  }

  @Nonnull
  public List<BinaryOption> getBinaryOptions() {
    return new ArrayList<>(binaryOptions.values());
  }

  @Nonnull
  public String getName() {
    return name;
  }

  @Nonnull
  public BinaryOption getRoot() {
    return binaryOptions.get(rootName);
  }

  @Nonnull
  public List<String> getBinaryConstraints() {
    return binaryConstraints;
  }

  @Nonnull
  public BinaryOption getBinaryOption(String optionName) {
    if (!binaryOptions.containsKey(optionName)) {
      throw new IllegalArgumentException(optionName + " is not part of the variability model");
    }
    return binaryOptions.get(optionName);
  }
}
