<!-- 
 * zippyzipjp
 * 
 * Copyright 2010 Michinobu Maeda.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 -->
<?xml version="1.0" encoding="UTF-8" ?><%@
    page language="java"
        contentType="text/html; charset=UTF-8"
        pageEncoding="UTF-8"
%><%@
    page import="java.text.SimpleDateFormat" %><%@
    page import="java.util.TimeZone" %><%@
    page import="java.util.LinkedList" %><%@
    page import="java.util.Date" %><%@
    page import="jp.zippyzip.web.BreadCrumb" %><%@
    page import="jp.zippyzip.web.ListItem" %><%@
    page import="com.google.appengine.api.users.UserService" %><%@
    page import="com.google.appengine.api.users.UserServiceFactory" %><%

    UserService userService = UserServiceFactory.getUserService();
    boolean isAdmin = (userService.isUserLoggedIn() && userService.isUserAdmin());
    SimpleDateFormat formatJst = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    formatJst.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
    LinkedList<BreadCrumb> breadCrumbs =
        (LinkedList<BreadCrumb>) request.getAttribute("breadCrumbs");
    Date timestamp = (Date) request.getAttribute("timestamp");
    LinkedList<ListItem> list =
        (LinkedList<ListItem>) request.getAttribute("list");
    boolean isEven = true;
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <link type="text/css" rel="stylesheet" href="/stylesheets/main.css" />
        <link type="text/css" rel="stylesheet" href="/stylesheets/listm.css" />
        <meta name="viewport" content="width=device-width" />
        <title><%
    for (BreadCrumb bc : breadCrumbs) {
        if (bc.getKey() == null) {
            %><%= bc.getName() %><%
        }
    } %> - 住所別郵便番号一覧 - Zippyzipjp</title>
    </head>
    <body>
        <div id="body">
            <div id="header">

<h1><a href="http://code.google.com/p/zippyzipjp/">Zippyzipjp</a>
    <a href="/zippyzipjp/feed.atom">
        <img style="vertical-align: bottom;" src="/images/feed-icon-14x14.png"/>
    </a>
</h1>
<div id="status"><c:out value="${message}"/></div>
<p class="timestamp">更新: <%= formatJst.format(timestamp) %></p>
<div id="bc">
    <a href="http://code.google.com/p/zippyzipjp/">Home</a>
    <%  for (BreadCrumb bc : breadCrumbs) { %>
        <%  if (bc.getKey() == null) { %>
    <%= (breadCrumbs.size() > 3) ? "<br />" : ""%> &raquo; <%= bc.getName() %>
        <%  } else { %>
    &raquo; <a href="/list/<%= bc.getKey() %>"><%= bc.getName() %></a>
        <%  } %>
    <%  } %>
</div>

            </div>
            <div id="content">

<div class="list">

<%  for (ListItem item : list) {
    isEven = !isEven;
%><div class="<%= isEven ? "even" : "odd" %>"><%
        if (item.getKey() == null) {
            %><span><%= item.getZip1() %><%= (item.getZip1().length() > 0) ? " - " : ""
            %><%= item.getZip2()
            %></span><%= ((item.getName().length() + item.getYomi().length()) > 18) ? "<br/>" : " "
            %><span class="name"><%= item.getName()
            %></span><%= ((item.getName().length() + item.getYomi().length()) > 24) ? "<br/>" : " "
            %><span><%= item.getYomi()
            %></span><div class="note"><%= item.getNote() %></div><%
        } else {
            %><a href="/list/<%= item.getKey() %>"><%= item.getName() %></a> <%= item.getYomi()
            %><%
        }
%></div><%
    }
%>
</div>
            
            </div>
            <div id="footer">
                
<div id="copyright">Redistributed by <a href="http://wiki.michinobu.jp/">Michinobu Maeda</a>.</div>

            </div>
        </div>
    </body>
<%  if (!isAdmin) { %>
<script type="text/javascript">

  var _gaq = _gaq || [];
  _gaq.push(['_setAccount', 'UA-6415485-9']);
  _gaq.push(['_trackPageview']);

  (function() {
    var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
  })();

</script>
<%  } %>
</html>
