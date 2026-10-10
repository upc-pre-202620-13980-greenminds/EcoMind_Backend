package pe.greenminds.ecomind.bdd;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

/**
 * Runs every .feature file under src/test/resources/features with the Cucumber engine.
 * Step definitions live in this package and its subpackages.
 */
@Suite(failIfNoTests = false)
@IncludeEngines("cucumber")
@SelectPackages("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "pe.greenminds.ecomind.bdd")
@ConfigurationParameter(key = "cucumber.plugin", value = "pretty,html:target/cucumber/report.html,json:target/cucumber/report.json,junit:target/cucumber/report.xml")
public class RunCucumberTest {
}
