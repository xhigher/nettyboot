package com.nettyboot.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nettyboot.config.TaskAnnotation;
import com.nettyboot.config.TaskType;

import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FileUtil {

	public static Properties getProperties(String filepath){
		Properties properties = null;
		try{
			properties = new Properties();
			InputStream is = Object.class.getResourceAsStream(filepath);
			properties.load(is);
			if (is != null) {
				is.close();
			}
		}catch(Exception e){
			properties = null;
			e.printStackTrace();
		}
		return properties;
	}
	
	public static String readResourceFile(String fileName) {
		InputStreamReader inputReader = null;
		BufferedReader bufReader = null;
		StringBuffer resultBuf = new StringBuffer(1024);
		try {
			String lineStr = "";
			inputReader = new InputStreamReader(Object.class.getResourceAsStream(fileName)); 
			bufReader = new BufferedReader(inputReader);
			while((lineStr = bufReader.readLine()) != null){
				resultBuf.append(lineStr);
			}
		} catch (Exception e) {
				e.printStackTrace(); 
		}finally{
			try {
				if(inputReader != null){
					inputReader.close();
				}
				if(bufReader != null){
					bufReader.close();
				}	
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return resultBuf.toString();
	}
	
	public static String readFile(String fileName) {
		FileReader fReader = null;
		BufferedReader bufReader = null;
		StringBuffer resultBuf = new StringBuffer(1024);
		try {
			 File file =new File(fileName);
			 if (!file.exists()) {
				 return null;
			 }
			 String lineStr = "";
			 fReader = new FileReader(file.getAbsoluteFile());
			 bufReader = new BufferedReader(fReader);
			 while((lineStr = bufReader.readLine()) != null){
				 resultBuf.append(lineStr);
			 }
		} catch (Exception e) {
			e.printStackTrace(); 
		}finally{
			try {
				if(fReader != null){
					fReader.close();
				}
				if(bufReader != null){
					bufReader.close();
				}	
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return resultBuf.toString();
	}
	
	public static void writeFile(String fileName, JSONObject data) {
		FileWriter fWriter = null;
		BufferedWriter buffWriter = null;
		try {
			 File file =new File(fileName);
			 if (!file.exists()) {
				 file.createNewFile();
			 }
			 fWriter = new FileWriter(file.getAbsoluteFile());
			 buffWriter = new BufferedWriter(fWriter);
			 buffWriter.write(data.toJSONString());
		} catch (Exception e) {
			e.printStackTrace(); 
		}finally{
			try {
				if(fWriter != null){
					fWriter.close();
				}
				if(buffWriter != null){
					buffWriter.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void writeFile(String fileName, JSONArray data) {
		FileWriter fWriter = null;
		BufferedWriter buffWriter = null;
		try {
			 File file =new File(fileName);
			 if (!file.exists()) {
				 file.createNewFile();
			 }
			 fWriter = new FileWriter(file.getAbsoluteFile());
			 buffWriter = new BufferedWriter(fWriter);
			 buffWriter.write(data.toJSONString());
		} catch (Exception e) {
			e.printStackTrace(); 
		}finally{
			try {
				if(fWriter != null){
					fWriter.close();
				}
				if(buffWriter != null){
					buffWriter.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static JSONObject getJSONObject(String filename) {
		JSONObject result = null;
		String resultStr = readFile(filename);
		if(resultStr != null){
			result = JSONObject.parseObject(resultStr);
		}
		return result;
	}
	
	public static JSONArray getJSONArray(String filename) {
		JSONArray result = null;
		String resultStr = readFile(filename);
		if(resultStr != null){
			result = JSONArray.parseArray(resultStr);
		}
		return result;
	}

	private static void dfs(File[] files, ZipOutputStream zos, String fpath) throws IOException {
		byte[] buf = new byte[1024];
		for (File child : files) {
			if (child.isFile()) {
				FileInputStream fis = new FileInputStream(child);
				BufferedInputStream bis = new BufferedInputStream(fis);
				zos.putNextEntry(new ZipEntry(fpath + child.getName()));
				int len;
				while((len = bis.read(buf)) > 0) {
					zos.write(buf, 0, len);
				}
				bis.close();
				zos.closeEntry();
				continue;
			}
			File[] fs = child.listFiles();
			String nfpath = fpath + child.getName() + "/";
			if (fs.length <= 0) {
				zos.putNextEntry(new ZipEntry(nfpath));
				zos.closeEntry();
			} else {
				dfs(fs, zos, nfpath);
			}
		}
	}

	public static final void zip(File[] files, String destpath) throws IOException {
		FileOutputStream fos = new FileOutputStream(destpath);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		ZipOutputStream zos = new ZipOutputStream(bos);
		dfs(files, zos, "");
		zos.flush();
		zos.close();
	}

	public static final boolean unzip(String zippath, String destpath) {
		return unzip(new File(zippath), destpath);
	}

	public static final boolean unzip(File zipfile, String destpath) {
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		ZipInputStream zis = null;
		ZipEntry zn = null;
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		try{
			byte[] buf = new byte[1024];
			fis = new FileInputStream(zipfile);
			bis = new BufferedInputStream(fis);
			zis = new ZipInputStream(bis);
			File tempFile = null;
			File parentFile = null;
			while ((zn = zis.getNextEntry()) != null) {
				tempFile = new File(destpath + "/" + zn.getName());
				if (zn.isDirectory()) {
					tempFile.mkdirs();
				} else {
					parentFile = tempFile.getParentFile();
					if (!parentFile.exists()) {
						parentFile.mkdirs();
					}

					fos = new FileOutputStream(tempFile);
					bos = new BufferedOutputStream(fos);
					int len;
					while ((len = zis.read(buf)) != -1) {
						bos.write(buf, 0, len);
					}
					bos.flush();
					bos.close();
				}
				zis.closeEntry();
			}
			return true;
		}catch(Exception e){
			return false;
		}finally{
			try {
				if(bos != null){
					bos.flush();
					bos.close();
				}
				if(fos != null){
					fos.close();
				}

				if(zis != null){
					zis.close();
				}
				if(bis != null){
					bis.close();
				}
				if(fis != null){
					fis.close();
				}
			} catch (IOException e) {
			}
		}
	}

	public static List<Class> getClassesFromPackage(String targtPackage){
		File file = null;
		List<String> classFiles = null;
		String path = targtPackage.replace('.', '/');
		List<Class> classList = new ArrayList<>();
		try{
			URL url = null;
			JarURLConnection jarConnection = null;
			JarFile jarFile = null;
			Enumeration<JarEntry> jarEntryEnumeration = null;
			String jarEntryName = null;
			String fullClazz = null;
			Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(path);
			while (urls.hasMoreElements()) {
				url = urls.nextElement();
				if ("jar".equalsIgnoreCase(url.getProtocol())) {
					jarConnection = (JarURLConnection) url.openConnection();
					if (jarConnection != null) {
						jarFile = jarConnection.getJarFile();
						if (jarFile != null) {
							jarEntryEnumeration = jarFile.entries();
							while (jarEntryEnumeration.hasMoreElements()) {
								jarEntryName = jarEntryEnumeration.nextElement().getName();
								if (jarEntryName.contains(".class") && jarEntryName.replace("/",".").startsWith(targtPackage)) {
									fullClazz = jarEntryName.substring(0, jarEntryName.lastIndexOf(".")).replace("/", ".");
									classList.add(Class.forName(fullClazz));
								}
							}
						}
					}
				}else{
					file = new File(url.toURI());
					if (file != null) {
						classFiles = new ArrayList<String>();
						listClassFiles(file, classFiles);
						for (String clz : classFiles) {
							fullClazz = clz.replaceAll("[/\\\\]", ".");
							fullClazz = fullClazz.substring(fullClazz.indexOf(targtPackage), clz.length() - 6);
							classList.add(Class.forName(fullClazz));
						}
					}
				}
			}
		}catch(Exception e) {
			classList = null;
		}
		return classList;
	}

	private static void listClassFiles(File file, List<String> classFiles){
		File tf = null;
		File[] files = file.listFiles();
		for(int i=0; i<files.length; i++){
			tf = files[i];
			if(tf.isDirectory()){
				listClassFiles(tf, classFiles);
			}else if(tf.isFile() && tf.getName().endsWith(".class")){
				classFiles.add(tf.getAbsolutePath());
			}
		}
	}

}
