<!-- 
 * zippyzipjp: jQury で利用するプログラムのサンプル
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
    page import="com.google.appengine.api.users.UserService" %><%@
    page import="com.google.appengine.api.users.UserServiceFactory" %><%

    UserService userService = UserServiceFactory.getUserService();
    boolean isAdmin = (userService.isUserLoggedIn() && userService.isUserAdmin());

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>住所から郵便番号を選択 - サンプル - zippyzipjp</title>
        <link rel="stylesheet" type="text/css" href="/stylesheets/ui-lightness/jquery-ui-1.8.6.custom.css" />
        <link rel="stylesheet" type="text/css" href="/stylesheets/main.css" />
        <link rel="stylesheet" type="text/css" href="/stylesheets/jquerysample.css" />
        <script type="text/javascript" charset="UTF-8" src="/scripts/jquery-1.4.2.min.js"></script>
        <script type="text/javascript" charset="UTF-8" src="/scripts/jquery-ui-1.8.6.custom.min.js"></script>
        <script type="text/javascript" charset="UTF-8" src="/scripts/jquerysamplea.js"></script>
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
    <a href="/">Menu</a> &raquo; サンプル :
    <a href="/list/jquerysamplez">郵便番号 &rarr; 住所</a> |
    郵便番号 &larr; 住所 |
    <a href="/list/jquerysample">郵便番号 &harr; 住所</a>
</div>

            </div>
            <div id="content">

        <form id="main">
          <div>
            <span class="title">郵便番号</span>
            <span class="short-col">
              <input type="text" id="zip1" name="zip1" value="" maxlength="3" />
              -
              <input type="text" id="zip2" name="zip2" value="" maxlength="4" />
            </span>
          </div>
          <div>
            <span class="title">都道府県</span>
            <span class="short-col">
              <input type="text" id="pref" name="pref" value="" />
            </span>
          </div>
          <div>
            <span class="title">市区町村</span>
            <span class="short-col">
              <input type="text" id="city" name="city" value="" />
            </span>
            <input type="hidden" id="x0402" name="x0402" value="" />
            <button id="showCity">候補</button>
          </div>
          <div>
            <span class="title">住所１</span>
            <span class="short-col">
              <input type="text" id="add1" name="add1" value="" />
            </span>
            <button id="showAdd1">候補</button>
          </div>
          <div>
            <span class="title">住所２</span>
            <span class="short-col">
              <input type="text" id="add2" name="add2" value="" />
            </span>
            <button id="showAdd2">候補</button>
          </div>
          <div>
            <span class="title">事業所</span>
            <span class="short-col">
              <input type="text" id="corp" name="corp" value="" />
            </span>
            <button id="showCorp">候補</button>
          </div>
          <div>
            <span class="title">&nbsp;</span>
            <input type="reset" id="reset" name="reset" value="リセット" />
          </div>
        </form>
        <p>ソース <a href="/scripts/jquerysamplea.js" charset="UTF-8">jquerysamplea.js</a></p>

            </div>
            <div id="footer">

<div id="copyright">Copyright 2008-2010 <a href="http://wiki.michinobu.jp/">Michinobu Maeda</a>.</div>

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
