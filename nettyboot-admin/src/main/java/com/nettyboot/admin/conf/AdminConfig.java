package com.nettyboot.admin.conf;

import com.nettyboot.util.StringUtil;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AdminConfig {

    public static String BASE_URL = "";

    public static final int SUPERMAN_STAFFID = 1;

    public static final int ROLEID_SUPERMAN = 1;
    public static final int ROLEID_USER = 2;
    public static final int[] ROLE_BASE_LIST = {
            ROLEID_SUPERMAN,
            ROLEID_USER
    };

    public static final int DUTYID_DEV = 1;
    public static final int DUTYID_ICENTER = 2;
    public static final int DUTYID_RBAC = 3;
    public static final int DUTYID_BRANCH = 4;
    public static final int DUTYID_STAFF = 5;

    public static final int[] DUTY_BASE_LIST = {
            DUTYID_DEV,DUTYID_ICENTER,DUTYID_RBAC,DUTYID_BRANCH,DUTYID_STAFF
    };

    public static void init(Properties properties){
        BASE_URL = properties.getProperty("service.baseUrl", BASE_URL);

    }

    public static String getCmdid(String module, String action){
        return StringUtil.md5(module+"_"+action);
    }


    public static String createSessionid(){
        return StringUtil.randomString(6, true) + Long.toString(System.currentTimeMillis(), 36) + StringUtil.randomString(10, true);
    }

    public static boolean checkUsername(String username){
        Pattern pattern = Pattern.compile("^(1[0-9]{10})$");
        Matcher matcher = pattern.matcher(username);
        return matcher.matches();
    }

}
