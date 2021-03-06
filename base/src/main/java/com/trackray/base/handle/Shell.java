package com.trackray.base.handle;

import com.trackray.base.bean.Constant;
import com.trackray.base.utils.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

/**
 * 系统命令类
 * @author 浅蓝
 * @email blue@ixsec.org
 * @since 2019/1/8 12:28
 */
public class Shell {

    private static final String[] LINUX_PREFIX = {"/bin/bash","-c"};
    private static final String[] WIN_PREFIX = {"cmd","/c"};

    private Runtime runtime = Runtime.getRuntime();
    private Process process;
    private boolean block = false;
    private String target = "";
    private boolean enable = false;
    public Shell(){
    }
    public Shell(boolean enable){
        this.enable = enable;
    }
    public Shell block(boolean b){
        this.block = b;
        return this;
    }
    public Shell target(String t){
        target = t;
        return this;
    }
    public void exec(String... c) throws IOException {
        Properties props=System.getProperties();
        String os = props.getProperty("os.name");
        ArrayList<String> base = new ArrayList<>();
        base.addAll(Arrays.asList((os.contains("indows") ? WIN_PREFIX : LINUX_PREFIX)));
        base.add(target);
        if (c!=null&&c.length>0)
            base.addAll(Arrays.asList(c));
        String path = System.getenv().get("Path");
        String[] bases = base.toArray(new String[]{});
        process = runtime.exec(bases,new String[]{"Path="+path});
    }

    public void echo(String s){
        OutputStream stdin = process.getOutputStream();
        PrintWriter pw = new PrintWriter(stdin,true);
        pw.println(s);
        pw.close();
    }

    public String readLine(){
        BufferedReader reader = IOUtils.streamToReader(getProcess().getInputStream());
        try {
            return reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public String content(){
        if (block){
            try {
                process.waitFor();
                return IOUtils.analysisProcess(process);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            try {
                return IOUtils.analysisProcess(process);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    public String readError(){
        if (block) {
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                return "";
            }
        }
        StringBuffer str = new StringBuffer();
        InputStream errorStream = getProcess().getErrorStream();

        try {
            String err = IOUtils.analysisStream(errorStream);
            str.append(err);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return str.toString();
    }
    public String readAll(){
        if (block) {
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                return "";
            }
        }
        StringBuffer str = new StringBuffer();
        InputStream inputStream = getProcess().getInputStream();
        InputStream errorStream = getProcess().getErrorStream();

        try {
            String in = IOUtils.analysisStream(inputStream);
            String err = IOUtils.analysisStream(errorStream);
            str.append(in);
            str.append(Constant.LINE);
            str.append(err);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return str.toString();
    }
    public Runtime getRuntime() {
        return runtime;
    }

    public Process getProcess() {
        return process;
    }
}

