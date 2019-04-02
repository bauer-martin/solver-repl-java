package spl_conqueror;

import org.dom4j.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class BinaryOption {

  @Nonnull
  public static final String ROOT_NAME = "root";

  @Nonnull
  private static final Pattern DISJUNCTIVE_OPTIONS = Pattern.compile(" *\\| *");

  @Nonnull
  private final String name;

  private final boolean isOptional;

  @Nonnull
  private final Collection<List<String>> impliedOptionsNames;

  @Nonnull
  private final Collection<List<String>> excludedOptionsNames;

  @Nullable
  private String parentName;

  @Nullable
  private BinaryOption parent;

  @Nonnull
  private Collection<List<BinaryOption>> impliedOptions;

  @Nonnull
  private Collection<List<BinaryOption>> excludedOptions;

  private boolean hasFinalizedInitialization;

  public BinaryOption(Element element) {
    name = element.elementTextTrim("name");
    isOptional = element.elementTextTrim("optional").equals("True");
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

  private BinaryOption() {
    name = ROOT_NAME;
    isOptional = false;
    parentName = null;
    impliedOptionsNames = new ArrayList<>();
    impliedOptions = new ArrayList<>();
    excludedOptionsNames = new ArrayList<>();
    excludedOptions = new ArrayList<>();
    hasFinalizedInitialization = false;
  }

  @Nonnull
  public static BinaryOption createRoot() {
    return new BinaryOption();
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
      List<BinaryOption> collect = impliedOptionsName.stream()
                                                     .map(vm::getBinaryOption)
                                                     .collect(Collectors.toList());
      impliedOptions.add(collect);
    }

    excludedOptions = new ArrayList<>(excludedOptionsNames.size());
    for (List<String> optionNames : excludedOptionsNames) {
      List<BinaryOption> list = optionNames.stream()
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

  public boolean isOptional() {
    return isOptional;
  }

  public boolean isMandatory() {
    return !isOptional;
  }

  public boolean isRoot() {
    return isTopLevel() && !isOptional;
  }

  /**
   * List, in which the current option implies one and/or a combination of other options.
   */
  @Nonnull
  public Collection<List<BinaryOption>> getImpliedOptions() {
    return Collections.unmodifiableCollection(impliedOptions);
  }

  public boolean hasImpliedOptions() {
    return !impliedOptions.isEmpty();
  }

  /**
   * List, in which the current option excludes the selection of one and/or a combination of
   * other options.
   */
  @Nonnull
  public Collection<List<BinaryOption>> getExcludedOptions() {
    return Collections.unmodifiableCollection(excludedOptions);
  }

  public boolean hasExcludedOptions() {
    return !excludedOptions.isEmpty();
  }

  public boolean isTopLevel() {
    return parentName == null;
  }

  @Nullable
  public BinaryOption getParent() {
    return parent;
  }

  /**
   * Checks whether this binary option has alternative options meaning that there are other
   * binary options with the same parents, but cannot be present in the same configuration as this
   * option.
   *
   * @return True if it has alternative options, false otherwise
   */
  public boolean hasAlternatives() {
    if (isOptional) {
      return false;
    }
    return excludedOptions.stream()
                          .anyMatch(group -> group.stream()
                                                  .allMatch(o -> Objects.equals(o.parent, parent)));
  }

  /**
   * Collects all options that are excluded by this option and that have the same parent
   * (i.e., the alternative group)
   */
  @Nonnull
  public List<BinaryOption> collectAlternativeOptions() {
    List<BinaryOption> result = new ArrayList<>();
    if (isOptional) {
      return result;
    }
    result = excludedOptions.stream()
                            .filter(group -> group.size() == 1)
                            .map(group -> group.get(0))
                            .filter(option -> Objects.equals(option.parent, parent)
                                              && option.isMandatory())
                            .collect(Collectors.toList());
    return result;
  }

  /**
   * Collects all options that are excluded by this option, but do not have the same parent
   * (i.e., cross-tree constraints)
   */
  @Nonnull
  public Collection<List<BinaryOption>> getNonAlternativeExcludedOptions() {
    Collection<List<BinaryOption>> result = new ArrayList<>();
    for (List<BinaryOption> group : excludedOptions) {
      if (group.size() == 1) {
        BinaryOption option = group.get(0);
        if (Objects.equals(option.parent, parent)) {
          if (isOptional && option.isOptional) {
            result.add(group);
          }
        } else {
          result.add(group);
        }
      }
    }
    return result;
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
    if (!(o instanceof BinaryOption)) {
      return false;
    }
    //noinspection QuestionableName
    BinaryOption that = (BinaryOption) o;
    return name.equals(that.name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }
}
