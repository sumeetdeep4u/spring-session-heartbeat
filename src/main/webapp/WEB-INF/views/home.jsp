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
            const HEARTBEAT_INTERVAL = 15000;    // 15 seconds
            const IDLE_TIMEOUT = 10 * 60 * 1000; // 10 minutes

            let lastActivityTime = Date.now();
            let idleLogged = false;

            function resetActivity() {
                if (idleLogged) {
                    idleLogged = false;
                    console.log('User active again, resuming heartbeat - ' + new Date().toLocaleString());
                }
                lastActivityTime = Date.now();
            }

            function sendHeartbeat() {
                if (Date.now() - lastActivityTime > IDLE_TIMEOUT) {
                    if (!idleLogged) {
                        idleLogged = true;
                        console.log('User idle for 10+ min, heartbeat paused - ' + new Date().toLocaleString());
                    }
                    return;
                }
                fetch('${pageContext.request.contextPath}/heartbeat', { credentials: 'include' })
                    .then(resp => {
                        if (resp.status === 401) {
                            console.log('Session expired, redirecting to login');
                            window.location.href = '${pageContext.request.contextPath}/login';
                        } else {
                            console.log('Heartbeat successful - ' + new Date().toLocaleString());
                        }
                    })
                    .catch(err => console.warn('Heartbeat failed:', err));
            }

            // Reset idle timer on any user interaction
            ['mousemove', 'mousedown', 'keydown', 'touchstart', 'scroll', 'click'].forEach(evt =>
                document.addEventListener(evt, resetActivity, { passive: true })
            );

            // Treat returning to a visible tab as activity
            document.addEventListener('visibilitychange', () => {
                if (document.visibilityState === 'visible') resetActivity();
            });

            // Send immediately on load, then on every interval (skipped when idle)
            sendHeartbeat();
            setInterval(sendHeartbeat, HEARTBEAT_INTERVAL);
        </script>
    </body>

    </html>