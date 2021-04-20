package com.nettyboot.mysql_mybatis.entrys;

import com.nettyboot.mysql_mybatis.beans.StudentInfo;
import org.apache.ibatis.annotations.Param;

public interface StudentInfoMapper{
    public StudentInfo selectById123(int id);

    public boolean updateById(@Param("id") int id, @Param("memo") String memo);
}
