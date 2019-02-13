package com.example.work_order.scheduler;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class Scheduler {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${http.url}")
    private String url;

    @Value("${http.cookies}")
    private String cookiesStr;

    //每隔2秒执行一次
//    @Scheduled(fixedRate = 2000)
//    public void testTask() {
//        System.out.println("执行了");
//        Date now = new Date();
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//        httpToWorkOrder(url, cookiesStr, dateFormat.format(now), getContent());
//    }

    //每天18：05执行
    @Scheduled(cron = "0 05 18 ? * *")
    public void testTasks() {
        System.out.println("每天18：05执行写工单");
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyyMMdd");
        if (isWorkDay(dateFormat2.format(now))) {
            httpToWorkOrder(url, cookiesStr, dateFormat.format(now), getContent());
            //TODO:添加其他用户
            System.out.println(dateFormat + " 18：05 执行了写工单");
        }
    }

    private void httpToWorkOrder(String url, String cookiesStr, String date, String content) {
        String[] split = cookiesStr.split("; ");
        List<String> cookies = Arrays.asList(split);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.put(HttpHeaders.COOKIE, cookies);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("id[1]", "1");
        params.add("dates[1]", date);
        params.add("consumed[1]", "10");
        params.add("left[1]", "10");
        params.add("work[1]", content);
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(params, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
        System.out.println(response.getBody());
    }

    public static boolean isWorkDay(String httpArg) {
        BufferedReader reader = null;
        String result = null;
        StringBuffer sbf = new StringBuffer();
        String httpUrl = "http://api.goseek.cn/Tools/holiday?date=" + httpArg;
        try {
            URL url = new URL(httpUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            InputStream is = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String strRead = null;
            while ((strRead = reader.readLine()) != null) {
                sbf.append(strRead);
                sbf.append("\r\n");
            }
            reader.close();
            result = sbf.toString();
            Map map = (Map) JSONObject.parse(result);//转为JSONObject对象
            if (0 == (int) map.get("data")) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private String getContent() {
        String[] content = new String[]{"功能需求协商", "功能完成进度增加10%", "功能测试", "功能修改bug", "功能修改"};
        Random random = new Random();
        int index = random.nextInt(content.length);
        return content[index];
    }

}
