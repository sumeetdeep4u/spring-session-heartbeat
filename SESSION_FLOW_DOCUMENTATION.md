# Spring Session Heartbeat - Complete Flow Documentation

## Application Overview
This is a traditional Spring MVC application that implements session management with automatic timeout when all browser tabs are closed. The session stays alive as long as at least one browser tab is open, using a JavaScript heartbeat mechanism.

---

## Architecture Components

### 1. Configuration Files

#### `pom.xml`
- **Purpose**: Maven project configuration
- **Key Dependencies**:
  - Spring MVC 5.3.31
  - Servlet API 4.0.1
  - JSP/JSTL for views
- **Packaging**: WAR file for deployment

#### `WEB-INF/web.xml`
- **Purpose**: Servlet container configuration
- **Key Settings**:
  ```xml
  <session-timeout>1</session-timeout>  <!-- 1 minute timeout -->
  ```
- **Components**:
  - DispatcherServlet: `/` - Routes all requests to Spring MVC
  - ContextLoaderListener: Loads Spring context
  - Session timeout: 1 minute

#### `WEB-INF/spring/servlet-context.xml`
- **Purpose**: Spring MVC configuration
- **Key Beans**:
  - Component scanning: `com.example.demo`
  - ViewResolver: Maps view names to JSP files in `/WEB-INF/views/`
  - SessionInterceptor: Configured to intercept all requests except `/login` and `/heartbeat`

#### `WEB-INF/spring/root-context.xml`
- **Purpose**: Application-level Spring configuration
- **Current**: Empty (for shared beans if needed)

---

## Application Flow

### Flow 1: First Access (Unauthenticated User)

```
User Browser
    ↓
http://localhost:8081/home
    ↓
Tomcat Server (Port 8081)
    ↓
DispatcherServlet
    ↓
SessionInterceptor.preHandle()
    ├─ Gets HttpSession
    ├─ Checks: session.getAttribute("loggedIn")
    ├─ Result: null (not logged in)
    └─ Action: response.sendRedirect("/login")
    ↓
Browser redirected to /login
    ↓
PageController.login() [GET]
    ↓
Returns "login" view name
    ↓
ViewResolver resolves to: /WEB-INF/views/login.jsp
    ↓
login.jsp rendered and sent to browser
```

### Flow 2: Login Process

```
User enters credentials
    ↓
Form POST to /login
    ├─ username: "user"
    └─ password: "password123"
    ↓
DispatcherServlet
    ↓
SessionInterceptor SKIPPED (login excluded)
    ↓
PageController.handleLogin(username, password, session)
    ├─ Validates credentials
    ├─ If VALID:
    │   ├─ session.setAttribute("loggedIn", true)
    │   ├─ session.setAttribute("username", "user")
    │   └─ return "redirect:/home"
    └─ If INVALID:
        └─ return "redirect:/login?error=true"
    ↓
Browser redirected to /home
```

### Flow 3: Accessing Protected Page (After Login)

```
User Browser
    ↓
http://localhost:8081/home
    ↓
DispatcherServlet
    ↓
SessionInterceptor.preHandle()
    ├─ Gets HttpSession
    ├─ Checks: session.getAttribute("loggedIn")
    ├─ Result: true (logged in)
    └─ Returns: true (allow request to proceed)
    ↓
PageController.home()
    ↓
Returns "home" view name
    ↓
ViewResolver resolves to: /WEB-INF/views/home.jsp
    ↓
home.jsp rendered with JavaScript heartbeat
    ↓
Browser displays home page
```

### Flow 4: Heartbeat Mechanism (Keeping Session Alive)

```
home.jsp loads in browser
    ↓
JavaScript starts:
    setInterval(() => {
        fetch('/heartbeat', {credentials:'include'});
    }, 30000);  // Every 30 seconds
    ↓
Every 30 seconds:
    ↓
fetch('/heartbeat') called
    ↓
Browser sends HTTP GET to /heartbeat
    ├─ Includes session cookie (JSESSIONID)
    └─ credentials: 'include' ensures cookies sent
    ↓
Tomcat receives request
    ↓
DispatcherServlet
    ↓
SessionInterceptor SKIPPED (heartbeat excluded)
    ↓
HeartbeatController.heartbeat(session)
    ├─ Method body is empty
    └─ Simply accessing 'session' parameter keeps it alive
    ↓
Tomcat's SessionManager:
    ├─ Detects session was accessed
    └─ Resets inactivity timer to 0
    ↓
Session timeout counter RESET to 1 minute
    ↓
Loop continues while browser tab remains open
```

### Flow 5: Session Expiration (All Tabs Closed)

