package com.nettyboot.admin.model.business;

import com.nettyboot.mysql.XModel;

public abstract class BusinessDatabase extends XModel {

    @Override
    protected String getDataSourceName() {
        return "business";
    }
}
