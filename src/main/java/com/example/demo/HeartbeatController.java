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
        // Just accessing session keeps it alive
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        System.out.printf(
            "[%s] [HEARTBEAT] Session %s touched, maxInactive=%ds, user=%s%n",
            LocalDateTime.now().format(formatter),
            session.getId(),
            session.getMaxInactiveInterval(),
            session.getAttribute("username")
        );
    }
}
