package com.atguigu.gulimail.product.controller.web;

import com.atguigu.common.utils.R;
import com.atguigu.gulimail.product.entity.CategoryEntity;
import com.atguigu.gulimail.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class IndexController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping(value = {"/","/index.html","index","home"})
    public String index(Model model){
        List<CategoryEntity> ones = categoryService.categoryLevelOne();
        model.addAttribute("cateGory", ones);
        return "index";
    }

    @GetMapping("/index/catalog.json")
    @ResponseBody
    public R getCateGory3(){
        return R.ok().put("catalog",categoryService.categoryLevelThree());
    }

    @GetMapping("/yhxy")
    public String getYhxy(){
        return "yhxy";
    }

    /**
     * 隐私协议-游历星河-盛阳伍月
     * @return
     */
    @GetMapping("/yszc")
    public String getYszc(){
        return "yszc";
    }

}
