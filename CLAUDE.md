# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build WAR file
mvn clean package

# Run with embedded Tomcat on port 8081
mvn tomcat7:run

# Compile only
mvn compile
```

Access the running app at http://localhost:8081/

## Architecture

This is a traditional **Spring MVC 5.3 / Java 17** web application (WAR packaging) that demonstrates session management with a JavaScript heartbeat to keep sessions alive while any browser tab is open.

### Key components

- **`PageController`** — Handles `/login` (GET/POST), `/home`, and `/logout`. Login validates hardcoded credentials (`user` / `password123`) and sets `session.setAttribute("loggedIn", true)`.
- **`HeartbeatController`** — Handles `GET /heartbeat`. Empty method body; merely receiving the request with the session parameter causes Tomcat to reset the session inactivity timer.
- **`SessionInterceptor`** — `HandlerInterceptor` that protects all routes except `/login` and `/heartbeat`. Checks `session.getAttribute("loggedIn")` and redirects to `/login` if absent.
- **`home.jsp`** — Runs a `setInterval` every 30 seconds calling `fetch('/heartbeat', {credentials:'include'})` to keep the session alive.

### Configuration files

- `WEB-INF/web.xml` — Sets session timeout to 1 minute; registers `DispatcherServlet` on `/`.
- `WEB-INF/spring/servlet-context.xml` — Registers the `SessionInterceptor` with `/**` mapping, excluding `/login` and `/heartbeat`; configures `InternalResourceViewResolver` for `/WEB-INF/views/*.jsp`.
- `WEB-INF/spring/root-context.xml` — Empty; present for shared beans.

### Session lifecycle

- **Timeout**: 1 minute (web.xml)
- **Heartbeat interval**: 30 seconds (home.jsp) — provides a 2x safety margin
- **Expiry trigger**: All browser tabs closed → JavaScript stops → no heartbeat → session expires after 1 minute of inactivity
