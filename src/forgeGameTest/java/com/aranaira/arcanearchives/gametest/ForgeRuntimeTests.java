package com.aranaira.arcanearchives.gametest;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherConfig;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.reporting.legacy.xml.LegacyXmlReportGeneratingListener;

/** Runs the existing JUnit assertions after Forge transformation, mod registration and world startup. */
@GameTestHolder("arcanearchives_test")
@PrefixGameTestTemplate(false)
public final class ForgeRuntimeTests {
    @GameTest(template = "empty", timeoutTicks = 1200)
    public static void sharedAssertions(GameTestHelper helper) {
        var roots = Arrays.stream(System.getProperty("arcanearchives.test.classes").split(File.pathSeparator))
            .map(Path::of).collect(Collectors.toSet());
        var request = LauncherDiscoveryRequestBuilder.request()
            .selectors(DiscoverySelectors.selectClasspathRoots(roots))
            .configurationParameter("junit.jupiter.execution.parallel.enabled", "false")
            .build();
        var summary = new SummaryGeneratingListener();
        var output = new PrintWriter(System.out, true);
        var reports = new LegacyXmlReportGeneratingListener(Path.of(System.getProperty("arcanearchives.test.reports")), output);
        var config = LauncherConfig.builder().enableTestEngineAutoRegistration(false)
            .addTestEngines(new JupiterTestEngine()).build();
        Thread thread = Thread.currentThread();
        ClassLoader previous = thread.getContextClassLoader();
        try {
            thread.setContextClassLoader(ForgeRuntimeTests.class.getClassLoader());
            LauncherFactory.create(config).execute(request, summary, reports);
        } finally {
            thread.setContextClassLoader(previous);
        }
        var result = summary.getSummary();
        result.printTo(output);
        result.printFailuresTo(output);
        if (result.getTestsFoundCount() == 0 || result.getTotalFailureCount() != 0
                || result.getTestsSucceededCount() != result.getTestsFoundCount()) {
            helper.fail("Shared Forge assertions failed or were not executed; inspect forge-runtime XML reports");
            return;
        }
        helper.succeed();
    }
}
