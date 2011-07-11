package org.openengsb.itests.console;

import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSession;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.itests.util.AbstractExamTestHelper;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.Bundle;

import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.scanFeatures;

/**
 *
 */
@RunWith(JUnit4TestRunner.class)
public class BaseConsoleIT extends AbstractExamTestHelper {


    @Configuration
    public final Option[] configureConsole() {
        return options(scanFeatures(
            maven().groupId("org.openengsb").artifactId("openengsb").type("xml").classifier("features-itests")
                .versionAsInProject(), "apache-felix-commands"));
    }

    @Test
    public void testInstallCommand() throws Exception {

        CommandProcessor cp = getOsgiService(CommandProcessor.class);
        CommandSession cs = cp.createSession(System.in, System.out, System.err);

        Bundle b = getInstalledBundle("org.apache.karaf.shell.log");
        b.start();
        cs.execute("log:display");
        b.stop();
        cs.close();
    }


}
