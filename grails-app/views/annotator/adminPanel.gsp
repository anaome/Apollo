<%--
  Created by IntelliJ IDEA.
  User: ndunn
  Date: 5/12/15
  Time: 12:03 PM
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title></title>
</head>

<body>

<ul>
    <g:each in="${links}" var="link">
        <li>
            <g:link target="_blank" uri="${link.link}">${link.label}</g:link>
        </li>
    </g:each>
</ul>

</body>
</html>