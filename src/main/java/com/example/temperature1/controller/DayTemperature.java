package com.example.temperature1.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Random;

//초단기예보
@Controller
public class DayTemperature {

    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    Calendar c1 = Calendar.getInstance();
    String strToday = sdf.format(c1.getTime());
    private String nx = "60"; // x좌표
    private String ny = "126"; // y좌표
    private String baseDate = strToday; // 조회하고 싶은 날짜
    private LocalDateTime now = LocalDateTime.now(); // 현재 시각을 조회 시각에.
    int hour = now.getHour()-1;
    private String baseTime;
    private String fTime;
    private String type = "xml"; // 조회하고 싶은 type
    private String numOfRows = "250";

    @GetMapping("/ootd")
    public String getTemperature(Model model) {
        String apiUrl = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtFcst";
        String serviceKey ="7j%2Bj0bSrBD%2F9JbCHMUaNX3lm%2FxhH9nkQieSYF8IjvfZOBuK2f%2FgvJyiHeA27URwozmEk3U%2BSy%2BV6eUk9tba8UQ%3D%3D"; // 홈페이지에서 받은 키


        try {
            LocalDateTime now = LocalDateTime.now();
            int hour = now.getHour();
            if (hour < 12) {
                baseTime = String.format("%02d00", hour - 1); // 앞에 0을 붙여주기 위해 %02d 사용
            } else {
                baseTime = (hour - 1) + "00";
            }
            if (hour < 12) {
                fTime = String.format("%02d00", hour); // 앞에 0을 붙여주기 위해 %02d 사용
            } else {
                fTime = (hour) + "00";
            }
            StringBuilder urlBuilder = new StringBuilder(apiUrl);
            urlBuilder.append("?" + URLEncoder.encode("ServiceKey", "UTF-8") + "=" + serviceKey);
            urlBuilder.append("&" + URLEncoder.encode("nx", "UTF-8") + "=" + URLEncoder.encode(nx, "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("ny", "UTF-8") + "=" + URLEncoder.encode(ny, "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("base_date", "UTF-8") + "=" + URLEncoder.encode(baseDate, "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("base_time", "UTF-8") + "=" + URLEncoder.encode(baseTime, "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("_type", "UTF-8") + "=" + URLEncoder.encode(type, "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode(numOfRows, "UTF-8"));	/* 한 페이지 결과 수 */

            URL url = new URL(urlBuilder.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-type", "application/xml");
            System.out.println("Response code: " + conn.getResponseCode());

            BufferedReader rd;
            if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
                rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            } else {
                rd = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
            }

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            rd.close();
            conn.disconnect();
            String result = sb.toString();

            // XML 파싱
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(url.openStream());

            // XML에서 필요한 데이터 추출
            NodeList itemList = document.getElementsByTagName("item");
            StringBuilder weatherInfo = new StringBuilder();
            StringBuilder ootdInfo = new StringBuilder();
            StringBuilder skyInfo = new StringBuilder();

            double[] TemArray = new double[10];
            for (int i = 0; i < itemList.getLength(); i++) {
                Element item = (Element) itemList.item(i);
                Object category = item.getElementsByTagName("category").item(0).getTextContent();
                String fcstValue = item.getElementsByTagName("fcstValue").item(0).getTextContent();
                String fcstTime = item.getElementsByTagName("fcstTime").item(0).getTextContent();
                String baseDate = item.getElementsByTagName("baseDate").item(0).getTextContent();
                String baseTime = item.getElementsByTagName("baseTime").item(0).getTextContent();

                if(category.equals("SKY")&&fcstTime.equals(fTime)){
                    skyInfo.append("지금의 날씨는 ");
                    if(fcstValue.equals("1")) {
                        skyInfo.append("☀️\n");
                    }else if(fcstValue.equals("2")) {
                        skyInfo.append("☔\n");
                    }else if(fcstValue.equals("3")) {
                        skyInfo.append("🌤️\n");
                    }else if(fcstValue.equals("4")) {
                        skyInfo.append("️☁️\n");
                    }
                }
                if(category.equals("T1H")){
                    weatherInfo.append(fcstTime).append("시 : ").append(fcstValue).append("°\n");

                }

            }
            List<String> tops = Arrays.asList("프린팅 반팔", "니트 슬리브리스", "고슬고슬 니트", "반팔셔츠");
            List<String> outers = Arrays.asList("-", "여름용 가디건", "얇은 후드집업");
            List<String> bottoms = Arrays.asList("린넨팬츠", "코튼팬츠", "나일론 카고팬츠", "데님 반바지");
            List<String> points = Arrays.asList("곱창머리끈", "사파리햇", "-", "비즈 팔찌");

            Random random = new Random();
            int randomTopIndex = random.nextInt(tops.size());
            int randomOuterIndex = random.nextInt(outers.size());
            int randomBottomIndex = random.nextInt(bottoms.size());
            int randomPointIndex = random.nextInt(points.size());

            String top = tops.get(randomTopIndex);
            String outer = outers.get(randomOuterIndex);
            String bottom = bottoms.get(randomBottomIndex);
            String point = points.get(randomPointIndex);

            for (int i = 0; i < itemList.getLength(); i++) {
                Element item = (Element) itemList.item(i);
                Object category = item.getElementsByTagName("category").item(0).getTextContent();
                String fcstValue = item.getElementsByTagName("fcstValue").item(0).getTextContent();String baseDate = item.getElementsByTagName("baseDate").item(0).getTextContent();
                if(category.equals("T1H")){
                    double temperatureValue = Double.parseDouble(fcstValue);
                    if (temperatureValue >= 25) {
                        model.addAttribute("outer", outer);
                        model.addAttribute("top", top);
                        model.addAttribute("bottom", bottom);
                        model.addAttribute("point", point);
                    } else if (temperatureValue >= 23) {
                        model.addAttribute("top", "반팔");
                        model.addAttribute("outer", "가디건");
                        model.addAttribute("bottom", "반바지, 얇은 나일론 소재의 긴바지");
                    } else if (temperatureValue >= 20) {
                        model.addAttribute("top", "긴팔 셔츠,반팔 위 긴 셔츠나, 반팔과 핸드워머");
                        model.addAttribute("outer", "-");
                        model.addAttribute("bottom", "긴바지");
                    } else if (temperatureValue >= 17) {
                        model.addAttribute("top", "긴팔");
                        model.addAttribute("outer", "0온스 학잠, 반팔 위 겉옷 ");
                        model.addAttribute("bottom", "긴바지");
                    } else if (temperatureValue >= 12) {
                        model.addAttribute("top", "니트,블레이저");
                        model.addAttribute("outer", "2온스 학잠, 가디건, 후드집업");
                        model.addAttribute("bottom", "-");
                    } else if (temperatureValue >= 9) {
                        model.addAttribute("top", "보온을 위한 목티, 히트텍 ,기모 후드티");
                        model.addAttribute("outer", "기모 후드집업");
                        model.addAttribute("bottom", "-");
                    } else if (temperatureValue >= 5) {
                        model.addAttribute("top", "보온을 위한 목티, 히트텍");
                        model.addAttribute("outer", "코트, 가죽자켓, 플리스");
                        model.addAttribute("bottom", "반바지, 얇은 나일론 소재의 긴바지");
                    } else {
                        model.addAttribute("top", "보온을 위한 목티, 히트텍");
                        model.addAttribute("outer", "패딩, 코트, 목도리");
                        model.addAttribute("bottom", "-");
                    }
                    model.addAttribute("tem", temperatureValue);
                }
            }
            model.addAttribute("weatherInfo", weatherInfo);
            model.addAttribute("skyInfo", skyInfo);
            model.addAttribute("ootdInfo", ootdInfo);
            return "ootd";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error occurred while fetching weather data.");
            return "error";
        }
    }
}