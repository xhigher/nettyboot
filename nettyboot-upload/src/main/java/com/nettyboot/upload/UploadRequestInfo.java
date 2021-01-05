package com.nettyboot.upload;

import com.nettyboot.util.TimeUtil;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.util.CharsetUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UploadRequestInfo {

    private String module;
    private String catalog;
    private boolean isProtected;
    private String savePath;

    public UploadRequestInfo(String path){
        Matcher matcher = null;
        Pattern[] patterns = UploadConfig.getProtectedPatternList();
        for (int i = 0; i < patterns.length; i++) {
            matcher = patterns[i].matcher(path);
            if (matcher.matches()) {
                isProtected = true;
                module = matcher.group(1);
                catalog = matcher.group(2);
                break;
            }
        }

        if (module == null) {
            patterns = UploadConfig.getPublicPatternList();
            for (int i = 0; i < patterns.length; i++) {
                matcher = patterns[i].matcher(path);
                if (matcher.matches()) {
                    module = matcher.group(1);
                    catalog = matcher.group(2);
                    break;
                }
            }
        }

        savePath = UploadConfig.getUploadPath(isProtected);

    }

    public String module(){
        return this.module;
    }

    public String catalog(){
        return catalog;
    }

    public boolean isProtected() {
        return isProtected;
    }

    public String getSavePath() {
        return savePath;
    }

    public String getSaveDir() {
        String saveDir = module + "/" + catalog + "/" + TimeUtil.getCurrentYM() + "/";
        File file = new File(savePath + saveDir);
        try {
            if (!file.isDirectory()) {
                if (!file.mkdirs()) {
                    return null;
                }
            }
        } catch (Exception e) {
            return null;
        }
        return saveDir;
    }

}
