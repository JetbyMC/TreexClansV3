package org.jetby.clans.common.tools;

public class Speedometer {

    private static long MS = System.currentTimeMillis();

    public static void start() {
        MS = System.currentTimeMillis();
    }
    public static long result() {
        return System.currentTimeMillis() - MS;
    }
}
