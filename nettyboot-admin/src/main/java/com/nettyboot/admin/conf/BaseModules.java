package com.nettyboot.admin.conf;

import java.util.Arrays;

public final class BaseModules {
    public static final String common = "common";
    public static final String branch = "branch";
    public static final String staff = "staff";
    public static final String rbac = "rbac";
    public static final String icenter = "icenter";
    public static final String devtool = "devtool";

    public static final String[] internal_modules = { branch, staff, rbac, icenter, devtool, common };

    public static boolean isBaseModule(String module){
        return Arrays.binarySearch(internal_modules, module) > -1;
    }
}
