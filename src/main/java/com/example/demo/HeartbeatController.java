package com.example.demo;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.io.IOException;

@Controller
public class HeartbeatController {

    @RequestMapping(value = "/heartbeat", method = RequestMethod.GET)
    public void heartbeat(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // getSession(false) never creates a new session — returns null if
        // expired/missing
        HttpSession session = request.getSession(false);
        if (session == null || !Boolean.TRUE.equals(session.getAttribute("loggedIn"))) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("session expired");
            return;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        long minutesLoggedIn = (System.currentTimeMillis() - session.getCreationTime()) / (1000 * 60);

        System.out.printf(
                "[%s] [HEARTBEAT] Session %s touched, maxInactive=%ds, user=%s, User logged in since %d min%n",
                LocalDateTime.now().format(formatter),
                session.getId(),
                session.getMaxInactiveInterval(),
                session.getAttribute("username"),
                minutesLoggedIn);

        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write("ok");
    }
}