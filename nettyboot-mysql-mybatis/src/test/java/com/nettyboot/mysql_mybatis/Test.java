package com.nettyboot.mysql_mybatis;

import com.nettyboot.mysql_mybatis.beans.StudentInfo;
import com.nettyboot.mysql_mybatis.entrys.StudentInfoMapper;
import com.nettyboot.mysql_mybatis.xhs.SchoolDataDatabase;
import com.nettyboot.util.FileUtil;

import java.io.IOException;
import java.util.Properties;

/**
 * add mybatis
 *
 * @author : [Administrator]
 * @version : [v1.0]
 * @createTime : [2021/4/16 10:20]
 */
public class Test {
    private static final String PROPERTIES_FILEPATH = "/application.properties";

    public static void main(String[] args) throws IOException {
        Properties properties = FileUtil.getProperties(PROPERTIES_FILEPATH);

        XMybatisMySQL.init(properties);
//
//        StudentInfoModel studentInfoModel = new StudentInfoModel();
////
////        XContext context = new XContext();
////        context.startTransaction();
////        studentInfoModel.setTransaction(context);
////

        SchoolDataDatabase schoolDataDatabase = new SchoolDataDatabase();
        StudentInfoMapper mapper = schoolDataDatabase.getMapperHandlerProxy(StudentInfoMapper.class);

        mapper.selectById123(1);
        mapper.updateById(1, "223");
//
//
//        List<StudentInfo> studentInfos = mapper.selectAll();
//        System.out.println(studentInfos);
//
//        studentInfoModel.closeSqlSession(studentInfoModel.getSession());

//        studentInfoModel.closeSqlSession();


//        StudentInfo studentInfo1 = studentInfoModel.selectById(1);
//        System.out.println(studentInfo1);

//        boolean b = studentInfoModel.updateById(1, "123456789");
//        System.out.println("---------" + b);

//        context.endTransaction(true);

    }
}
