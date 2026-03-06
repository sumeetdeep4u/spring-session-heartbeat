package com.example.demo;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class HeartbeatController {

    @RequestMapping(value = "/heartbeat", method = RequestMethod.GET)
    public void heartbeat(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // getSession(false) never creates a new session — returns null if expired/missing
        HttpSession session = request.getSession(false);
        if (session == null || !Boolean.TRUE.equals(session.getAttribute("loggedIn"))) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("session expired");
            return;
        }
        System.out.printf("[HEARTBEAT] Session %s touched, maxInactive=%ds, user=%s%n",
                session.getId(),
                session.getMaxInactiveInterval(),
                session.getAttribute("username"));
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write("ok");
    }
}
