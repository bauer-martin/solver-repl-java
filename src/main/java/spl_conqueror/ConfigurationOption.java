package spl_conqueror;

import org.dom4j.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class encapsulates all properties that have binary and numeric options in common.
 */
@SuppressWarnings({ "WeakerAccess", "AbstractClassWithoutAbstractMethods" })
public abstract class ConfigurationOption implements Comparable<ConfigurationOption> {

  private static final Pattern DISJUNCTIVE_OPTIONS = Pattern.compile(" *\\| *");

  @Nonnull
  protected final String name;

  @Nonnull
  protected final String outputString;

  @Nonnull
  protected final String prefix;

  @Nonnull
  protected final String postfix;

  @Nullable
  private String parentName;

  @Nonnull
  private final Collection<List<String>> impliedOptionsNames;

  @Nonnull
  private final Collection<List<String>> excludedOptionsNames;

  @Nonnull
  protected Collection<List<ConfigurationOption>> impliedOptions;

  @Nonnull
  protected Collection<List<ConfigurationOption>> excludedOptions;

  @Nullable
  protected ConfigurationOption parent;

  private boolean hasFinalizedInitialization;

  protected ConfigurationOption(Element element) {
    name = element.elementTextTrim("name");
    outputString = element.elementTextTrim("outputString");
    prefix = element.elementTextTrim("prefix");
    postfix = element.elementTextTrim("postfix");
    parentName = nullIfEmpty(element.elementTextTrim("parent"));
    impliedOptionsNames = new ArrayList<>();
    Iterator<Element> it = element.element("impliedOptions").elementIterator();
    while (it.hasNext()) {
      String[] tokens = DISJUNCTIVE_OPTIONS.split(it.next().getTextTrim());
      impliedOptionsNames.add(Arrays.asList(tokens));
    }
    impliedOptions = new ArrayList<>(impliedOptionsNames.size());
    excludedOptionsNames = new ArrayList<>();
    it = element.element("excludedOptions").elementIterator();
    while (it.hasNext()) {
      String[] tokens = DISJUNCTIVE_OPTIONS.split(it.next().getTextTrim());
      excludedOptionsNames.add(Arrays.asList(tokens));
    }
    excludedOptions = new ArrayList<>(excludedOptionsNames.size());
    hasFinalizedInitialization = false;
  }

  protected ConfigurationOption(String name) {
    this.name = name;
    outputString = "";
    prefix = "";
    postfix = "";
    parentName = null;
    impliedOptionsNames = new ArrayList<>();
    impliedOptions = new ArrayList<>();
    excludedOptionsNames = new ArrayList<>();
    excludedOptions = new ArrayList<>();
    hasFinalizedInitialization = false;
  }

  @Nullable
  private static String nullIfEmpty(String s) {
    return s.isEmpty() ? null : s;
  }

  void setParentName(String parentName) {
    if (hasFinalizedInitialization) {
      throw new UnsupportedOperationException("references have already been set");
    }
    this.parentName = parentName;
  }

  /**
   * Replaces the names for parent, children, etc. with their actual objects.
   */
  void finalizeInitialization(VariabilityModel vm) {
    if (hasFinalizedInitialization) {
      throw new UnsupportedOperationException("references have already been set");
    }
    if (parentName != null) {
      parent = vm.getBinaryOption(parentName);
    }

    impliedOptions = new ArrayList<>(impliedOptionsNames.size());
    for (List<String> impliedOptionsName : impliedOptionsNames) {
      List<ConfigurationOption> collect = impliedOptionsName.stream()
                                                            .map(vm::getBinaryOption)
                                                            .collect(Collectors.toList());
      impliedOptions.add(collect);
    }

    excludedOptions = new ArrayList<>(excludedOptionsNames.size());
    for (List<String> optionNames : excludedOptionsNames) {
      List<ConfigurationOption> list = optionNames.stream()
                                                  .map(vm::getBinaryOption)
                                                  .collect(Collectors.toList());
      excludedOptions.add(list);
    }
    hasFinalizedInitialization = true;
  }

  @Nonnull
  public String getName() {
    return name;
  }

  @Nonnull
  public String getOutputString() {
    return outputString;
  }

  @Nonnull
  public String getPrefix() {
    return prefix;
  }

  @Nonnull
  public String getPostfix() {
    return postfix;
  }

  /**
   * List, in which the current option implies one and/or a combination of other options.
   */
  @Nonnull
  public Collection<List<ConfigurationOption>> getImpliedOptions() {
    return impliedOptions;
  }

  public boolean hasImpliedOptions() {
    return !impliedOptions.isEmpty();
  }

  /**
   * List, in which the current option excludes the selection of one and/or a combination of
   * other options.
   */
  @Nonnull
  public Collection<List<ConfigurationOption>> getExcludedOptions() {
    return excludedOptions;
  }

  public boolean hasExcludedOptions() {
    return !excludedOptions.isEmpty();
  }

  public boolean isTopLevel() {
    return parentName == null;
  }

  @Nullable
  public ConfigurationOption getParent() {
    return parent;
  }

  public boolean isChildOf(ConfigurationOption option) {
    return parent != null && parent.equals(option);
  }

  @Override
  public int compareTo(ConfigurationOption other) {
    return name.compareTo(other.name);
  }

  /**
   * Checks whether the given option is an ancestor of the current option.
   * @param option The configuration option that might be an ancestor.
   * @return True if it is an ancestor, false otherwise
   */
  public boolean isAncestor(ConfigurationOption option) {
    if (parent == null) {
      return false;
    }
    return parent.equals(option) || parent.isAncestor(option);
  }

  @Nonnull
  @Override
  public String toString() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ConfigurationOption)) {
      return false;
    }
    ConfigurationOption that = (ConfigurationOption) o;
    return name.equals(that.name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }
}
