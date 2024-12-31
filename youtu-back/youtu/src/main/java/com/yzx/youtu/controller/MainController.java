package com.yzx.youtu.controller;

import com.yzx.youtu.common.BaseResponse;
import com.yzx.youtu.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class MainController {

    @GetMapping ("/hello")
    public BaseResponse<String> hello() {
        return ResultUtils.success("Hello");
    }

}
