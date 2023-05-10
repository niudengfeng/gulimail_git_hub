package com.atguigu.common.valid;


import com.atguigu.common.valid.service.ListValue;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

public class AnnotationValid implements ConstraintValidator<ListValue,Integer> {

    private static Set<Integer> set = new HashSet<>();

    @Override
    public void initialize(ListValue constraintAnnotation) {
        for (Integer i : constraintAnnotation.vals()) {
            if (i!=null){
                set.add(i);
            }
        }
    }

    @Override
    public boolean isValid(Integer integer, ConstraintValidatorContext constraintValidatorContext) {
        if (set.contains(integer)){
            return true;
        }else {
            return false;
        }
    }


}
