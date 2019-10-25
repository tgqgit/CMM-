<%--
  Created by IntelliJ IDEA.
  User: lenovo
  Date: 2019/10/21
  Time: 13:26
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head>
    <title>$Title$</title>
  </head>
  <script type='text/javascript' src='./dwr/engine.js'></script>
  <script type='text/javascript' src='./dwr/interface/HelloDwr.js'></script>
  <script type='text/javascript' src='./dwr/util.js'></script>
  <body>
  $END$
  <form action="" name="f1" method="post">
    用户名：<input type="text" name="username">
    <input type="button" value="comfirm" onclick="sendHellodwr(f1.username.value)">
  </form>
  </body>
  <script type="text/javascript">
    function sendHellodwr(s) {
      alert("按钮响应没问题");
      HelloDwr.helloDwr(s,callbackhellodwr);
    }
    function callbackhellodwr(data) {
      alert(data);
    }
  </script>
</html>
