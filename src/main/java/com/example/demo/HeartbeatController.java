package com.example.demo;

import javax.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

@Controller
public class HeartbeatController {

    @RequestMapping(value = "/heartbeat", method = RequestMethod.GET)
    @ResponseBody
    public void heartbeat(HttpSession session) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

        long minutesLoggedIn = (System.currentTimeMillis() - session.getCreationTime()) / (1000 * 60);

        System.out.printf(
                "[%s] [HEARTBEAT] Session %s touched, maxInactive=%ds, user=%s, User logged in since %d min%n",
                LocalDateTime.now().format(formatter),
                session.getId(),
                session.getMaxInactiveInterval(),
                session.getAttribute("username"),
                minutesLoggedIn);
    }
}