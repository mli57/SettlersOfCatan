/**
 * Test suite for core game mechanics, which includes bank, player, node, and edge tests.
 * @author Sarthak Kulashari
 */
package UnitTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
    BankTest.class,
    PlayerTest.class,
    EdgeTest.class,
    NodeTest.class
})
public class CoreGameSuite {
}