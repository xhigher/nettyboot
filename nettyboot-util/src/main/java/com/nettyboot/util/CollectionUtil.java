package com.nettyboot.util;

import java.util.Collection;

/**
 * The relevant operation of Collection
 *
 * @author : lmr2015
 * @version : v1.0
 * @Description :
 * @createTime : 2021/6/9 13:52
 * @updateTime : 2021/6/9 13:52
 */
public class CollectionUtil {

    public static <T> boolean isNullOrEmpty(Collection<T> value) {
        return (value == null || value.isEmpty());
    }

}
