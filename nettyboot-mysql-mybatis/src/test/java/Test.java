import beans.StudentInfo;
import com.nettyboot.mysql.XContext;
import com.nettyboot.mysql.XMySQL;
import com.nettyboot.util.FileUtil;
import entrys.StudentInfoMapper;
import model.xhs.SchoolDataDatabase;
import model.xhs.StudentInfoModel;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
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

    public static void main(String[] args) {
        Properties properties = FileUtil.getProperties(PROPERTIES_FILEPATH);

        XMySQL.init(properties);

        StudentInfoModel studentInfoModel = new StudentInfoModel();

        XContext context = new XContext();
        context.startTransaction();

        studentInfoModel.setTransaction(context);
        StudentInfo studentInfo1 = studentInfoModel.selectById(1);
        System.out.println(studentInfo1);

        boolean b = studentInfoModel.updateById(1, "123456789");
        System.out.println("---------" + b);

        context.endTransaction(true);


    }
}
