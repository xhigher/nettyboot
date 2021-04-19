package com.nettyboot.mysql_mybatis.beans;

import lombok.Data;

import javax.persistence.Table;

@Table(name = "student_info")
@Data
public class StudentInfo {


    Integer student_id;

    String student_name;

}

