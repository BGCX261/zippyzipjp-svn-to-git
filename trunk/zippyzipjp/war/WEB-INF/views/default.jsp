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
    page import="java.util.Collection" %><%@
    page import="jp.zippyzip.ContentStatus" %><%@
    page import="jp.zippyzip.Lzh" %><%@
    page import="com.google.appengine.api.users.UserService" %><%@
    page import="com.google.appengine.api.users.UserServiceFactory" %><%
    
    UserService userService = UserServiceFactory.getUserService();
    boolean isAdmin = (userService.isUserLoggedIn() && userService.isUserAdmin());
    String message = (String)request.getAttribute("message");
    String sttMessage = (String)request.getAttribute("stt");
    String sttEdit = (String)request.getAttribute("sttEdit");
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat formatJst = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    formatJst.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
    Collection<ContentStatus> sttList =
        (Collection<ContentStatus>)request.getAttribute("sttList");
    String fileUploadKey = (String)request.getAttribute("fileUploadKey");
    Collection<Lzh> lzhList =
        (Collection<Lzh>)request.getAttribute("archList");
    boolean isEven = true;
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <link type="text/css" rel="stylesheet" href="/stylesheets/main.css" />
        <title>管理機能 - Zippyzipjp</title>
    </head>
    <body>
        <div id="body">
            <div id="header">

<h1><a href="http://code.google.com/p/zippyzipjp/">Zippyzipjp</a>
    <a href="/zippyzipjp/feed.atom">
        <img style="vertical-align: bottom;" src="/images/feed-icon-14x14.png"/>
    </a>
</h1>
<div id="status"><%= message %></div>
<div id="bc">
    <div id="refreshpage"><a href="/zippyzipjp/">表示を更新する</a></div>
    <a href="http://code.google.com/p/zippyzipjp/">Home</a> &raquo;
    <a href="/">Menu</a> &raquo;
    管理機能
</div>

            </div>
            <div id="content">

<p><%= sttMessage %></p>

<h2>Menu</h2>
<ul>
<%  if (isAdmin) { %>
    <li><a href="./content/initAll">全てを初期化</a></li>
    <li><a href="./content/init">更新情報の初期化</a></li>
    <li><a href="./content/dep">依存コンテンツの確認</a></li>
    <li><a href="./content/con">コンテンツの確認</a></li>
    <li><a href="./arch/fetchLzh">最新のデータを日本郵便のサイトから取得</a></li>
    <li><a href="./arch/refreshZipDataStart">郵便番号データの更新を開始する</a></li>
    <li><a href="./arch/resetZipDataStart">郵便番号データのリセットを開始する</a></li>
    <li><a href="/zippyzipjp/job/updateBuilding">テスト</a></li>
<%  } else { %>
    <li>全てを初期化</li>
    <li>更新情報の初期化</li>
    <li>依存コンテンツの確認</li>
    <li>コンテンツの確認</li>
    <li>最新のデータを日本郵便のサイトから取得</li>
    <li>郵便番号データの更新を開始する</li>
    <li>郵便番号データのリセットを開始する</li>
<%  } %>
</ul>

<h2>更新状態</h2>

<form action="./content/status" method="post">
<table>
    <tr>
        <th>ID</th>
        <th>依存</th>
        <th>URL</th>
        <th>最終更新</th>
        <th>最終確認</th>
        <th>LHZ</th>
        <th>事業所</th>
        <th>-</th>
    </tr>
<%  if (sttEdit == null) {        
        if (isAdmin) { %>
    <tr>
        <td><input type="text" name="id" value="" size="5" /></td>
        <td><input type="text" name="depends" value="" size="5" /></td>
        <td><input type="text" name="url" value="" size="60" /></td>
        <td><input type="text" name="lastUpdate" value="" size="20" /></td>
        <td><input type="text" name="lastUpdate" value="" size="20" /></td>
        <td style="text-align:center;"><input type="checkbox" name="lzh" value="1" /></td>
        <td style="text-align:center;"><input type="checkbox" name="corp" value="1" /></td>
        <td><input type="submit" value="保存" /></td>
    </tr>
    <%  }
    } %>
<%  for (ContentStatus stt : sttList) {
        if (stt.getId().equals(sttEdit)) {
            if (isAdmin) { %>
    <tr>
        <td><input type="text" name="id" value="<%= stt.getId() %>" size="5" /></td>
        <td><input type="text" name="depends" value="<%= stt.getDepends() %>" size="5" /></td>
        <td><input type="text" name="url" value="<%= stt.getUrl() %>" size="60" /></td>
        <td><input type="text" name="lastUpdate" size="20"
            value="<%= formatJst.format(stt.getLastUpdate()) %>"/></td>
        <td><input type="text" name="lastCheck" size="20"
            value="<%= formatJst.format(stt.getLastCheck()) %>"/></td>
        <td style="text-align:center;">
            <input type="checkbox" name="lzh" value="true" <%= stt.isLzh() ? "checked" : "" %> />
        </td>
        <td style="text-align:center;">
            <input type="checkbox" name="corp" value="true" <%= stt.isCorp() ? "checked" : "" %> />
        </td>
        <td><input type="submit" value="保存" /></td>
    </tr>
            <%  sttEdit = null;
            }
        } else { %>                
    <tr>
        <td><%= stt.getId() %></td>
        <td><%= stt.getDepends() %></td>
        <td><%= stt.getUrl() %></td>
        <td><%= formatJst.format(stt.getLastUpdate()) %></td>
        <td><%= formatJst.format(stt.getLastCheck()) %></td>
        <td style="text-align:center;"><%= stt.isLzh() ? "o" : "" %></td>
        <td style="text-align:center;"><%= stt.isCorp() ? "o" : "" %></td>
        <td><%  if (isAdmin) {
        %><a href="./?sttEdit=<%= stt.getId() %>">編集</a><%
            } else {
        %>編集<%
            } %></td>
    </tr>
    <%  }    
    } %>
</table>
</form>

<h2>アーカイブデータ</h2>

<%  if (isAdmin) { %>
<form action="./arch/upload" method="post" enctype="multipart/form-data">
    <input type="file" name="<%= fileUploadKey %>"/>
    <input type="submit" value="Upload"/>
</form>
<%  } %>

<div class="list">
<%  for (Lzh lzh : lzhList) {
    isEven = !isEven; %>
    <p class="<%= isEven ? "even" : "odd" %>">
        <a href="./download/<%= lzh.getKey() %>">LZH</a> |
        <%  if (isAdmin) {
        %><a href="./arch/delete?key=<%= lzh.getKey() %>">削除</a> :<%
            } else {
        %>削除 :<%
            } %>
        <%= format.format(lzh.getTimestamp()) %> (
        <%= lzh.isUploaded() ? "Uploaded" : "Downloaded" %>
        <%= formatJst.format(lzh.getStored()) %> )
        <%= lzh.getFilename() %>
    </p>
<%  } %>
</div>

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
