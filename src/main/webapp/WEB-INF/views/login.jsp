<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <!DOCTYPE html>
    <html>

    <head>
        <meta charset="UTF-8">
        <title>Login</title>
    </head>

    <body>
        <h2>Login</h2>
        <form method="post" action="${pageContext.request.contextPath}/login">
            <input name="username" placeholder="username" value="user" required />
            <input name="password" placeholder="password" value="password123" type="password" required />
            <button type="submit">Login</button>
        </form>
        <p style="color:red;" id="error"></p>
        <script>
            const params = new URLSearchParams(window.location.search);
            if (params.get('error')) {
                document.getElementById('error').textContent = 'Invalid username or password';
            }
        </script>
    </body>

    </html>