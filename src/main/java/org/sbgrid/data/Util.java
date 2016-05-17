package org.sbgrid.data;

public class Util {
    public static void memory(){
        int mb = 1024*1024;
        Runtime runtime = Runtime.getRuntime();
        System.out.println(":----------:");
        System.out.println("Used Memory:" + (runtime.totalMemory() - runtime.freeMemory()) / mb);
        System.out.println("Free Memory:" + runtime.freeMemory() / mb);
        System.out.println("Total Memory:" + runtime.totalMemory() / mb);
        System.out.println("Max Memory:" + runtime.maxMemory() / mb);
        System.out.println(":----------:");
    }

}

