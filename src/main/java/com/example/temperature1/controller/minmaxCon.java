package com.example.temperature1.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class minmaxCon {

    private String nx = "60"; // x좌표
    private String ny = "126"; // y좌표
    private String baseDate; // 오늘 날짜
    private String baseTime; // 조회 시각 (ex: 0100, 1130, ...)
    private String type = "xml"; // 조회하고 싶은 type

    @GetMapping("/minmaxtemperature")
    public String getTemperature(Model model) {
        String apiUrl = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst";
        String serviceKey = "7j%2Bj0bSrBD%2F9JbCHMUaNX3lm%2FxhH9nkQieSYF8IjvfZOBuK2f%2FgvJyiHeA27URwozmEk3U%2BSy%2BV6eUk9tba8UQ%3D%3D"; // 홈페이지에서 받은 키

        try {
            // 현재 날짜와 시각 정보를 가져옵니다.
            LocalDateTime now = LocalDateTime.now();
            LocalDate currentDate = now.toLocalDate();
            int hour = now.getHour();

            // 오늘 날짜로 baseDate 설정
            String formattedDate = currentDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            baseDate = formattedDate;

            // 12시 이전이면 앞에 0을 붙여줍니다.
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
            StringBuilder minInfo = new StringBuilder();
            StringBuilder maxInfo = new StringBuilder();

            String maxTemperature = null;
            String minTemperature = null;
            boolean foundTemperature = false;

            for (int i = 0; i < itemList.getLength(); i++) {
                Element item = (Element) itemList.item(i);
                String category = item.getElementsByTagName("category").item(0).getTextContent();
                String fcstValue = item.getElementsByTagName("obsrValue").item(0).getTextContent();

                if (category.equals("T1H")) {
                    // category가 'T1H'인 경우 기온 정보로 처리
                    weatherInfo.append(fcstValue).append("℃ ");
                    foundTemperature = true;
                    break;
                }
                if (category.equals("TMAX")) {
                    // category가 'TMAX'인 경우 최고온도 정보로 처리
                    maxTemperature = fcstValue;
                   // maxInfo.append("최고온도: ").append(maxTemperature).append("℃, ");
                }
                if (category.equals("TMIN")) {
                    // category가 'TMIN'인 경우 최저온도 정보로 처리
                    minTemperature = fcstValue;
                  //  minInfo.append("최저온도: ").append(minTemperature).append("℃, ");
                }
            }


            if (foundTemperature) {
                model.addAttribute("weatherInfo", weatherInfo.toString());
                model.addAttribute("maxTemperature", maxTemperature);
                model.addAttribute("minTemperature", minTemperature);
            } else {
                model.addAttribute("error", "No temperature data available.");
            }

            return "week";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error occurred while fetching weather data.");
            return "error";
        }
    }
}

