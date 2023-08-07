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
        if (precipitationValue> 0) {
            model.addAttribute("point", "우산, 우비, 장화");
        }
        model.addAttribute("tem", temperatureValue);
        model.addAttribute("pre", precipitationValue);


        System.out.println(top);
        return "inputtem";
    }
}