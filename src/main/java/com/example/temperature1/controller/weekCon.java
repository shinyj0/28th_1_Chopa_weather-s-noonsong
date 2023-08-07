package com.example.temperature1.controller;

import java.util.HashMap;
import java.util.Map;

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
public class weekCon {

    private String nx = "60"; // x좌표
    private String ny = "126"; // y좌표
    private String baseDate; // 오늘 날짜
    private String yesterdaybaseDate; // 어제 날짜
    private String tomorrowbaseDate; // 어제 날짜
    private String dayAfterTomorrowbaseDate;
    private String baseTime; // 조회 시각 (ex: 0100, 1130, ...)
    private String type = "xml"; // 조회하고 싶은 type
    private String numOfRows ="800";

    private String getSkyStatus(String skyCode) {
        Map<String, String> skyStatusMap = new HashMap<>();
        skyStatusMap.put("1", "맑음");
        skyStatusMap.put("3", "구름많음");
        skyStatusMap.put("4", "흐림");

        return skyStatusMap.getOrDefault(skyCode, "알 수 없음");
    }

    @GetMapping("/week")
    public String getTemperature(Model model) {
        String ultraSrtApiUrl = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst";
        String vilageFcstApiUrl = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst";
        String serviceKey = "7j%2Bj0bSrBD%2F9JbCHMUaNX3lm%2FxhH9nkQieSYF8IjvfZOBuK2f%2FgvJyiHeA27URwozmEk3U%2BSy%2BV6eUk9tba8UQ%3D%3D"; // 홈페이지에서 받은 키

        try {
            // 현재 날짜와 시각 정보를 가져옵니다.
            LocalDateTime now = LocalDateTime.now();

            LocalDate currentDate = now.toLocalDate();
            LocalDateTime yesterday = now.minusDays(1);
            // 내일 날짜에 대한 baseDate 설정
            LocalDate tomorrowDate = currentDate.plusDays(1);
            String formattedTomorrowDate = tomorrowDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            tomorrowbaseDate = formattedTomorrowDate;

// 모레 날짜에 대한 baseDate 설정
            LocalDate dayAfterTomorrowDate = currentDate.plusDays(2);
            String formattedDayAfterTomorrowDate = dayAfterTomorrowDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
// 이 값을 다른 변수에 할당하거나 baseDate 변수를 재사용하세요.
            dayAfterTomorrowbaseDate = formattedDayAfterTomorrowDate;

            int hour = now.getHour();

            // 오늘 날짜로 baseDate 설정
            String formattedDate = currentDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            baseDate = formattedDate;
            String formattedyesterdayDate = yesterday.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            yesterdaybaseDate = formattedyesterdayDate;

            // 12시 이전이면 앞에 0을 붙여줍니다.
            if (hour < 12) {
                baseTime = String.format("%02d00", hour - 1); // 앞에 0을 붙여주기 위해 %02d 사용
            } else {
                baseTime = (hour - 1) + "00";
            }
        //초단기 실황 호출
            StringBuilder ultraSrturlBuilder = new StringBuilder(ultraSrtApiUrl);
            ultraSrturlBuilder.append("?" + URLEncoder.encode("ServiceKey", "UTF-8") + "=" + serviceKey);
            ultraSrturlBuilder.append("&" + URLEncoder.encode("nx", "UTF-8") + "=" + URLEncoder.encode(nx, "UTF-8"));
            ultraSrturlBuilder.append("&" + URLEncoder.encode("ny", "UTF-8") + "=" + URLEncoder.encode(ny, "UTF-8"));
            ultraSrturlBuilder.append("&" + URLEncoder.encode("base_date", "UTF-8") + "=" + URLEncoder.encode(baseDate, "UTF-8"));
            ultraSrturlBuilder.append("&" + URLEncoder.encode("base_time", "UTF-8") + "=" + URLEncoder.encode(baseTime, "UTF-8"));
            ultraSrturlBuilder.append("&" + URLEncoder.encode("_type", "UTF-8") + "=" + URLEncoder.encode(type, "UTF-8"));

            URL utraSrturl = new URL(ultraSrturlBuilder.toString());
            HttpURLConnection ultraSrtconn = (HttpURLConnection) utraSrturl.openConnection();
            ultraSrtconn.setRequestMethod("GET");
            ultraSrtconn.setRequestProperty("Content-type", "application/xml");
            System.out.println("Response code: " + ultraSrtconn.getResponseCode());

            BufferedReader ultraSrtrd;
            if (ultraSrtconn.getResponseCode() >= 200 && ultraSrtconn.getResponseCode() <= 300) {
                ultraSrtrd = new BufferedReader(new InputStreamReader(ultraSrtconn.getInputStream(), "UTF-8"));
            } else {
                ultraSrtrd = new BufferedReader(new InputStreamReader(ultraSrtconn.getErrorStream(), "UTF-8"));
            }

            StringBuilder ultraSrtsb = new StringBuilder();
            String ultraSrtline;
            while ((ultraSrtline = ultraSrtrd.readLine()) != null) {
                ultraSrtsb.append(ultraSrtline);
            }
            ultraSrtrd.close();
            ultraSrtconn.disconnect();
            String ultraSrtresult = ultraSrtsb.toString();
            System.out.println("ultraSrt" +ultraSrtresult);

            // XML 파싱
            DocumentBuilderFactory ultraSrtfactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder ultraSrtbuilder = ultraSrtfactory.newDocumentBuilder();
            Document ultraSrtdocument = ultraSrtbuilder.parse(utraSrturl.openStream());


            // XML에서 필요한 데이터 추출
            NodeList ultraSrtitemList = ultraSrtdocument.getElementsByTagName("item");
            StringBuilder weatherInfo = new StringBuilder();
           // String precipitation = null;


            boolean foundTemperature = false;

            for (int i = 0; i < ultraSrtitemList.getLength(); i++) {
                Element item = (Element) ultraSrtitemList.item(i);
                String category = item.getElementsByTagName("category").item(0).getTextContent();
                String obsrValue = item.getElementsByTagName("obsrValue").item(0).getTextContent();

                if (category.equals("T1H")) {
                    // category가 'T1H'인 경우 기온 정보로 처리
                    weatherInfo.append(obsrValue).append("° ");
                    foundTemperature = true;
                  //  break;
                }

            }


            if (foundTemperature) {
                model.addAttribute("weatherInfo", weatherInfo.toString());


            }



            else {
                model.addAttribute("error", "No temperature data available.");
            }



            //초단기예보
            StringBuilder vilageFcstUrlBuilder = new StringBuilder(vilageFcstApiUrl);
            vilageFcstUrlBuilder.append("?" + URLEncoder.encode("ServiceKey", "UTF-8") + "=" + serviceKey);
            vilageFcstUrlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode("1000", "UTF-8"));
            vilageFcstUrlBuilder.append("&" + URLEncoder.encode("nx", "UTF-8") + "=" + URLEncoder.encode(nx, "UTF-8"));
            vilageFcstUrlBuilder.append("&" + URLEncoder.encode("ny", "UTF-8") + "=" + URLEncoder.encode(ny, "UTF-8"));
            vilageFcstUrlBuilder.append("&" + URLEncoder.encode("base_date", "UTF-8") + "=" + URLEncoder.encode(yesterdaybaseDate, "UTF-8"));
           //
           // vilageFcstUrlBuilder.append("&" + URLEncoder.encode("base_date", "UTF-8") + "=" + URLEncoder.encode(yesterdaybaseDate, "UTF-8"));
           // vilageFcstUrlBuilder.append("&" + URLEncoder.encode("base_date", "UTF-8") + "=" + URLEncoder.encode(yesterdaybaseDate, "UTF-8"));
            //
            vilageFcstUrlBuilder.append("&" + URLEncoder.encode("base_time", "UTF-8") + "=" + URLEncoder.encode("2000", "UTF-8"));
            vilageFcstUrlBuilder.append("&" + URLEncoder.encode("_type", "UTF-8") + "=" + URLEncoder.encode(type, "UTF-8"));

            URL vilageFcstUrl = new URL(vilageFcstUrlBuilder.toString());
            HttpURLConnection vilageFcstConn = (HttpURLConnection) vilageFcstUrl.openConnection();
            vilageFcstConn.setRequestMethod("GET");
            vilageFcstConn.setRequestProperty("Content-type", "application/xml");
            System.out.println("VilageFcst Response code: " + vilageFcstConn.getResponseCode());

            BufferedReader vilageFcstRd;
            if (vilageFcstConn.getResponseCode() >= 200 && vilageFcstConn.getResponseCode() <= 300) {
                vilageFcstRd = new BufferedReader(new InputStreamReader(vilageFcstConn.getInputStream(), "UTF-8"));
            } else {
                vilageFcstRd = new BufferedReader(new InputStreamReader(vilageFcstConn.getErrorStream(), "UTF-8"));
            }

            StringBuilder vilageFcstSb = new StringBuilder();
            String vilageFcstLine;
            while ((vilageFcstLine = vilageFcstRd.readLine()) != null) {
                vilageFcstSb.append(vilageFcstLine);
            }
            vilageFcstRd.close();
            vilageFcstConn.disconnect();
            String vilageFcstResult = vilageFcstSb.toString();
           // System.out.println("VilageFcst Result: " + precipitation);
            System.out.println("VilageFcst Result: " + vilageFcstResult);

            // XML 파싱 (초단기 예보에서 최저기온과 최고기온 추출)
            DocumentBuilderFactory vilageFcstFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder vilageFcstBuilder = vilageFcstFactory.newDocumentBuilder();
            Document vilageFcstDocument = vilageFcstBuilder.parse(vilageFcstUrl.openStream());

            // XML에서 필요한 데이터 추출 (최저기온과 최고기온)
            NodeList vilageFcstItemList = vilageFcstDocument.getElementsByTagName("item");
            String minTemperature = null;
            String maxTemperature = null;
            String  precipitation = null;
            String skyCode = null;
            String skyStatus = null;
            String minTemperature1 = null;
            String maxTemperature1 = null;
            String  precipitation1 = null;
            String minTemperature2 = null;
            String maxTemperature2 = null;
            String  precipitation2 = null;


            for (int i = 0; i < vilageFcstItemList.getLength(); i++) {
                Element item = (Element) vilageFcstItemList.item(i);
                String category = item.getElementsByTagName("category").item(0).getTextContent();
                String fcstValue = item.getElementsByTagName("fcstValue").item(0).getTextContent();
                String fcstDate = item.getElementsByTagName("fcstDate").item(0).getTextContent();

// 오늘 날짜와 fcstDate가 일치하는 경우에만 최저기온과 최고기온을 추출합니다.
                if (fcstDate.equals(formattedDate)) {
                    if (category.equals("TMN")) {
                        minTemperature = fcstValue;
                    } else if (category.equals("TMX")) {
                        maxTemperature = fcstValue;
                    } else if (category.equals("SKY")) {
                        skyCode = fcstValue;
                        if (skyCode.equals("1")) {
                            skyStatus = "맑음";
                        } else if (skyCode.equals("3")) {
                            skyStatus = "구름 많음";
                        } else if (skyCode.equals("4")) {
                            skyStatus = "흐림";
                        }
                    } else if (category.equals("POP")) {
                        precipitation = fcstValue;
                    }
                }
                if (fcstDate.equals(tomorrowbaseDate)) {
                    if (category.equals("TMN")) {
                        minTemperature1 = fcstValue;
                    } else if (category.equals("TMX")) {
                        maxTemperature1 = fcstValue;
                    } else if (category.equals("POP")) {
                        precipitation1 = fcstValue;
                    }
                }
           //     System.out.println(tomorrowbaseDate);
             //   System.out.println(dayAfterTomorrowbaseDate);
                if (fcstDate.equals(dayAfterTomorrowbaseDate)) {
                    if (category.equals("TMN")) {
                        minTemperature2 = fcstValue;
                    } else if (category.equals("TMX")) {
                        maxTemperature2 = fcstValue;
                    } else if (category.equals("POP")) {
                        precipitation2 = fcstValue;
                    }
                }


                // 최저기온과 최고기온 모두 추출되면 반복문 종료

                if (minTemperature != null && maxTemperature != null && skyCode != null && precipitation!= null && minTemperature1 != null && maxTemperature1 != null && precipitation1!= null && minTemperature2 != null && maxTemperature2 != null && precipitation2 != null) {
                    break;
                }

            }


            // 최저기온, 최고기온, 하늘상태를 모델에 추가
            if (minTemperature != null && maxTemperature != null && skyCode != null  && precipitation!= null && minTemperature2 != null && minTemperature1 != null && maxTemperature1 != null && precipitation1 != null && minTemperature2 != null && maxTemperature2 != null && precipitation2 != null) {

                model.addAttribute("minTemperature", minTemperature + "°");
                model.addAttribute("maxTemperature", maxTemperature + "°");
                model.addAttribute("skyStatus", skyStatus);
                model.addAttribute("precipitation", precipitation + "%");
                model.addAttribute("minTemperature1", minTemperature1 + "°");
                model.addAttribute("maxTemperature1", maxTemperature1 + "°");
                model.addAttribute("precipitation1", precipitation1 + "%");

                model.addAttribute("minTemperature2", minTemperature2 + "°");
                model.addAttribute("maxTemperature2", maxTemperature2 + "°");
                model.addAttribute("precipitation2", precipitation2 + "%");

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