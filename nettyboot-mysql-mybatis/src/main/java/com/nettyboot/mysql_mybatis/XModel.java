package com.nettyboot.mysql_mybatis;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/*
 * @copyright (c) xhigher 2015
 * @author xhigher    2015-3-26
 */
public abstract class XModel {

    protected static Logger logger = LoggerFactory.getLogger(XModel.class);

    private XContext mContext = null;

    protected abstract String getDataSourceName();

    // 判断是否开启事务
    private boolean isBadTransaction() {
        return (mContext == null || mContext.getTransaction() == null || mContext.getTransaction().isEnded());
    }

    // 配置开启事务
    public boolean setTransaction(XContext context) {
        mContext = context;
        if (mContext != null && mContext.getTransaction() != null) {
            if (mContext.getTransaction().getSqlSession() == null) {
                return mContext.getTransaction().setConnection(XMySQL.getSqlSession(getDataSourceName()));
            }
            return true;
        }
        return false;
    }

    // 获取 SqlSession
    public SqlSession getSqlSession() {
        if (!isBadTransaction()) {
            return mContext.getTransaction().getSqlSession();
        }
        return XMySQL.getSqlSession(getDataSourceName());
    }

    // 关闭 SqlSession
    public void closeSqlSession(SqlSession sqlSession) {
        if (isBadTransaction()) {
            XMySQL.releaseSqlSession(sqlSession);
        }
    }

    public <T> T getMapperHandlerProxy(Class<T> mapperClazz){
        // 2. 获取对应的 ClassLoader
        ClassLoader classLoader = mapperClazz.getClassLoader();
        // 3. 获取所有接口的Class，这里的UserServiceImpl只实现了一个接口UserService，
        Class[] interfaces = new Class[]{ mapperClazz };
        // 4. 创建一个将传给代理类的调用请求处理器，处理所有的代理对象上的方法调用
        //     这里创建的是一个自定义的日志处理器，须传入实际的执行对象 userServiceImpl
        InvocationHandler logHandler = new MapperHandler(mapperClazz);
        /*
		   5.根据上面提供的信息，创建代理对象 在这个过程中，
               a.JDK会通过根据传入的参数信息动态地在内存中创建和.class 文件等同的字节码
               b.然后根据相应的字节码转换成对应的class，
               c.然后调用newInstance()创建代理实例
		 */
        return  (T)Proxy.newProxyInstance(classLoader, interfaces, logHandler);
    }

    public class MapperHandler implements InvocationHandler {
        Class mapperClazz;

        public MapperHandler(Class mapperClazz) {
            this.mapperClazz = mapperClazz;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // 获取sqlSession
            SqlSession sqlSession = getSqlSession();
            try {
                // 获取mapper
                Object mapper = sqlSession.getMapper(mapperClazz);
                // 调用 mapper 的 method 方法
                Object result = method.invoke(mapper, args);
                // 如果是非事务，则提交
                if(isBadTransaction()){
                    sqlSession.commit();
                }
                // 返回结果
                return result;
            }catch (Exception e){
                logger.error("MapperHandler.invoke.Exception", e);
            }finally {
                // 关闭sqlSession
                if(sqlSession != null){
                    closeSqlSession(sqlSession);
                }
            }
            // 返回结果
            return null;
        }
    }


}

