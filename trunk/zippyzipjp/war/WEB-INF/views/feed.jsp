<?xml version="1.0" encoding="UTF-8" ?>
<%@ page
    language="java"
    contentType="application/atom+xml; charset=UTF-8"
    pageEncoding="UTF-8"%><%@
    page import="java.text.SimpleDateFormat" %><%@
    page import="java.util.TimeZone" %><%@
    page import="jp.zippyzip.ZipInfo" %><%

    ZipInfo info = (ZipInfo) request.getAttribute("info");
    SimpleDateFormat formatJst = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    formatJst.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
    SimpleDateFormat formatUpd = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    formatUpd.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
    
    String generated = formatJst.format(info.getGenerated());
    String updated = formatUpd.format(info.getGenerated());
    
%><feed xmlns="http://www.w3.org/2005/Atom">

    <title>Zippyzipjp updated</title>
    <link href="http://zippyzipjp.appspot.com/feed"/>
    <updated><%=updated %></updated>
    <author>
        <name>Michinobu Maeda</name>
    </author>
    <id>http://zippyzipjp.appspot.com/feed</id>

    <entry>
        <title>Zippyzipjp updated: <%=generated %></title>
        <link href="http://zippyzipjp.appspot.com/"/>
        <id>http://zippyzipjp.appspot.com/<%=updated %></id>
        <updated><%=updated %></updated>
        <summary>郵便番号データを更新しました。</summary>
        <content type="xhtml" xml:lang="ja"
            xml:base="http://diveintomark.org/">
            <div xmlns="http://www.w3.org/1999/xhtml">
            <jsp:include page="/WEB-INF/views/contents.jsp" />
            </div>
        </content>
    </entry>

</feed>
