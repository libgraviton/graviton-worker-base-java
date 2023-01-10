package com.github.libgraviton.workerbase.helper;

public class ClassScanBuildTime {
    public static void main(String[] args) throws Exception {
        try {
            DependencyInjection.cacheAllClassScanResults(args[0]);
            System.out.println("Wrote class scan cache to " + args[0]);
        } catch (Throwable t) {
            System.out.println("Could *not* write class scan cache to " + args[0] + " (" + t.getMessage() + ")");
        }
    }
}
