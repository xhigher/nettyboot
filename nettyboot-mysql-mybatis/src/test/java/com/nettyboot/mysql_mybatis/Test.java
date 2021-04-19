package com.nettyboot.mysql_mybatis;

import com.nettyboot.mysql_mybatis.beans.StudentInfo;
import com.nettyboot.mysql_mybatis.entrys.StudentInfoMapper;
import com.nettyboot.mysql_mybatis.xhs.StudentInfoModel;
import com.nettyboot.util.FileUtil;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import tk.mybatis.mapper.entity.Example;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
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

        XMySQL.init(properties);

        StudentInfoModel studentInfoModel = new StudentInfoModel();

//        XContext context = new XContext();
//        context.startTransaction();
//        studentInfoModel.setTransaction(context);

        StudentInfoMapper mapper = studentInfoModel.getMapper();
//        Example example = new Example(StudentInfo.class);
//        Example.Criteria criteria = example.createCriteria();
        List<StudentInfo> studentInfos = mapper.selectAll();
        System.out.println(studentInfos);

        studentInfoModel.closeSqlSession(studentInfoModel.getSession());

//        studentInfoModel.closeSqlSession();


//        StudentInfo studentInfo1 = studentInfoModel.selectById(1);
//        System.out.println(studentInfo1);

//        boolean b = studentInfoModel.updateById(1, "123456789");
//        System.out.println("---------" + b);

//        context.endTransaction(true);

    }
}
