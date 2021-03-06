package com.vkrh0406.shop.Controller;

import com.vkrh0406.shop.domain.Address;
import com.vkrh0406.shop.domain.Cart;
import com.vkrh0406.shop.domain.Member;
import com.vkrh0406.shop.form.LoginForm;
import com.vkrh0406.shop.form.MemberForm;
import com.vkrh0406.shop.interceptor.SessionConst;
import com.vkrh0406.shop.resolver.Login;
import com.vkrh0406.shop.resolver.SessionCart;
import com.vkrh0406.shop.service.CategoryService;
import com.vkrh0406.shop.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("member")
public class MemberController {

    private final MemberService memberService;

    @GetMapping("new")
    public String newMemberForm(Model model, @SessionCart Cart cart){


        model.addAttribute("category", CategoryService.category);
        model.addAttribute("memberForm", new MemberForm());
        model.addAttribute("cartSize", (cart == null) ? 0 : cart.getSize());

        return "member/createMemberForm";
    }


    @PostMapping("new")
    public String newMember(Model model, @SessionCart Cart cart, @ModelAttribute @Valid MemberForm memberForm, BindingResult bindingResult){


        model.addAttribute("category", CategoryService.category);
        model.addAttribute("cartSize", (cart == null) ? 0 : cart.getSize());


        if (bindingResult.hasErrors()) {
            return "member/createMemberForm";
        }

        Member member = new Member(memberForm.getLoginId(), memberForm.getPassword(), memberForm.getName(),
                new Address(memberForm.getCity(), memberForm.getStreet(), memberForm.getZipcode()));


        try {
            memberService.saveMember(member);
        } catch (IllegalStateException e) {
            bindingResult.reject("????????? ????????? ??????","????????? ????????? ???????????????.");
            return "member/createMemberForm";
        }



        return "redirect:/";
    }


    @GetMapping("login")
    public String loginForm(Model model, @SessionCart Cart cart, @Login Member member){
        //?????? ????????? ?????????
        if (member != null) {
            return "redirect:/";
        }

        //???????????? ????????? ?????? ?????? ????????? ???????????? ????????????
        LoginForm loginForm = new LoginForm();
        loginForm.setLoginId("test");
        loginForm.setPassword("test!");

        model.addAttribute("category", CategoryService.category);
        model.addAttribute("loginForm", loginForm);
        model.addAttribute("cartSize", (cart == null) ? 0 : cart.getSize());

        return "member/loginForm";
    }
    @PostMapping("login")
    public String login(Model model, @SessionCart Cart cart, @Login Member member, @ModelAttribute @Valid LoginForm loginForm,
                        BindingResult bindingResult, HttpServletRequest request,@RequestParam(required = false) String redirectURI){
        //?????? ????????????
        model.addAttribute("category", CategoryService.category);
        model.addAttribute("cartSize", (cart == null) ? 0 : cart.getSize());

        if (bindingResult.hasErrors()) {
            return "member/loginForm";
        }
        //?????? ????????? ?????????
        if (member != null) {
            return "redirect:/";
        }


        HttpSession session = request.getSession();

        try {
            memberService.login(loginForm.getLoginId(), loginForm.getPassword(), session, cart);
        } catch (IllegalStateException e) {
            bindingResult.reject("????????? ?????? ????????????","????????? ?????? ????????? ????????????.");
            return "member/loginForm";
        }


        return "redirect:" + ((redirectURI != null) ? redirectURI : "/");
    }

    @GetMapping("logout")
    public String logout(Model model, @SessionCart Cart cart, @Login Member member,HttpSession session){
        //????????? ????????? ?????????
        if (member == null) {
            return "redirect:/";
        }
        Cart cart1 = new Cart();
        cart1.setOrderItems(cart.getOrderItems());
        cart1.setSize(cart.getSize());

        //?????? ?????????
        session.removeAttribute(SessionConst.SESSION_LOGIN_MEMBER);
        //????????? ????????? ???????????? ????????? ????????? ?????? ?????????
//        session.setAttribute(SessionConst.SESSION_CART,cart1);
        session.removeAttribute(SessionConst.SESSION_CART);



        return "redirect:/";
    }

}
