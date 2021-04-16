package entrys;

import beans.StudentInfo;
import org.apache.ibatis.annotations.Param;

public interface StudentInfoMapper {
    public StudentInfo selectById(int id);
    
    public boolean updateById(@Param("id") int id, @Param("memo") String memo);
}