```
User closes all browser tabs
    ↓
JavaScript execution STOPS
    ↓
No more heartbeat requests sent
    ↓
Tomcat SessionManager:
    ├─ No session access for 1 minute
    ├─ Session inactivity timer reaches 1 minute
    └─ Triggers: session.invalidate()
    ↓
Session destroyed from memory
    ├─ session.getAttribute("loggedIn") → null
    └─ Session ID becomes invalid
    ↓
User reopens browser
    ↓
Navigates to http://localhost:8081/home
    ↓
DispatcherServlet
    ↓
SessionInterceptor.preHandle()
    ├─ Gets HttpSession (new session created)
    ├─ Checks: session.getAttribute("loggedIn")
    ├─ Result: null (session expired)
    └─ Action: response.sendRedirect("/login")
    ↓
User must login again ✅
```

### Flow 6: Logout Process

```
User clicks Logout button
    ↓
Browser navigates to /logout
    ↓
DispatcherServlet
    ↓
SessionInterceptor.preHandle()
    ├─ Checks: session.getAttribute("loggedIn")
    ├─ Result: true (currently logged in)
    └─ Returns: true (allow request)
    ↓
PageController.logout(session)
    ├─ session.invalidate()
    └─ return "redirect:/login"
    ↓
Session destroyed immediately
    ↓
Browser redirected to /login
```

---

## File Structure and Responsibilities

### Java Source Files

#### `PageController.java`
**Location**: `src/main/java/com/example/demo/`

**Responsibilities**:
- Display login page (GET /login)
- Process login form (POST /login)
- Display home page (GET /home)
- Handle logout (GET /logout)

**Key Methods**:
```java
@RequestMapping(value = "/login", method = RequestMethod.GET)
public String login() // Display login form

@RequestMapping(value = "/login", method = RequestMethod.POST)
public String handleLogin(...) // Validate credentials

@RequestMapping(value = "/home", method = RequestMethod.GET)
public String home() // Display home page

@RequestMapping(value = "/logout", method = RequestMethod.GET)
public String logout(HttpSession session) // Invalidate session
```

#### `HeartbeatController.java`
**Location**: `src/main/java/com/example/demo/`

**Responsibilities**:
- Receive heartbeat requests from JavaScript
- Keep session alive by accessing session object

**Key Method**:
```java
@RequestMapping(value = "/heartbeat", method = RequestMethod.GET)
@ResponseBody
public void heartbeat(HttpSession session)
```

**Note**: Empty method body - just accessing the session parameter is enough to keep it alive.

#### `SessionInterceptor.java`
**Location**: `src/main/java/com/example/demo/`

**Responsibilities**:
- Intercept ALL requests (except /login and /heartbeat)
- Check if user is authenticated
- Redirect to login if not authenticated

**Key Logic**:
```java
public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    HttpSession session = request.getSession();
    Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");
    
    if (loggedIn == null || !loggedIn) {
        response.sendRedirect(request.getContextPath() + "/login");
        return false; // Block request
    }
    
    return true; // Allow request to proceed
}
```

### View Files (JSP)

#### `login.jsp`
**Location**: `src/main/webapp/WEB-INF/views/`

**Responsibilities**:
- Display login form
- Show error message if login fails
- POST credentials to /login

**Key Features**:
- Form with username and password fields
- JavaScript to display error parameter from URL
- Uses `${pageContext.request.contextPath}` for proper context path

#### `home.jsp`
**Location**: `src/main/webapp/WEB-INF/views/`

**Responsibilities**:
- Display welcome message
- Show logout button
- Run heartbeat JavaScript

**Key Features**:
- Displays username from session
- JavaScript heartbeat every 30 seconds
- Uses Fetch API with credentials: 'include'

---

## Session Management Details

### Session Attributes

| Attribute Name | Type | Purpose | Set By |
|---------------|------|---------|--------|
| `loggedIn` | Boolean | Indicates user authentication status | PageController.handleLogin() |
| `username` | String | Stores logged-in username | PageController.handleLogin() |

### Session Lifecycle

1. **Creation**: Automatically created by Tomcat when first request arrives
2. **Authentication**: `loggedIn` attribute set to `true` upon successful login
3. **Maintenance**: Heartbeat requests reset inactivity timer every 30 seconds
4. **Expiration**: After 1 minute of inactivity (no requests)
5. **Destruction**: Explicitly via logout or automatically via timeout

### Why Session Expires When Tabs Close

```
Browser Tab Open:
    JavaScript running
        ↓
    Heartbeat every 30 seconds
        ↓
    Session accessed every 30 seconds
        ↓
    Inactivity timer never reaches 1 minute
        ↓
    Session stays alive ✅

All Tabs Closed:
    JavaScript stops
        ↓
    No heartbeat requests
        ↓
    No session access
        ↓
    Inactivity timer reaches 1 minute
        ↓
    Tomcat destroys session ✅
```

---

## Configuration Details

### Session Timeout Calculation

- **Configured Timeout**: 1 minute (in web.xml)
- **Heartbeat Interval**: 30 seconds (in home.jsp)
- **Safety Margin**: 30 seconds (timeout is 2x heartbeat interval)

**Why this works**:
- Heartbeat fires at: 0s, 30s, 60s, 90s...
- Each heartbeat resets timeout counter to 0
- If tab closes, next heartbeat never fires
- After 60 seconds, session expires

### Interceptor Exclusions

