package com.nettyboot.tomcat;

import com.nettyboot.config.LogicAnnotation;
import com.nettyboot.config.LogicMethod;
import com.nettyboot.logic.BaseLogic;

@LogicAnnotation(module="test", action = "demo", method= LogicMethod.GET, parameters= {"id"})
public final class DemoLogic extends LogicServlet {

    @Override
    public BaseLogic getLogic() {

        return new BaseLogic() {

            @Override
            protected String prepare() {
                return null;
            }

            @Override
            protected String execute() {
                return null;
            }
        };


    }
}
