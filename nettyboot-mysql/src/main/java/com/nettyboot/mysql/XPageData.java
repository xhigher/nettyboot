package com.nettyboot.mysql;

import java.util.List;

/**
 * 返回分页信息
 *
 * @author : lmr2015
 * @version : v1.0
 * @createTime : 2021/5/31 17:06
 */
public class XPageData<T> {

    int total;
    int pagenum;
    int pagesize;
    List<T> data;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPagenum() {
        return pagenum;
    }

    public void setPagenum(int pagenum) {
        this.pagenum = pagenum;
    }

    public int getPagesize() {
        return pagesize;
    }

    public void setPagesize(int pagesize) {
        this.pagesize = pagesize;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "XPageData{" +
                "total=" + total +
                ", pagenum=" + pagenum +
                ", pagesize=" + pagesize +
                ", data=" + data +
                '}';
    }
}
