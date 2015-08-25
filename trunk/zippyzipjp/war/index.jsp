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
        pageEncoding="UTF-8" %><%@
    page import="java.text.SimpleDateFormat" %><%@
    page import="java.util.TimeZone" %><%@
    page import="jp.zippyzip.ZipInfo" %><%@
    page import="jp.zippyzip.web.ApplicationContext" %><%@
    page import="com.google.appengine.api.users.UserService" %><%@
    page import="com.google.appengine.api.users.UserServiceFactory" %><%

    UserService userService = UserServiceFactory.getUserService();
    boolean isAdmin = (userService.isUserLoggedIn() && userService.isUserAdmin());
    ZipInfo zipInfo = ApplicationContext.getContext().getDistributorService().getZipInfo();
    request.setAttribute("info", zipInfo);
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat formatJst = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    formatJst.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
    SimpleDateFormat formatTs = new SimpleDateFormat("yyyyMMddHHmmss");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <meta http-equiv="content-type" content="text/html; charset=UTF-8" />
        <link type="text/css" rel="stylesheet" href="/stylesheets/main.css" />
        <title>Menu - Zippyzipjp</title>
    </head>
    <body>
        <div id="body">
            <div id="header">

<h1><a href="http://code.google.com/p/zippyzipjp/">Zippyzipjp</a>
    <a href="/zippyzipjp/feed.atom">
        <img style="vertical-align: bottom;" src="/images/feed-icon-14x14.png"/>
    </a>
</h1>
<div id="status"></div>
<div id="bc">
    <a href="http://code.google.com/p/zippyzipjp/">Home</a> &raquo;
    Menu
</div>

            </div>
            <div id="content">

<ul>
    <li><a href="zippyzipjp/">管理機能</a></li>
</ul>
<h2>サンプル</h2>
<ul>
    <li><a href="/list/jquerysamplez">郵便番号 &rarr; 住所</a> /
        <a href="/list/jquerysamplea">郵便番号 &larr; 住所</a> /
        <a href="/list/jquerysample">郵便番号 &harr; 住所</a></li>
    <li><a href="http://code.google.com/p/zippyzipjp/downloads/list">バッチでデータを取り込むサンプル</a></li>
</ul>

<h2>最新のデータ
    <a href="/zippyzipjp/feed.atom">
        <img style="vertical-align: middle;" src="/images/feed-icon-14x14.png" alt="feed"/>
    </a>
</h2>

<div style="display: inline-block; margin: 0 2em 0 1em; float: left;">
    <img src="/images/zippyzipjplist.png"/ alt="QR code">
</div>


<%  if (zipInfo != null) { %>
    <jsp:include page="/WEB-INF/views/contents.jsp" />
<%  } else { %>
    <p>無効</p>
<%  } %>

<h2>Notice</h2>

<p class="notice">This product includes software developed by
    the Apache Software Foundation (http://www.apache.org).</p>
<p class="notice">This product includes software developed by
    Michel Ishizuka (http://homepage1.nifty.com/dangan/).</p>
<p class="notice">This product includes software developed by
    JSON.org (http://www.json.org).</p>
<p class="notice">This product includes software developed by
    The jQuery Project (http://jquery.com/).</p>
<p class="notice"><img src="http://code.google.com/appengine/images/appengine-silver-120x30.gif"
    alt="Powered by Google App Engine" /></p>

            </div>
            <div id="footer">

<div id="copyright">Copyright 2008-2010
    <a href="http://wiki.michinobu.jp/">Michinobu Maeda</a>.</div>

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
