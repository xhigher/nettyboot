package com.nettyboot.upload;

import com.nettyboot.util.StringUtil;
import com.nettyboot.util.TimeUtil;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.util.CharsetUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UploadConfig {

    public static String FILEPATH = "filepath";
    public static String FILENAME = "filename";
    public static String FILESIZE = "filesize";
    public static String FILETYPE = "filetype";
    public static String FILEEXT = "fileext";
    public static String FIELDNAME = "fieldname";

    public enum UploadFileType {
        image, zip, document,
    }

    private static String PATH_PROTECTED = null;
    private static String PATH_PUBLIC = null;

    private static UploadFileType FILE_TYPE;

    private static int FILE_MAX_SIZE = 2097152;

    private static Pattern[] PROTECTED_PATTERN_LIST;

    private static Pattern[] PUBLIC_PATTERN_LIST;

    private static final Pattern requestPathPattern = Pattern.compile("^\\/v(\\d+)\\/([a-zA-Z]\\w+)\\/([a-zA-Z]\\w+)\\/?$");

    private static String accessControlAllowHeaders = null;
    private static String accessControlAllowMethods = null;
    private static Pattern accessControlAllowOrigin = null;
    private static String responseContentType = null;

    private static final Map<String, String> headerNameKeys = new HashMap<>();

    protected static void init(Properties properties){
        accessControlAllowHeaders = properties.getProperty("server.accessControlAllowHeaders", "").trim();
        accessControlAllowMethods = properties.getProperty("server.accessControlAllowMethods", "").trim().toUpperCase();
        String allowOrigin = properties.getProperty("server.accessControlAllowOrigin", "").trim();
        if(!allowOrigin.isEmpty()){
            accessControlAllowOrigin = Pattern.compile("^http(s?):\\/\\/("+allowOrigin+")(.*?)$");
        }
        responseContentType = properties.getProperty("server.responseContentType", "application/json").trim();

        int headerCount = Integer.parseInt(properties.getProperty("server.headerCount", "0").trim());
        for(int i=1; i<=headerCount; i++){
            headerNameKeys.put(properties.getProperty("server.headerName"+i).trim(), properties.getProperty("server.headerParamKey").trim());
        }

        FILE_MAX_SIZE = Integer.parseInt(properties.getProperty("server.fileMaxSize", "2097152").trim());

        FILE_TYPE = UploadFileType.valueOf(properties.getProperty("server.uploadType", "image").trim());
        String uploadPath = properties.getProperty("server.uploadPath", "/data/upload/").trim();
        PATH_PROTECTED = uploadPath + FILE_TYPE.name() + "/protected/";
        PATH_PUBLIC = uploadPath + FILE_TYPE.name() + "/public/";

        String protectedPatterns = properties.getProperty("server.protectedPatterns", "").trim();
        String[] protectedPatternArray = protectedPatterns.split(";");
        PROTECTED_PATTERN_LIST = new Pattern[protectedPatternArray.length];
        for (int i=0; i<protectedPatternArray.length; i++){
            PROTECTED_PATTERN_LIST[i] = Pattern.compile(protectedPatternArray[i]);
        }

        String publicPatterns = properties.getProperty("server.publicPatterns", "").trim();
        String[] publicPatternArray = publicPatterns.split(";");
        PUBLIC_PATTERN_LIST = new Pattern[protectedPatternArray.length];
        for (int i=0; i<publicPatternArray.length; i++){
            PUBLIC_PATTERN_LIST[i] = Pattern.compile(publicPatternArray[i]);
        }
    }

    protected static Map<String, String> checkHeaders(HttpHeaders header){
        Map<String, String> headerData = new HashMap<String, String>();
        for(String headerName : headerNameKeys.keySet()){
            if(header.contains(headerName)){
                headerData.put(headerNameKeys.get(headerName), header.get(headerName));
            }
        }
        return headerData;
    }

    protected static Matcher checkRequestPath(String path){
        return requestPathPattern.matcher(path);
    }

    protected static String getAccessControlAllowHeaders(){
        return accessControlAllowHeaders;
    }

    protected static String getAccessControlAllowMethods(){
        return accessControlAllowMethods;
    }

    protected static String getResponseContentType(){
        return responseContentType + "; charset=" + CharsetUtil.UTF_8.toString();
    }

    protected static boolean checkAllowOrigin(String origin){
        if(accessControlAllowOrigin == null){
            return true;
        }
        return accessControlAllowOrigin.matcher(origin).matches();
    }

    protected static boolean checkAllowMethods(HttpMethod method){
        if(accessControlAllowMethods == null || accessControlAllowMethods.isEmpty()){
            return true;
        }
        return (method != null && accessControlAllowMethods.contains(method.name().toUpperCase()));
    }

    protected static UploadFileType getFileType(){
        return FILE_TYPE;
    }

    protected static int getFileMaxSize(){
        return FILE_MAX_SIZE;
    }

    protected static String getUploadPath(boolean isProtected) {
        if(isProtected) {
            return PATH_PROTECTED;
        }
        return PATH_PUBLIC;
    }

    protected static Pattern[] getPublicPatternList(){
        return PUBLIC_PATTERN_LIST;
    }

    protected static Pattern[] getProtectedPatternList(){
        return PROTECTED_PATTERN_LIST;
    }

    protected static ResultFileInfo handleImageFromHttpData(FileUpload fileUpload, UploadRequestInfo uploadRequestInfo) throws Exception{
        String filetype = "";
        ResultFileInfo resultFileInfo = null;
        String contentType = fileUpload.getContentType();
        if (contentType.startsWith("image")) {
            String filename = fileUpload.getFilename();
            String newFilename = TimeUtil.getCurrentYMDHMSS2() + StringUtil.randomNumbers(5);
            String fileext = "";
            if (filename.lastIndexOf(".") != -1 && filename.lastIndexOf(".") != 0) {
                fileext = filename.substring(filename.lastIndexOf(".") + 1);
                if (fileext.length() < 5) {
                    newFilename = newFilename + "." + fileext;
                }
            }
            long filesize = fileUpload.length();
            if (filesize > 0 && filesize < UploadConfig.getFileMaxSize()) {
                String saveDir = uploadRequestInfo.getSaveDir();
                if (saveDir != null) {
                    String filepath = saveDir + newFilename;
                    if (fileUpload.renameTo(new File(uploadRequestInfo.getSavePath() + filepath))) {
                        resultFileInfo = new ResultFileInfo();
                        resultFileInfo.setFieldname(fileUpload.getName());
                        resultFileInfo.setFilename(filename);
                        resultFileInfo.setFilepath(filepath);
                        resultFileInfo.setFileext(fileext);
                        resultFileInfo.setFiletype(filetype);
                        resultFileInfo.setFilesize(filesize);
                    }
                }
            }
        }
        return resultFileInfo;
    }
}
