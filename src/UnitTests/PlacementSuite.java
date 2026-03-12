
/**
 * Test suite for placement mechanics, which includes runs tests for placement rules, including nodes, edges, and validation.
 * @author Sarthak Kulashari
 */

package UnitTests;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
    NodeTest.class,
    EdgeTest.class,
    PlacementValidatorTest.class
})
/*
This suite runs tests for placement rules, including nodes, edges, and validation
*/
public class PlacementSuite {
}