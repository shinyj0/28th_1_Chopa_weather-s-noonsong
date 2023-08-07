package com.example.temperature1.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
//초단기실황
@Controller
public class inputtemCon {

    private String top,outer,bottom;
    @GetMapping("/inputtem")
    public String showInputForm() {
        return "inputtem";
    }

    @PostMapping("/inputtem")
    public String processTemperature(
            @RequestParam("temperatureValue") String temperature,
            @RequestParam("precipitationValue") String precipitation,
            Model model
    ) {

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


        // 사용자가 입력한 온도와 강수량 값을 받아서 처리
        int temperatureValue = Integer.parseInt(temperature);
        int precipitationValue = Integer.parseInt(precipitation);

        // 강수량 값에 대한 처리 (추가적인 로직이 필요할 수 있습니다.)
        // ...
        if (temperatureValue >= 25) {
            model.addAttribute("outer", outer);
            model.addAttribute("top", top);
            model.addAttribute("bottom", bottom);
            model.addAttribute("point", point);

        } else if (temperatureValue >= 23) {
            model.addAttribute("top", "셔링 블라우스");
            model.addAttribute("outer", "가디건");
            model.addAttribute("bottom", "데님 스커트");
        } else if (temperatureValue >= 20) {
            model.addAttribute("top", "집업 맨투맨");
            model.addAttribute("outer", "-");
            model.addAttribute("bottom", "흑청바지");
        } else if (temperatureValue >= 17) {
            model.addAttribute("top", "스트라이프 셔츠");
            model.addAttribute("outer", "0온스 학잠");
            model.addAttribute("bottom", "치노 팬츠");
        } else if (temperatureValue >= 12) {
            model.addAttribute("top", "기모후드집업");
            model.addAttribute("outer", "가죽자켓");
            model.addAttribute("bottom", "조거팬츠");
        } else if (temperatureValue >= 9) {
            model.addAttribute("top", "목폴라");
            model.addAttribute("outer", "맥코트");
            model.addAttribute("bottom", "코듀로이 팬츠");
        } else if (temperatureValue >= 5) {
            model.addAttribute("top", "스웨터");
            model.addAttribute("outer", "두터운 플리스");
            model.addAttribute("bottom", "기모바지");
            model.addAttribute("point", "레그워머");
        } else {
            model.addAttribute("top", "니트집업");
            model.addAttribute("outer", "패딩");
            model.addAttribute("bottom", "패딩팬츠");
            model.addAttribute("point", "목도리");
        }
        if (precipitationValue> 0) {
            model.addAttribute("point", "레인부츠");
        }
        model.addAttribute("tem", temperatureValue);
        model.addAttribute("pre", precipitationValue);


        System.out.println(top);
        return "inputtem";
    }
}