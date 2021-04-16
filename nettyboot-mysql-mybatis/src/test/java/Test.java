import beans.StudentInfo;
import com.nettyboot.mysql.XContext;
import com.nettyboot.mysql.XMySQL;
import com.nettyboot.util.FileUtil;
import entrys.StudentInfoMapper;
import model.xhs.SchoolDataDatabase;

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

        SchoolDataDatabase schoolDataDatabase = new SchoolDataDatabase();
//        schoolDataDatabase.setTransaction(new XContext());

        StudentInfoMapper studentInfoMapper = schoolDataDatabase.getStudentInfoMapper();
        StudentInfo studentInfo = studentInfoMapper.selectById(1);
        System.out.println(studentInfo.toString());

    }
}