The SessionInterceptor does NOT intercept:
- `/login` - Must allow access to login page
- `/heartbeat` - Must allow heartbeat without authentication

**Configured in**: `servlet-context.xml`
```xml
<mvc:exclude-mapping path="/login"/>
<mvc:exclude-mapping path="/heartbeat"/>
```

---

## Security Considerations

### Current Implementation
- **Authentication**: Simple hardcoded credentials (user/password123)
- **Authorization**: Binary (logged in or not)
- **Session Management**: Cookie-based (JSESSIONID)

### For Production Use, Add:
1. **Password Encryption**: Use BCrypt or similar
2. **HTTPS Only**: Set secure flag on cookies
3. **CSRF Protection**: Add CSRF tokens to forms
4. **Session Fixation Protection**: Regenerate session ID after login
5. **HttpOnly Cookies**: Already configured in web.xml
6. **Database-backed Authentication**: Replace hardcoded credentials
7. **Role-based Authorization**: Add user roles and permissions

---

## Deployment Instructions

### Building the Application
```bash
cd C:\Sumit\PROJECTS\spring-session-heartbeat
mvn clean package
```

### Running with Embedded Tomcat (Development)
```bash
mvn tomcat7:run
```

**Access**: http://localhost:8081/

### Deploying to External Tomcat (Production)
1. Build WAR: `mvn clean package`
2. Copy `target/spring-session-heartbeat.war` to Tomcat's `webapps/` folder
3. Start Tomcat
4. Access: http://localhost:8080/spring-session-heartbeat/

---

## Testing the Flow

### Test 1: Login and Access
1. Navigate to http://localhost:8081/home
2. Should redirect to /login
3. Enter: user / password123
4. Should redirect to /home
5. Home page displays with logout button

### Test 2: Heartbeat Working
1. Login successfully
2. Open browser developer tools → Network tab
3. Watch for /heartbeat requests every 30 seconds
4. Each request keeps session alive

### Test 3: Session Timeout
1. Login successfully
2. Access /home
3. Close ALL browser tabs
4. Wait 70 seconds (to exceed 1-minute timeout)
5. Reopen browser
6. Navigate to /home
7. Should redirect to /login (session expired) ✅

### Test 4: Multiple Tabs
1. Login successfully
2. Open /home in 3 different tabs
3. Close 2 tabs
4. Wait 70 seconds
5. Refresh remaining tab
6. Should still work (session alive due to this tab's heartbeat)

### Test 5: Logout
1. Login successfully
2. Click Logout button
3. Should redirect to /login
4. Try to navigate to /home
5. Should redirect back to /login

---

## Troubleshooting

### Issue: Session not expiring after closing tabs
**Possible Causes**:
- Heartbeat interval too short relative to timeout
- Browser keeping background tabs alive
- Service workers or extensions keeping connection

**Solution**:
- Increase timeout in web.xml
- Check browser's task manager for background processes
- Test in incognito mode

### Issue: Session expiring too quickly
**Possible Causes**:
- Heartbeat not firing
- JavaScript errors in console
- CORS or cookie issues

**Solution**:
- Check browser console for JavaScript errors
- Verify /heartbeat requests in Network tab
- Ensure cookies are enabled

### Issue: Redirect loop on login
**Possible Causes**:
- SessionInterceptor intercepting /login
- Incorrect exclude-mapping configuration

**Solution**:
- Verify servlet-context.xml excludes /login
- Check interceptor logs

---

## Key Design Decisions

### Why 1-minute timeout?
- Quick demonstration of functionality
- Production should use 15-30 minutes
- Balance between security and user experience

### Why 30-second heartbeat?
- Provides 2x safety margin (heartbeat is half of timeout)
- Not too frequent (reduces server load)
- Not too infrequent (ensures session stays alive)

### Why Fetch API?
- Modern JavaScript standard
- Automatic cookie handling with `credentials: 'include'`
- Works with CORS if needed

### Why Interceptor instead of Filter?
- Spring MVC integration
- Easy access to Spring context
- Path-based exclusion support
- Type-safe request/response objects

---

## Related Files

- `pom.xml` - Maven dependencies and build configuration
- `web.xml` - Servlet and session configuration
- `servlet-context.xml` - Spring MVC configuration
- `PageController.java` - Login/Home/Logout endpoints
- `HeartbeatController.java` - Heartbeat endpoint
- `SessionInterceptor.java` - Authentication check
- `login.jsp` - Login form view
- `home.jsp` - Home page with heartbeat

---

## Summary

This application demonstrates a complete session management flow in traditional Spring MVC:

1. **Configuration-based timeout**: Set in web.xml (1 minute)
2. **Automatic session creation**: Managed by servlet container
3. **Custom authentication**: Via session attributes
4. **Interceptor-based protection**: Blocks unauthenticated requests
5. **JavaScript heartbeat**: Keeps session alive while browser is open
6. **Natural expiration**: Session dies when all tabs close

**Key Behavior**: Session is active as long as ANY browser tab is open, and expires 1 minute after ALL tabs are closed.
