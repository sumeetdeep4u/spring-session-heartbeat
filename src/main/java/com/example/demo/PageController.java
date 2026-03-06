package com.example.demo;

import javax.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PageController {

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login() {
        return "login";
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String handleLogin(@RequestParam String username, @RequestParam String password, HttpSession session) {
        // Simple hardcoded credentials for demo
        if ("user".equals(username) && "password123".equals(password)) {
            session.setAttribute("loggedIn", true);
            session.setAttribute("username", username);
            return "redirect:/home";
        }
        return "redirect:/login?error=true";
    }

    @RequestMapping(value = "/home", method = RequestMethod.GET)
    public String home() {
        return "home";
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
