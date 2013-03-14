<%@ page language="java" contentType="text/html; UTF-8" pageEncoding="UTF-8"
    import="java.util.*"%>

<%
    String institution = "MIT";
    Boolean authenticated = true;
    String username = null;
    String url = "http://delisle.mit.edu";
    url += request.getContextPath() + "/openGeoPortalHome.jsp";

    if (!((request.getRemoteUser() == null) || (request.getRemoteUser().isEmpty()))) {
        username = request.getRemoteUser();
    } else if (!((request.getRemoteAddr() == null) || (request.getRemoteAddr().isEmpty()))) {
        username = request.getRemoteAddr();
    } else {
        username = "anonymous";
    }

    session.setAttribute("username", username);
%>
<html>
    <head>
        <script type="text/javascript" src="../resources/javascript/jquery/js/jquery-1.6.4.min.js"></script>
        <script type="text/javascript" src="../resources/javascript/jquery.ba-postmessage.min.js"></script>
        <script type="text/javascript">
            var authenticated = <%out.write(Boolean.toString(authenticated));%>;
            var institution = "<%out.write(institution);%>;"
            var username = "<%out.write(username);%>";
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
        <%if (authenticated) {
            out.write("LOGGED IN");
        } else {
            out.write("NOT LOGGED IN");
        }%>
    </body>
</html>