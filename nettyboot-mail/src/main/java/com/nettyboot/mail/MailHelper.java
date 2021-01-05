package com.nettyboot.mail;

import com.alibaba.fastjson.JSONObject;
import com.sun.mail.util.MailSSLSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Date;
import java.util.Properties;

public class MailHelper {
	
	private static Logger logger = LoggerFactory.getLogger(MailHelper.class);
	
	private static String HOST = null;
	private static String FROM = null;
	private static String USER = null;
	private static String PWD = null;
	
	public static void init(Properties properties){
		HOST = properties.getProperty("mail.host");
		FROM = properties.getProperty("mail.from");
		USER = properties.getProperty("mail.user");
		PWD = properties.getProperty("mail.pwd");
	}
	
	public static void sendMail(JSONObject messageInfo) {
		try {
			String email = messageInfo.getString("email");
			String context = messageInfo.getString("context");
			String subject = messageInfo.getString("subject"); // 邮件标题
			
		    String TO = email; // 收件人地址
		    Properties props = new Properties();
	        props.put("mail.smtp.host", HOST);//设置发送邮件的邮件服务器的属性（这里使用网易的smtp服务器）
	        props.put("mail.smtp.auth", "true");  //需要经过授权，也就是有户名和密码的校验，这样才能通过验证（一定要有这一条）
	        
	      //-------当需使用SSL验证时添加，邮箱不需SSL验证时删除即可（测试SSL验证使用QQ企业邮箱）
	        String SSL_FACTORY="javax.net.ssl.SSLSocketFactory"; 
	        props.put("mail.smtp.socketFactory.class", SSL_FACTORY);
	        props.put("mail.smtp.socketFactory.fallback", "false");
	        props.put("mail.smtp.socketFactory.port", "465");
	        props.put("mail.smtp.port", "465"); //google使用465或587端口
	        MailSSLSocketFactory sf = new MailSSLSocketFactory();
			sf.setTrustAllHosts(true);
		    props.put("mail.smtp.ssl.socketFactory", sf);
	        
	        Session session = Session.getDefaultInstance(props);//用props对象构建一个session
	        session.setDebug(true);
	        MimeMessage message = new MimeMessage(session);//用session为参数定义消息对象
	        
	        message.setFrom(new InternetAddress(FROM));// 加载发件人地址
	        
	        Address sendTo = new InternetAddress(TO);
	        
	        message.addRecipient(Message.RecipientType.TO,sendTo);
	        message.setSentDate(new Date()); // 设置邮件消息发送的时间
//	      message.addRecipients(MimeMessage.RecipientType.CC, InternetAddress.parse(FROM));//设置在发送给收信人之前给自己（发送方）抄送一份，不然会被当成垃圾邮件，报554错
	        message.setSubject(subject);//加载标题
	        Multipart multipart = new MimeMultipart();//向multipart对象中添加邮件的各个部分内容，包括文本内容和附件
	        BodyPart contentPart = new MimeBodyPart();//设置邮件的文本内容
	        contentPart.setText(context);
	        
	        multipart.addBodyPart(contentPart);
	        message.setContent(multipart);//将multipart对象放到message中
	        message.saveChanges(); //保存邮件
	        Transport transport = session.getTransport("smtp");//发送邮件
	        transport.connect(HOST, USER, PWD);//连接服务器的邮箱
	        transport.sendMessage(message, message.getAllRecipients());//把邮件发送出去
	        transport.close();//关闭连接
	    	
	    }catch(Exception e) {
	    	logger.error("task.exception:", e);
	    }
	    
	}
}
