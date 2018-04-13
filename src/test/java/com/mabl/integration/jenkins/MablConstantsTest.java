package com.mabl.integration.jenkins;

import org.junit.Test;

import static com.mabl.integration.jenkins.MablStepConstants.PLUGIN_VERSION_UNKNOWN;
import static com.mabl.integration.jenkins.MablStepConstants.getPluginVersion;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit test runner
 */
public class MablConstantsTest {

    @Test
    // Note: Doesn't run in junit because manifest injected at packaging")
    public void ensurePluginVersionUnavailable() {
        final String pluginVersion = getPluginVersion();
        assertNotNull("plugin version unavailable", pluginVersion);
        assertEquals("plugin version unavailable", pluginVersion, PLUGIN_VERSION_UNKNOWN);
    }
}
