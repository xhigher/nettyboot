package com.nettyboot.admin.model.admin;

import com.nettyboot.mysql.XModel;

public abstract class AdminDatabase extends XModel {

    @Override
    protected String getDataSourceName() {
        return "admin";
    }
}
