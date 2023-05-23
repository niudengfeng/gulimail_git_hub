package com.atguigu.gulimail.product.controller;

import com.atguigu.common.utils.BusinessCode;
import com.atguigu.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice(basePackages="com.atguigu.gulimail.product.controller")
public class ExceptionController {

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleException(MethodArgumentNotValidException e){
        log.error("异常",e.getClass());
        BindingResult bindingResult = e.getBindingResult();
        Map<String,String> errorMap = new HashMap<>();
        bindingResult.getFieldErrors().forEach((item)->{
            errorMap.put(item.getField(),item.getDefaultMessage());
        });
        return R.error(BusinessCode.VALIDERROR.getCode(),BusinessCode.VALIDERROR.getMessage()).put("data",errorMap);
    }

    @ExceptionHandler(value = Exception.class)
    public R handleException(Exception e){
        log.error("未知异常",e);
        return R.error(BusinessCode.ERROR.getCode(),BusinessCode.ERROR.getMessage());
    }

}
