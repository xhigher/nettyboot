package com.nettyboot.admin.logic.devtool;

import com.alibaba.fastjson.JSONObject;
import com.nettyboot.admin.base.AdminLogic;
import com.nettyboot.admin.conf.AdminDataKey;
import com.nettyboot.admin.conf.BaseModules;
import com.nettyboot.admin.model.admin.TraceLogModel;
import com.nettyboot.config.BaseDataKey;
import com.nettyboot.config.LogicAnnotation;

@LogicAnnotation(module= BaseModules.devtool, action="log_list", parameters = {
        AdminDataKey.staffid,
        BaseDataKey.pagenum,
        BaseDataKey.pagesize

})
public final class LogList extends AdminLogic {
    private Integer staffid = null;
    private String commoand = null;
    private String event = null;
    private String startime = null;
    private String endtime = null;
    private Integer pagenum = null;
    private Integer pagesize = null;

    @Override
    protected String prepare() {
        staffid = getIntegerParameter(AdminDataKey.staffid);
        if (staffid == null || staffid <0){
            return errorParameterResult("STAFFID_ERROR");
        }

        commoand = getStringParameter(AdminDataKey.command);

        event = getStringParameter(AdminDataKey.event);

        startime = getStringParameter(AdminDataKey.starttime);

        endtime = getStringParameter(AdminDataKey.endtime);

        pagenum = this.getIntegerParameter(BaseDataKey.pagenum);
        if(pagenum == null || pagenum < 1) {
            return errorParameterResult("PAGENUM_ERROR");
        }
        pagesize = this.getIntegerParameter(BaseDataKey.pagesize);
        if(pagesize == null || pagesize < 10 || pagesize > 100) {
            return errorParameterResult("PAGESIZE_ERROR");
        }
        return null;
    }

    @Override
    protected String execute() {
        TraceLogModel logModel = new TraceLogModel();
        JSONObject pageData = logModel.getPageData(staffid, commoand, event, startime, endtime, pagenum, pagesize);
        if(pageData == null){
            return errorInternalResult();
        }

        return this.successResult(pageData);
    }
}
