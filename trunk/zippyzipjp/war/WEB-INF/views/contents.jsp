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
    
    String original = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(info.getTimestamp());
    String generated = formatJst.format(info.getGenerated());
    String ts = new SimpleDateFormat("yyyyMMddHHmmss").format(info.getGenerated());
    String base = "http://zippyzipjp.appspot.com";
    String download = "http://zippyzipjp.appspot.com/zippyzipjp/download";
    
%>
<p><%=generated %> 更新</p>
<p><%=original %> 配布元データのタイムスタンプ</p>
<ul>
    <li><a href="<%=base %>/list/prefs">住所別郵便番号一覧</a></li>
    <li><a href="<%=download %>/<%=ts %>_x0401.zip">
            都道府県コード ( JIS X 0401 準拠 )</a>
        <ul>
            <li>タブ区切り ( UTF-8 )</li>
            <li>CSV ( Shift_JIS )</li>
            <li>JSON ( UTF-8 )</li>
            <li>XML ( UTF-8 )</li>
        </ul>
    </li>
    <li><a href="<%=download %>/<%=ts %>_x0402.zip">
            市区町村コード ( JIS X 0402 にだいたい準拠 )</a>
        <ul>
            <li>タブ区切り ( UTF-8 )</li>
            <li>CSV ( Shift_JIS )</li>
            <li>JSON ( UTF-8 )</li>
            <li>XML ( UTF-8 )</li>
        </ul>
    </li>
    <li>住所データ
        <ul>
            <li><a href="<%=download %>/<%=ts %>_area_utf8_txt.zip">
                    タブ区切り ( UTF-8 )</a></li>
            <li><a href="<%=download %>/<%=ts %>_area_sjis_csv.zip">
                    CSV ( Shift_JIS )</a></li>
            <li><a href="<%=download %>/<%=ts %>_area_utf8_json.zip">
                    JSON ( UTF-8 )</a></li>
            <li><a href="<%=download %>/<%=ts %>_area_utf8_xml.zip">
                    XML ( UTF-8 )</a></li>
            <li><a href="<%=download %>/<%=ts %>_area_ime_dic.zip">
                    Windows IME用辞書データ ( Shift_JIS )</a></li>
        </ul>
    </li>
    <li>事業所データ
        <ul>
            <li><a href="<%=download %>/<%=ts %>_corp_utf8_txt.zip">
                    タブ区切り ( UTF-8 )</a></li>
            <li><a href="<%=download %>/<%=ts %>_corp_sjis_csv.zip">
                    CSV ( Shift_JIS )</a></li>
            <li><a href="<%=download %>/<%=ts %>_corp_utf8_json.zip">
                    JSON ( UTF-8 )</a></li>
            <li><a href="<%=download %>/<%=ts %>_corp_utf8_xml.zip">
                    XML ( UTF-8 )</a></li>
            <li><a href="<%=download %>/<%=ts %>_corp_ime_dic.zip">
                    Windows IME用辞書データ ( Shift_JIS )</a></li>
        </ul>
    </li>
    <li>細切れJSONデータ
        <ul>
            <li>郵便番号別 :
                <a href="<%=download %>/json_zip0.zip">0</a>
                <a href="<%=download %>/json_zip1.zip">1</a>
                <a href="<%=download %>/json_zip2.zip">2</a>
                <a href="<%=download %>/json_zip3.zip">3</a>
                <a href="<%=download %>/json_zip4.zip">4</a>
                <a href="<%=download %>/json_zip5.zip">5</a>
                <a href="<%=download %>/json_zip6.zip">6</a>
                <a href="<%=download %>/json_zip7.zip">7</a>
                <a href="<%=download %>/json_zip8.zip">8</a>
                <a href="<%=download %>/json_zip9.zip">9</a>
            </li> 
            <li><a href="<%=download %>/json_prefcity.zip">都道府県・市区町村</a></li>
            <li><a href="<%=download %>/json_area.zip">都道府県別 ( 住所 )</a></li>
            <li><a href="<%=download %>/json_corp.zip">都道府県別 ( 事業所 )</a></li>
        </ul>
    </li>
</ul>
