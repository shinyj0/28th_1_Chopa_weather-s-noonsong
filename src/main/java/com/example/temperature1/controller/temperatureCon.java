package com.example.temperature1.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
import java.time.format.DateTimeFormatter;

//초단기실황
@Controller
public class temperatureCon {

    private String nx = "60"; // x좌표
    private String ny = "126"; // y좌표
    private String baseDate  ; // 조회하고 싶은 날짜
    private LocalDateTime now = LocalDateTime.now(); // 현재 시각을 조회 시각에.
    int hour = now.getHour()-1;
    private String baseTime = hour + "00";

    //private String baseTime = "1200";
    private String type = "xml"; // 조회하고 싶은 type
    private String top,outer,bottom;
    @GetMapping("/temperature")
    public String getTemperature(Model model) {
        String apiUrl = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst";
        String serviceKey = "7j%2Bj0bSrBD%2F9JbCHMUaNX3lm%2FxhH9nkQieSYF8IjvfZOBuK2f%2FgvJyiHeA27URwozmEk3U%2BSy%2BV6eUk9tba8UQ%3D%3D"; // 홈페이지에서 받은 키

        try {
            LocalDate currentDate = now.toLocalDate();
            String formattedDate = currentDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            baseDate = formattedDate;

            StringBuilder urlBuilder = new StringBuilder(apiUrl);
            urlBuilder.append("?" + URLEncoder.encode("ServiceKey", "UTF-8") + "=" + serviceKey);
            urlBuilder.append("&" + URLEncoder.encode("nx", "UTF-8") + "=" + URLEncoder.encode(nx, "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("ny", "UTF-8") + "=" + URLEncoder.encode(ny, "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("base_date", "UTF-8") + "=" + URLEncoder.encode(baseDate, "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("base_time", "UTF-8") + "=" + URLEncoder.encode(baseTime, "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("_type", "UTF-8") + "=" + URLEncoder.encode(type, "UTF-8"));

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
            System.out.println(result);

            // XML 파싱
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(url.openStream());

            // XML에서 필요한 데이터 추출
            NodeList itemList = document.getElementsByTagName("item");
            StringBuilder weatherInfo = new StringBuilder();
            //   String temperature = "";

            Element item = (Element) itemList.item(3);
            String category = item.getElementsByTagName("category").item(0).getTextContent();
            // 이 부분의 코드를 수정합니다.
            String fcstValue = item.getElementsByTagName("obsrValue").item(0).getTextContent();
            String baseDate = item.getElementsByTagName("baseDate").item(0).getTextContent();
            String baseTime = item.getElementsByTagName("baseTime").item(0).getTextContent();

            weatherInfo.append(fcstValue).append("℃, ");

            double tem1 = Double.parseDouble(fcstValue);
            if (tem1 <= 15) weatherInfo.append("\nCOLD");
            else if (tem1 > 15 && tem1 <= 25) weatherInfo.append("\nWARM");
            else if (tem1 > 25) weatherInfo.append("\nHOT");

// weatherInfo를 model에 추가할 때 변수명을 수정합니다.
            model.addAttribute("weatherInfo", weatherInfo.toString()); // 이 부분을 수정합니다.
            System.out.println(weatherInfo);
         //   processTemperature(fcstValue, model);
            // model.addAttribute("weatherInfo", weatherInfo.toString());

            return "recommend";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error occurred while fetching weather data.");
            return "error";
        }
    }
    @PostMapping("/temperature")
    public String processTemperature(
            @RequestParam("temperatureValue") String temperature,
            @RequestParam("precipitationValue") String precipitation,
            Model model
    ) {
        // 사용자가 입력한 온도와 강수량 값을 받아서 처리
        int temperatureValue = Integer.parseInt(temperature);

        // 강수량 값에 대한 처리 (추가적인 로직이 필요할 수 있습니다.)
        // ...
        if (temperatureValue >= 28) {

            model.addAttribute("top", "반팔");
            model.addAttribute("outer", "-");
            model.addAttribute("bottom", "반바지, 얇은 나일론 소재의 긴바지");
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


        System.out.println(top);
        return "recommend";
    }
}