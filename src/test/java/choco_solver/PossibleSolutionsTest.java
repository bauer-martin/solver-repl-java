package choco_solver;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.List;

import spl_conqueror.VariabilityModel;
import utilities.XMLUtils;

@SuppressWarnings("JUnitTestMethodWithNoAssertions")
final class PossibleSolutionsTest {

  private static void countPossibleSolutions(String featureModel, int expectedSolutionsCount)
      throws DocumentException {
    URL url = PossibleSolutionsTest.class.getResource("feature-models/" + featureModel);
    Element rootElement = XMLUtils.loadXML(url);
    VariabilityModel vm = new VariabilityModel(rootElement);
    Model constraintSystem = ConstraintSystemContext.from(vm).getConstraintSystem();
    Solver solver = constraintSystem.getSolver();
    List<Solution> allSolutions = solver.findAllSolutions();
    Assertions.assertEquals(expectedSolutionsCount, allSolutions.size());
  }

  @Test
  void testApacheEnergy() throws DocumentException {
    countPossibleSolutions("Apache_energy.xml", 580);
  }

  @Test
  void testBerkeleyDBC() throws DocumentException {
    countPossibleSolutions("BerkeleyDBC.xml", 2560);
  }

  @Test
  void testBrotli() throws DocumentException {
    countPossibleSolutions("brotli.xml", 180);
  }

  @Test
  void testDuneBin() throws DocumentException {
    countPossibleSolutions("Dune_bin.xml", 2304);
  }

  @Test
  void testExastencilsEnergy() throws DocumentException {
    countPossibleSolutions("exastencils_energy.xml", 86058);
  }

  @Test
  void testHipaccBin() throws DocumentException {
    countPossibleSolutions("Hipacc_bin.xml", 13485);
  }

  @Test
  void testHSQLDBEnergy() throws DocumentException {
    countPossibleSolutions("HSQLDB_energy.xml", 864);
  }

  @Test
  void testLLVM() throws DocumentException {
    countPossibleSolutions("LLVM.xml", 1024);
  }

  @Test
  void testLLVMEnergy() throws DocumentException {
    countPossibleSolutions("LLVM_energy.xml", 65536);
  }
}
