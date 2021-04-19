package com.nettyboot.mysql_mybatis.entrys;

//import com.nettyboot.mysql_mybatis.beans.StudentInfo;
import com.nettyboot.mysql_mybatis.beans.StudentInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import javax.persistence.Table;

//@Table(name = "student_info")
public interface StudentInfoMapper extends Mapper<StudentInfo> {
    public StudentInfo selectById123(int id);

    public boolean updateById(@Param("id") int id, @Param("memo") String memo);
}
