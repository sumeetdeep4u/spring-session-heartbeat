<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <!DOCTYPE html>
    <html>

    <head>
        <meta charset="UTF-8">
        <title>Home</title>
    </head>

    <body>
        <h2>Home</h2>
        <p>Welcome, <%= session.getAttribute("username") %>!</p>
        <a href="${pageContext.request.contextPath}/logout"><button>Logout</button></a>

        <script>
            const HEARTBEAT_INTERVAL = 15000; // 15 seconds (session timeout is 1 minute, 4x margin)
            let heartbeatTimer;
            function sendHeartbeat() {
                fetch('${pageContext.request.contextPath}/heartbeat', { credentials: 'include' })
                    .then(resp => {
                        if (resp.status === 401) {
                            // Session expired on the server — redirect to login
                            window.location.href = '${pageContext.request.contextPath}/login';
                        }
                    })
                    .catch(err => console.warn('Heartbeat failed:', err));
            }

            // Send immediately on load, then every HEARTBEAT_INTERVAL regardless of tab visibility
            sendHeartbeat();
            heartbeatTimer = setInterval(sendHeartbeat, HEARTBEAT_INTERVAL);
        </script>
    </body>

    </html>