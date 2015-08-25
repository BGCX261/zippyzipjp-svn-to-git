<!-- 
 * Copyright 2010 Michinobu Maeda.
 * Licensed under the Apache License, Version 2.0 (the "License");
 *      http://www.apache.org/licenses/LICENSE-2.0
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

    String br = (String) request.getAttribute("br");
    SimpleDateFormat formatJst = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    formatJst.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
    LinkedList<BreadCrumb> breadCrumbs =
        (LinkedList<BreadCrumb>) request.getAttribute("breadCrumbs");
    Date timestamp = (Date) request.getAttribute("timestamp");
    LinkedList<ListItem> list =
        (LinkedList<ListItem>) request.getAttribute("list");
    boolean isEven = true;
    
    int lines = 100;
    int curr = 1;
    int last = 0;
    int cnt = 0;
    
    if (list.size() > (lines / 2 * 3)) {
        last = (list.size() - 1) / lines + 1;
        String[] pages = request.getParameterValues("page");
        curr = (pages == null) ? 1 : Integer.parseInt(pages[0]);
        if (curr < 1) { curr = 1; }
        if (curr > last) { curr = last; }
    }
    
    String path = (String) request.getAttribute("path");
%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <link type="text/css" rel="stylesheet" href="/stylesheets/listk.css" />
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

<h1><a href="http://code.google.com/p/zippyzipjp/">Zippyzipjp</a></h1>
<p class="timestamp">更新: <%= formatJst.format(timestamp) %></p>
<div id="bc"><a href="http://code.google.com/p/zippyzipjp/">Home</a><%
    for (BreadCrumb bc : breadCrumbs) {
        if (bc.getKey() == null) {
            %>&raquo;<%= bc.getName() %><%
        } else {
            %>&raquo;<a href="/list/<%= bc.getKey() %>"><%= bc.getName() %></a><%
        }
    }
%></div>
<div class="pages">
<%  for (int i = 1; i <= last; ++i) {
        if (i == curr) {
            %><%="" + i %> <%
        } else {
            %><a href="<%="/list" + path + "?page=" + i %>"><%="" + i %></a> <%
        }
    } %>
</div>

            </div>
            <div id="content">

<div class="list">

<%  for (ListItem item : list) {
        ++cnt;
        if (cnt <= (lines * (curr - 1))) { continue; }
        if ((lines * curr) < cnt) { break; }
        isEven = !isEven;
%><p class="<%= isEven ? "even" : "odd" %>"><%
        if (item.getKey() == null) {
            %><%= item.getZip1() %>-<%= item.getZip2()
            %> <span class="name"><%= item.getName() %></span> <%= item.getYomi()
            %> <span class="note"><%= item.getNote() %></span><%
        } else {
            %><a href="/list/<%= item.getKey() %>"><%= item.getName() %></a> <%= item.getYomi()
            %><%
        }
%></p><%
    }
%>
</div>
<div class="pages">
<%  for (int i = 1; i <= last; ++i) {
        if (i == curr) {
            %><%="" + i %> <%
        } else {
            %><a href="<%="/list" + path + "?page=" + i %>"><%="" + i %></a> <%
        }
    } %>
</div>
            
            </div>
            <div id="footer">
                
<div id="copyright">Redistributed by <a href="http://wiki.michinobu.jp/">Michinobu Maeda</a>.</div>

            </div>
        </div>
    </body>
</html>
