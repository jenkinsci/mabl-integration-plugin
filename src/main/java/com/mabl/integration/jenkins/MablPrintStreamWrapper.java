package com.mabl.integration.jenkins;

import java.io.PrintStream;

/**
 * Wrapper for consistent output of mabl lines
 *
 */
class MablPrintStreamWrapper {

    private static final String MABL_SLUG = String.format("[%s] ", MablStepConstants.PLUGIN_SYMBOL);

    public final PrintStream stream;

    public MablPrintStreamWrapper(PrintStream stream) {
        this.stream = stream;
    }

    public void printf(
            final String format,
            final Object ... args
    ) {
        final String output = String.format(format, args).replaceAll("\n(?=.)", "\n"+MABL_SLUG);
        stream.print(MABL_SLUG + output);
    }

    public void println(final String x) {
        stream.print(MABL_SLUG + x + "\n");
    }
}