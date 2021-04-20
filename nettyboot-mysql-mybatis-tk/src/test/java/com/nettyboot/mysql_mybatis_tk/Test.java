package com.nettyboot.mysql_mybatis_tk;

import com.nettyboot.mysql_mybatis.XMybatisMySQL;
import com.nettyboot.mysql_mybatis_tk.beans.StudentInfo;
import com.nettyboot.mysql_mybatis_tk.entrys.StudentInfoMapper;
import com.nettyboot.mysql_mybatis_tk.xhs.SchoolDataDatabase;
import com.nettyboot.util.FileUtil;
import tk.mybatis.mapper.entity.Example;

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

        XTkMybatisMySQL.init(properties);
//
//        StudentInfoModel studentInfoModel = new StudentInfoModel();
////
////        XContext context = new XContext();
////        context.startTransaction();
////        studentInfoModel.setTransaction(context);
////

        SchoolDataDatabase schoolDataDatabase = new SchoolDataDatabase();
        StudentInfoMapper mapper = schoolDataDatabase.getMapperHandlerProxy(StudentInfoMapper.class);
        Example example = new Example(StudentInfo.class);
        Example.Criteria criteria = example.createCriteria();
        mapper.selectByExample(example);

        mapper.selectAll();

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
