<%@ page language="java" contentType="text/html; UTF-8" pageEncoding="UTF-8"
    import="java.util.*"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<%
    String institution = "MIT";
    String username = null;
    String url = "http://arrowsmith.mit.edu"; //set to OGP host
    url += request.getContextPath() + "/openGeoPortalHome.jsp";

%>
<html>
    <head>
        <script type="text/javascript" src="../resources/javascript/jquery/js/jquery-1.6.4.min.js"></script>
        <script type="text/javascript" src="../resources/javascript/jquery.ba-postmessage.min.js"></script>
        <script type="text/javascript">
            var authenticated = false;
            <sec:authorize access="isAuthenticated()">
                authenticated = true;
            </sec:authorize>
            var institution = "<%out.write(institution);%>;"
            var username = "<sec:authentication property="principal.username" />";
            var url = "<%out.write(url);%>";
            jQuery(function() {
                var msg = {
                    authenticated: authenticated,
                    admin: false,
                    username: username,
                    institution: institution
                };

                jQuery.postMessage(JSON.stringify(msg), url);
            });
        </script>
    </head>
    <body>
        <sec:authorize access="isAuthenticated()">
            LOGGED IN
        </sec:authorize>
    </body>
</html>