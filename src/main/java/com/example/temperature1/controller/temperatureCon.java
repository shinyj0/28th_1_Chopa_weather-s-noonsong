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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Random;

//초단기실황
@Controller
public class temperatureCon {

    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    Calendar c1 = Calendar.getInstance();
    String strToday = sdf.format(c1.getTime());
    private String nx = "60"; // x좌표
    private String ny = "126";// y좌표
    private String baseDate = strToday; // 조회하고 싶은 날짜
    private String baseTime;
    private String type = "xml"; // 조회하고 싶은 type

    @GetMapping("/temperature")
    public String getTemperature(Model model) {
        String apiUrl = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst";
        String serviceKey = "7j%2Bj0bSrBD%2F9JbCHMUaNX3lm%2FxhH9nkQieSYF8IjvfZOBuK2f%2FgvJyiHeA27URwozmEk3U%2BSy%2BV6eUk9tba8UQ%3D%3D"; // 홈페이지에서 받은 키


        try {
            LocalDateTime now = LocalDateTime.now();
            int hour = now.getHour();
            if (hour < 12) {
                baseTime = String.format("%02d00", hour - 1); // 앞에 0을 붙여주기 위해 %02d 사용
            } else {
                baseTime = (hour - 1) + "00";
            }
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
            StringBuilder ootdInfo = new StringBuilder();
            String temperature = "";

            /*for (int i = 0; i < itemList.getLength(); i++) {
                Element item = (Element) itemList.item(i);
                String category = item.getElementsByTagName("category").item(0).getTextContent();
                String fcstValue = item.getElementsByTagName("obsrValue").item(0).getTextContent();
                String baseDate = item.getElementsByTagName("baseDate").item(0).getTextContent();
                String baseTime = item.getElementsByTagName("baseTime").item(0).getTextContent();

                weatherInfo.append("\tcategory: ").append(category).append(", obsrValue: ").append(fcstValue).append("\n");
            }*/

            Element item = (Element) itemList.item(3);
            String category = item.getElementsByTagName("category").item(0).getTextContent();
            String fcstValue = item.getElementsByTagName("obsrValue").item(0).getTextContent();
            String baseDate = item.getElementsByTagName("baseDate").item(0).getTextContent();
            String baseTime = item.getElementsByTagName("baseTime").item(0).getTextContent();

            weatherInfo.append("지금은 ").append(fcstValue).append("°, ");

            double tem1 = Double.parseDouble(fcstValue);
            if(tem1 <= 15) weatherInfo.append("\nCOLD");
            else if(tem1 > 15 && tem1 <= 25) weatherInfo.append("\nWARM");
            else if(tem1 > 25) weatherInfo.append("\nHOT");

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

            double temperatureValue = Double.parseDouble(fcstValue);
            if (temperatureValue >= 27) {
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


            model.addAttribute("weatherInfo", weatherInfo.toString());
            model.addAttribute("ootdInfo", ootdInfo.toString());
            model.addAttribute("temperature", temperature);
            return "recommend";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error occurred while fetching weather data.");
            return "error";
        }
    }
    /*Ncst category 순서
    PYT 강수형태
    REH 습도
    RN1 1시간 강수량
    T1H 기온
    UUU 풍속(동서)
    VEC 풍향
    VVV 풍속(남북)
    WSD 풍속*/
}