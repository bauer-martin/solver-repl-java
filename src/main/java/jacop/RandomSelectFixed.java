package jacop;

import org.jacop.constraints.PrimitiveConstraint;
import org.jacop.core.Var;
import org.jacop.search.Indomain;
import org.jacop.search.SelectChoicePoint;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

public class RandomSelectFixed<T extends Var> implements SelectChoicePoint<T> {

  private final T[] searchVariables;

  private final Indomain<T> valueOrdering;

  private final Map<T, Integer> position;

  private final Random random;

  private int currentIndex;

  @SuppressWarnings("unchecked")
  public RandomSelectFixed(T[] variables, Indomain<T> indomain, int seed) {
    position = Var.createEmptyPositioning();
    int unique = 0;
    for (int i = 0; i < variables.length; i++) {
      if (position.get(variables[i]) == null) {
        position.put(variables[i], unique++);
      }
    }
    searchVariables = (T[]) new Var[position.size()];
    for (Iterator<Entry<T, Integer>> itr = position.entrySet().iterator(); itr.hasNext(); ) {
      Entry<T, Integer> e = itr.next();
      searchVariables[e.getValue()] = e.getKey();
    }
    valueOrdering = indomain;
    random = new Random(seed);
  }

  @Override
  public T getChoiceVariable(int index) {
    assert index < searchVariables.length;
    int finalIndex = searchVariables.length;
    T currentVariable;
    do {
      int size = finalIndex - index;
      int selectedIndex = index + random.nextInt(size);
      currentVariable = placeSearchVariable(index, selectedIndex);
    } while (currentVariable.singleton() && ++index < finalIndex);
    if (index == finalIndex) {
      return null;
    } else {
      currentIndex = index;
      return currentVariable;
    }
  }

  @Override
  public int getChoiceValue() {
    assert currentIndex >= 0;
    assert currentIndex < searchVariables.length;
    assert searchVariables[currentIndex].dom() != null;
    return valueOrdering.indomain(searchVariables[currentIndex]);
  }

  @Override
  public PrimitiveConstraint getChoiceConstraint(int index) {
    return null;
  }

  @Override
  public Map<T, Integer> getVariablesMapping() {
    return position;
  }

  @Override
  public int getIndex() {
    return currentIndex;
  }

  private T placeSearchVariable(int searchPosition, int variablePosition) {
    if (searchPosition != variablePosition) {
      T temp = searchVariables[searchPosition];
      searchVariables[searchPosition] = searchVariables[variablePosition];
      searchVariables[variablePosition] = temp;
    }
    return searchVariables[searchPosition];
  }

  public String toString() {
    return String.valueOf(Arrays.asList(searchVariables));
  }
}
