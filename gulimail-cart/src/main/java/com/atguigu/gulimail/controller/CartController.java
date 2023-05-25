package com.atguigu.gulimail.controller;

import com.atguigu.gulimail.interpt.CartInterpt;
import com.atguigu.gulimail.service.CartService;
import com.atguigu.gulimail.vo.Cart;
import com.atguigu.gulimail.vo.CartItem;
import com.atguigu.common.vo.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/getCartsByMemberId")
    @ResponseBody
    public List<CartItem> getCartsByMemberId(){
        return cartService.getCartsByMemberId();
    }

    /**
     * 购物车列表页数据获取方法
     * @return
     */
    @GetMapping("/cartList.html")
    public String toCartListPage(Model model) throws ExecutionException, InterruptedException {
        UserInfo userInfo = CartInterpt.threadLocal.get();
        Cart cart = cartService.getCartList(userInfo);
        model.addAttribute("cart",cart);
        return "cartList";
    }

    /**
     * 改变商品选中状态，同步到redis
     * @param skuId
     * @param check
     * @return
     */
    @GetMapping("/changeChecked")
    public String changeChecked(@RequestParam("skuId") Long skuId,@RequestParam("check") boolean check){
        cartService.changeChecked(skuId,check);
        //重新加载购物车列表页
        return "redirect:http://cart.gulimail.com/cartList.html";
    }

    /**
     * 改变商品数量，同步到redis
     * @param skuId
     * @param num
     * @return
     */
    @GetMapping("/changeCount")
    public String changeCount(@RequestParam("skuId") Long skuId,@RequestParam("num") int num){
        cartService.changeCount(skuId,num);
        //重新加载购物车列表页
        return "redirect:http://cart.gulimail.com/cartList.html";
    }


    /**
     * 删除商品，同步到redis
     * @param skuId
     * @return
     */
    @GetMapping("/delItemBySkuId")
    public String delItemBySkuId(@RequestParam("skuId") Long skuId){
        cartService.delItemBySkuId(skuId);
        //重新加载购物车列表页
        return "redirect:http://cart.gulimail.com/cartList.html";
    }

    /**
     * 添加购物车，
     * 然后重定向到成功页面
     * 这样防止刷新页面重复调用添加接口
     *
     * redirectAttributes.addAttribute("key","value"):代表把key放在重定向请求URL后面拼接参数?key=value
     * redirectAttributes.addFlashAttribute("key",value);代表把key放在缓存session中，页面可以${key}获取到对应value ,不过只能取一次，取完就删除key了
     * @param skuId
     * @param num
     * @param redirectAttributes
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId, @RequestParam("num") int num, RedirectAttributes redirectAttributes) throws ExecutionException, InterruptedException {
        CartItem cartItem = cartService.addCart(skuId,num);
        redirectAttributes.addAttribute("skuId",skuId);
        return "redirect:http://cart.gulimail.com/addToCartSuccessPage.html";
    }

    /**
     * 添加购物车后的成功页面，从redis中获取数据返回给前端
     * @param skuId
     * @param model
     * @return
     */
    @GetMapping("/addToCartSuccessPage.html")
    public String addToCartSuccessPage(@RequestParam("skuId") Long skuId, Model model){
        CartItem cartItem = cartService.getCartItemBySkuId(skuId.toString());
        model.addAttribute("cartItem",cartItem);
        return "success";
    }
}
