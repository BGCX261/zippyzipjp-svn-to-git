/*
 * zippyzipjp
 * 
 * Copyright 2008-2010 Michinobu Maeda.
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
 */
package jp.zippyzip.web;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.zippyzip.City;
import jp.zippyzip.ParentChild;
import jp.zippyzip.Pref;
import jp.zippyzip.Zip;
import jp.zippyzip.ZipInfo;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * データの一覧を返すサーブレット。
 * 
 * @author Michinobu Maeda
 */
public class ListServlet extends HttpServlet {

    /** シリアライズのIDの値。 */
    private static final long serialVersionUID = -7687570505918475381L;
    
    /** JIS X 0401 コードのパターン */
    private static final Pattern PATTERN_PREF = Pattern.compile("[0-9]{2}");
    
    /** JIS X 0402 コードのパターン */
    private static final Pattern PATTERN_CITY = Pattern.compile("[0-9]{5}");
    
    /** JIS X 0402 コードと住所1のパターン */
    private static final Pattern PATTERN_ADD1 = Pattern.compile("[0-9]{5}\\-.*");
    
    /** JIS X 0402 コードと "j" のパターン */
    private static final Pattern PATTERN_CORP = Pattern.compile("[0-9]{5}c");
    
    /** 郵便番号のパターン */
    private static final Pattern PATTERN_ZIP = Pattern.compile("[0-9]{7}");
    
    /** 一覧のビュー */
    private static final String LIST_JSP = "/WEB-INF/views/list.jsp";
    
    /** 一覧のビュー : スマートフォン用 */
    private static final String LISTM_JSP = "/WEB-INF/views/listm.jsp";
    
    /** 一覧のビュー : 携帯用 */
    private static final String LISTK_JSP = "/WEB-INF/views/listk.jsp";
    
    /** サンプルのビュー */
    private static final String SAMPLE_JSP = "/WEB-INF/views/jquerysample.jsp";
    
    /** サンプルのビュー */
    private static final String SAMPLEA_JSP = "/WEB-INF/views/jquerysamplea.jsp";
    
    /** サンプルのビュー */
    private static final String SAMPLEZ_JSP = "/WEB-INF/views/jquerysamplez.jsp";
    
    /** デフォルトのパス */
    private static final String PATH_DEF = "/list/prefs";
    
    /** タイトル */
    private static final String PAGE_TITLE = "都道府県";

    /** ログ　*/
    protected Logger log = Logger.getLogger(this.getClass().getName());
    {
        log.setLevel(Level.INFO);
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String path = request.getPathInfo();
        String redirect = null;
        
        if ((path == null) || path.equals("") || path.equals("/")) {
            response.sendRedirect(PATH_DEF);
            return;
        } else {
            path = path.replace("/", "");
        }
        
        if (path.equals("jquerysample")) {
            getServletContext().getRequestDispatcher(SAMPLE_JSP
                    ).forward(request, response);
            return;
        }

        if (path.equals("jquerysamplea")) {
            getServletContext().getRequestDispatcher(SAMPLEA_JSP
                    ).forward(request, response);
            return;
        }
        if (path.equals("jquerysamplez")) {
            getServletContext().getRequestDispatcher(SAMPLEZ_JSP
                    ).forward(request, response);
            return;
        }
        
        if (path.equals("lastUpdate")) {
            
            PersistenceManager pm = PMF.get().getPersistenceManager();
            
            try {
                
                for (ZipInfo zipInfo : pm.getExtent(ZipInfo.class)) {
                    response.getWriter().print(zipInfo.getGenerated().toString());
                    break;
                }
                
            } finally {
                pm.close();
            }
            
            return;
        }
        
        final String add1Code;
        final String key;
        String val = path.endsWith(".json") ?
                path.substring(0, path.length() - 5) : path; 

        if (val.contains("-")) {
            key = val.substring(0, val.indexOf("-"));
            add1Code = val.substring(val.indexOf("-") + 1);
        } else {
            key = val;
            add1Code = null;
        }
        
        PersistenceManager pm = PMF.get().getPersistenceManager();

        try {
            
            ParentChild data =  null;
            
            try {

                if (PATTERN_ZIP.matcher(key).matches()) {
                    data = pm.getObjectById(ParentChild.class, key.substring(0, 3));
                } else {
                    data = pm.getObjectById(ParentChild.class, key);
                }
                
                data.getChildren().size();
                
            } catch (JDOObjectNotFoundException e) { }

            if (path.endsWith(".json")) {
                
                response.setContentType("text/json");
                response.setCharacterEncoding("UTF-8");
                
                final String ret;
                
                if (data == null) {
                    ret = "{}";
                } else if (key.equals("prefs")) {
                    ret = toJsonPrefs(data);
                } else if (PATTERN_PREF.matcher(key).matches()) {
                    ret = toJsonCities(data);
                } else if (PATTERN_CITY.matcher(key).matches()) {
                    if (add1Code == null) {
                        ret = toJsonAdd1s(data);
                    } else {
                        ret = toJsonAdd2s(data, add1Code);
                    }
                } else if (PATTERN_CORP.matcher(key).matches()) {
                    ret = toJsonCorps(data);
                } else if (PATTERN_ZIP.matcher(key).matches()) {
                    ret = toJsonZips(data, key, pm);
                } else {
                    ret = "{}";
                }
                
                response.getWriter().print(ret);
                
            } else {
                
                String userAgent = request.getHeader("User-Agent");
                String jsp = LIST_JSP;
                request.setAttribute("path", request.getPathInfo());
                
                if (!userAgent.toLowerCase().contains("mozilla")) {
                    jsp = LISTK_JSP;
                } else if (userAgent.toLowerCase().contains("mobile")) {
                    jsp = LISTM_JSP;
                }
                
                if (path.equals("prefs")) {
                    
                    setPrefs(request, data);
                    
                } else if (PATTERN_PREF.matcher(path).matches()) {

                    if (data == null) {
                        redirect = PATH_DEF;
                    } else {
                        setCities(request, data);
                    }
                    
                } else if (PATTERN_CITY.matcher(path).matches()) {
                    
                    if (data == null) {
                        redirect = "/list/" + path.substring(0, 2);
                    } else {
                        setAdd1s(request, data);
                    }
                    
                } else if (PATTERN_ADD1.matcher(path).matches()) {
                    
                    if (data == null) {
                        redirect = "/list/" + path.substring(0, 5);
                    } else {
                        setAdd2s(request, data, fromHex(add1Code));
                    }
                    
                } else if (PATTERN_CORP.matcher(path).matches()) {
                    
                    if (data == null) {
                        redirect = "/list/" + path.substring(0, 2);
                    } else {
                        setCorp(request, data);
                    }
                    
                } else if (path.equals("building")) {
                    
                    if (data == null) {
                        redirect = "/list/";
                    } else {
                        SortedMap<String, Pref> prefs = getPrefMap(pm);
                        SortedMap<String, City> cities = getCityMap(pm, prefs);
                        setBuilding(request, data, prefs, cities);
                    }
                    
                } else {
                    redirect = PATH_DEF;
                }
                
                if (redirect == null) {
                    getServletContext().getRequestDispatcher(
                            jsp).forward(request, response);
                }
            }

        } catch (JSONException e) {
            log.log(Level.WARNING, "", e);
        } finally {
            pm.close();
        }
        
        if (redirect != null) {
            response.sendRedirect(redirect);
        }
    }
    
    void setPrefs(HttpServletRequest request, ParentChild data
            ) throws JSONException {
        
        LinkedList<ListItem> list = new LinkedList<ListItem>();
        LinkedList<BreadCrumb> breadCrumbs = new LinkedList<BreadCrumb>();
        
        breadCrumbs.add(new BreadCrumb(null, PAGE_TITLE));
        request.setAttribute("breadCrumbs", breadCrumbs);
        request.setAttribute("br", "");
        
        for (String json : data.getChildren()) {
            
            Pref pref = Pref.fromJson(json);
            
            list.add(new ListItem(
                    pref.getCode(),
                    null,
                    null,
                    pref.getName(),
                    pref.getYomi(),
                    ""));
        }
        
        list.add(new ListItem(
                "building",
                null,
                null,
                "ビル階層ごと",
                "",
                ""));
        
        request.setAttribute("list", list);
        request.setAttribute("timestamp", data.getTimestamp());
    }
    
    void setCities(HttpServletRequest request, ParentChild data
            ) throws JSONException {
        
        LinkedList<ListItem> list = new LinkedList<ListItem>();
        LinkedList<BreadCrumb> breadCrumbs = new LinkedList<BreadCrumb>();
        long timestamp = new Date().getTime();
        Pref pref = Pref.fromJson(data.getParents().getFirst());

        breadCrumbs.add(new BreadCrumb("prefs", PAGE_TITLE));
        breadCrumbs.add(new BreadCrumb(null, pref.getName()));
        
        request.setAttribute("breadCrumbs", breadCrumbs);
        request.setAttribute("br", "");
        
        for (String json : data.getChildren()) {
            
            City city = City.fromJson(json);
            boolean expired = (city.getExpiration().getTime() < timestamp);
            
            list.add(new ListItem(
                    city.getCode() + (expired ? "c" : ""),
                    null,
                    null,
                    city.getName(),
                    city.getYomi(),
                    (expired ? "旧自治体" : "")));
        }
        
        request.setAttribute("list", list);
        request.setAttribute("timestamp", data.getTimestamp());
    }
    
    void setAdd1s(HttpServletRequest request, ParentChild data
            ) throws JSONException {

        LinkedList<Zip> zips = new LinkedList<Zip>();
        Set<String> add1s = new HashSet<String>();
        Set<String> add2s = new HashSet<String>();
        Set<String> add2hit = new HashSet<String>();
        LinkedList<ListItem> list = new LinkedList<ListItem>();
        LinkedList<BreadCrumb> breadCrumbs = new LinkedList<BreadCrumb>();
        Pref pref = Pref.fromJson(data.getParents().getFirst());
        City self = City.fromJson(data.getParents().getLast());
        
        breadCrumbs.add(new BreadCrumb("prefs", PAGE_TITLE));
        breadCrumbs.add(new BreadCrumb(pref.getCode(), pref.getName()));
        breadCrumbs.add(new BreadCrumb(null, self.getName() + "(住所)"));
        breadCrumbs.add(new BreadCrumb(self.getCode() + "c", "(事業所)"));
        
        request.setAttribute("breadCrumbs", breadCrumbs);
        request.setAttribute("br", "");
        
        for (String json : data.getChildren()) {
            
            Zip zip = Zip.fromJson(json);
            zips.add(zip);
            if (add1s.add(zip.getAdd1())) { continue; }
            add2s.add(zip.getAdd1());
        }
        
        for (Zip zip : zips) {
            
            if (add2s.contains(zip.getAdd1())) {
                
                if (!add2hit.contains(zip.getAdd1())) {
                    
                    list.add(new ListItem(
                            zip.getX0402() + "-" + toHex(zip.getAdd1()),
                            null,
                            null,
                            zip.getAdd1(),
                            zip.getAdd1Yomi(),
                            null));
                    
                    add2hit.add(zip.getAdd1());
                }

            } else {
                
                list.add(new ListItem(
                        null,
                        zip.getCode().substring(0, 3),
                        zip.getCode().substring(3),
                        zip.getAdd1() + zip.getAdd2(),
                        zip.getAdd1Yomi() + zip.getAdd2Yomi(),
                        zip.getNote()));
                
            }
            
        }
        
        request.setAttribute("list", list);
        request.setAttribute("timestamp", data.getTimestamp());
    }
    
    void setBuilding(HttpServletRequest request, ParentChild data,
            SortedMap<String, Pref> prefs, SortedMap<String, City> cities
            )throws JSONException {

        LinkedList<ListItem> list = new LinkedList<ListItem>();
        LinkedList<BreadCrumb> breadCrumbs = new LinkedList<BreadCrumb>();
        String x0402 = "";
        
        breadCrumbs.add(new BreadCrumb("prefs", PAGE_TITLE));
        breadCrumbs.add(new BreadCrumb(null, "ビル階層ごと"));
        
        request.setAttribute("breadCrumbs", breadCrumbs);
        request.setAttribute("br", "");
        
        for (String json : data.getChildren()) {
            
            Zip zip = Zip.fromJson(json);
            
            if (!x0402.equals(zip.getX0402())) {
                
                x0402 = zip.getX0402();
                
                list.add(new ListItem(
                        null,
                        "",
                        "",
                        prefs.get(x0402.subSequence(0, 2)).getName()
                            + "" + cities.get(x0402).getName(),
                        "",
                        ""));
            }
            
            int sep = zip.getAdd1().indexOf(" ");
            String add1 = (sep < 0) ? "" : zip.getAdd1().substring(0, sep);
            String name = zip.getAdd1().substring(sep + 1);
            
            sep = zip.getAdd1Yomi().indexOf(" ");
            
            list.add(new ListItem(
                    zip.getX0402() + "-" + toHex(add1 + name),
                    null,
                    null,
                    name,
                    zip.getAdd1Yomi().substring(sep + 1),
                    null));
        }
        
        request.setAttribute("list", list);
        request.setAttribute("timestamp", data.getTimestamp());
    }
    
    void setAdd2s(HttpServletRequest request, ParentChild data,
            String add1) throws JSONException {

        LinkedList<ListItem> list = new LinkedList<ListItem>();
        LinkedList<BreadCrumb> breadCrumbs = new LinkedList<BreadCrumb>();
        Pref pref = Pref.fromJson(data.getParents().getFirst());
        City self = City.fromJson(data.getParents().getLast());
        
        breadCrumbs.add(new BreadCrumb("prefs", PAGE_TITLE));
        breadCrumbs.add(new BreadCrumb(pref.getCode(), pref.getName()));
        breadCrumbs.add(new BreadCrumb(self.getCode(), self.getName()));
        breadCrumbs.add(new BreadCrumb(null, add1));
        
        request.setAttribute("breadCrumbs", breadCrumbs);
        request.setAttribute("br", "");
        
        for (String json : data.getChildren()) {
            
            Zip zip = Zip.fromJson(json);
            
            if (!add1.equals(zip.getAdd1())) { continue; }
            
            list.add(new ListItem(
                    null,
                    zip.getCode().substring(0, 3),
                    zip.getCode().substring(3),
                    zip.getAdd2(),
                    zip.getAdd2Yomi(),
                    zip.getNote()));
        }
        
        request.setAttribute("list", list);
        request.setAttribute("timestamp", data.getTimestamp());
    }
    
    void setCorp(HttpServletRequest request, ParentChild data
            ) throws JSONException {

        LinkedList<ListItem> list = new LinkedList<ListItem>();
        LinkedList<BreadCrumb> breadCrumbs = new LinkedList<BreadCrumb>();
        Pref pref = Pref.fromJson(data.getParents().getFirst());
        City self = City.fromJson(data.getParents().getLast());
        
        breadCrumbs.add(new BreadCrumb("prefs", PAGE_TITLE));
        breadCrumbs.add(new BreadCrumb(pref.getCode(), pref.getName()));
        breadCrumbs.add(new BreadCrumb(null, self.getName() + "(事業所)"));
        breadCrumbs.add(new BreadCrumb(self.getCode(), "(住所)"));
        
        request.setAttribute("breadCrumbs", breadCrumbs);
        request.setAttribute("br", "<br />");
        
        for (String json : data.getChildren()) {
            
            Zip zip = Zip.fromJson(json);
            
            list.add(new ListItem(
                    null,
                    zip.getCode().substring(0, 3),
                    zip.getCode().substring(3),
                    zip.getCorp(),
                    zip.getCorpYomi(),
                    zip.getAdd1() + zip.getAdd2()));
        }
        
        request.setAttribute("list", list);
        request.setAttribute("timestamp", data.getTimestamp());
    }
    
    String toHex(String str) {
        
        StringBuilder ret = new StringBuilder();
        
        for (int i = 0; i < str.length(); ++i) {
            ret.append(String.format("%04x", (int) str.charAt(i)));
        }
        
        return ret.toString();
    }
    
    String fromHex(String hex) {
        
        StringBuilder ret = new StringBuilder();
        
        for (int i = 0; i < hex.length(); i += 4) {
            
            try {
                ret.append((char) Integer.parseInt(hex.substring(i, i + 4), 16));
            } catch (NumberFormatException e) {
                log.warning(e.getMessage() + " : \"" + hex.substring(i, i + 4) + "\"");
            }
        }
        
        return ret.toString();
    }
    
    String toJsonPrefs(ParentChild data) throws JSONException {
        
        StringBuilder ret = new StringBuilder("{\"prefs\":[");
        boolean start = true;
        
        for (String json : data.getChildren()) {
            
            if (start) {
                start = false;
            } else {
                ret.append(",");
            }
            
            JSONObject jo = new JSONObject(json);
            
            jo.remove("yomi");
            ret.append(jo.toString());
        }
        
        return ret.append("]}").toString();
    }
    
    String toJsonCities(ParentChild data) throws JSONException {
        
        boolean start = true;
        JSONObject pref = new JSONObject(data.getParents().getFirst());
        StringBuilder ret = new StringBuilder("{\"pref\":");

        pref.remove("yomi");
        ret.append(pref.toString());
        ret.append(",\"cities\":[");
        
        for (String json : data.getChildren()) {
            
            if (start) {
                start = false;
            } else {
                ret.append(",");
            }
            
            JSONObject jo = new JSONObject(json);
            
            if (Timestamp.valueOf(jo.getString("expiration")).getTime()
                    < new Date().getTime()) {
                jo.put("note", "旧自治体");
            }
            
            jo.remove("yomi");
            jo.remove("expiration");
            ret.append(jo.toString());
        }
        
        return ret.append("]}").toString();
    }
    
    String toJsonAdd1s(ParentChild data) throws JSONException {
        
        LinkedList<String> zips = new LinkedList<String>();
        Set<String> add1s = new HashSet<String>();
        Set<String> add2s = new HashSet<String>();
        Set<String> add2hit = new HashSet<String>();
        
        boolean start = true;
        JSONObject pref = new JSONObject(data.getParents().getFirst());
        JSONObject city = new JSONObject(data.getParents().getLast());
        StringBuilder ret = new StringBuilder("{\"pref\":");

        pref.remove("yomi");
        ret.append(pref.toString());
        ret.append(",\"city\":");
        city.remove("yomi");
        city.remove("expiration");
        ret.append(city.toString());
        ret.append(",\"zips\":[");
        
        for (String json : data.getChildren()) {
            
            String add1 = new JSONObject(json).optString("add1", "");
            zips.add(json);
            if (add1s.add(add1)) { continue; }
            add2s.add(add1);
        }
        
        for (String json : zips) {
            
            JSONObject jo = new JSONObject(json);
            String add1 = jo.optString("add1", "");

            jo.remove("x0402");
            jo.remove("corp");
            jo.remove("add1Yomi");
            jo.remove("add2Yomi");
            jo.remove("corpYomi");
            
            if (add2s.contains(add1)) {
                
                if (!add2hit.contains(add1)) {
                    
                    if (start) {
                        start = false;
                    } else {
                        ret.append(",");
                    }
                    
                    jo.remove("code");
                    jo.remove("add2");
                    jo.remove("note");
                    jo.put("code", toHex(add1));
                    ret.append(jo.toString());
                    add2hit.add(add1);
                }

            } else {
                
                if (start) {
                    start = false;
                } else {
                    ret.append(",");
                }
                
                String zip = jo.optString("code", "");
                jo.remove("code");
                jo.put("zip1", zip.substring(0, 3));
                jo.put("zip2", zip.substring(3));
                ret.append(jo.toString());
            }
            
        }
        
        return ret.append("]}").toString();
    }
    
    String toJsonAdd2s(ParentChild data, String code
            ) throws JSONException {
        
        boolean start = true;
        JSONObject pref = new JSONObject(data.getParents().getFirst());
        JSONObject city = new JSONObject(data.getParents().getLast());
        String name = fromHex(code);
        StringBuilder ret = new StringBuilder("{\"pref\":");

        pref.remove("yomi");
        ret.append(pref.toString());
        ret.append(",\"city\":");
        city.remove("yomi");
        city.remove("expiration");
        ret.append(city.toString());
        ret.append(",\"add1\":{\"code\":\"");
        ret.append(code);
        ret.append("\",\"name\":\"");
        ret.append(name);
        ret.append("\"},\"zips\":[");
        
        for (String json : data.getChildren()) {
            
            JSONObject jo = new JSONObject(json);
            String add1 = jo.optString("add1", "");
            
            if (!name.equals(add1)) { continue; }
            
            if (start) {
                start = false;
            } else {
                ret.append(",");
            }
            
            String zip = jo.optString("code", "");
            jo.remove("code");
            jo.put("zip1", zip.substring(0, 3));
            jo.put("zip2", zip.substring(3));
            jo.remove("x0402");
            jo.remove("corp");
            jo.remove("add1");
            jo.remove("add1Yomi");
            jo.remove("add2Yomi");
            jo.remove("corpYomi");
            ret.append(jo.toString());
        }
        
        return ret.append("]}").toString();
    }
    
    String toJsonCorps(ParentChild data) throws JSONException {
        
        boolean start = true;
        StringBuilder ret = new StringBuilder("{\"pref\":");
        JSONObject pref = new JSONObject(data.getParents().getFirst());
        JSONObject city = new JSONObject(data.getParents().getLast());

        pref.remove("yomi");
        ret.append(pref.toString());
        ret.append(",\"city\":");
        city.remove("yomi");
        city.remove("expiration");
        ret.append(city.toString());
        ret.append(",\"zips\":[");
        
        for (String json : data.getChildren()) {
            
            if (start) {
                start = false;
            } else {
                ret.append(",");
            }
            
            JSONObject jo = new JSONObject(json);
            String code = jo.optString("code", "");
            
            jo.remove("code");
            jo.remove("x0402");
            jo.remove("corpYomi");
            jo.put("zip1", code.substring(0, 3));
            jo.put("zip2", code.substring(3));
            ret.append(jo.toString());
        }
        
        return ret.append("]}").toString();
    }
    
    String toJsonZips(ParentChild data, String zip, PersistenceManager pm
            ) throws JSONException {
        
        String zip2 = zip.substring(3);
        SortedSet<String> keys = new TreeSet<String>();
        boolean start = true;
        StringBuilder ret = new StringBuilder("{\"zip1\":\"");
        
        ret.append(data.getParents().getFirst());
        ret.append("\",\"zip2\":\"");
        ret.append(zip2);
        ret.append("\",\"zips\":[");
        
        for (String json : data.getChildren()) {
            
            JSONObject jo = new JSONObject(json);
            
            if (!jo.optString("zip2", "").equals(zip2)) { continue; }
            keys.add(jo.optString("key", ""));
        }
        
        for (String key : keys) {
            
            try {
                
                ParentChild zips = pm.getObjectById(ParentChild.class, key);
                City city = City.fromJson(zips.getParents().getLast());
            
                for (String json : zips.getChildren()) {
                    
                    JSONObject jo = new JSONObject(json);
                    
                    if (!jo.optString("code", "").equals(zip)) { continue; }
                    
                    if (start) {
                        start = false;
                    } else {
                        ret.append(",");
                    }
                    
                    jo.put("x0402", city.getCode());
                    jo.put("city", city.getName());
                    jo.remove("add1Yomi");
                    jo.remove("add2Yomi");
                    jo.remove("corpYomi");
                    ret.append(jo.toString());
                }
                
            } catch (JDOObjectNotFoundException e) { }
        }
        
        return ret.append("]}").toString();
    }
    
    /**
     * 都道府県のリストを取得する。
     * 
     * @return 都道府県のリスト
     */
    SortedMap<String, Pref> getPrefMap(PersistenceManager pm) {
        
        SortedMap<String, Pref> ret = new TreeMap<String, Pref>();
        
        for (String json : pm.getObjectById(
                ParentChild.class, "prefs").getChildren()) {
            
            Pref pref = Pref.fromJson(json);
            
            ret.put(pref.getCode(), pref);
        }
        
        return ret;
    }
    
    /**
     * 市区町村のリストを取得する。
     * 
     * @return 市区町村のリスト
     */
    SortedMap<String, City> getCityMap(PersistenceManager pm,
            SortedMap<String, Pref> prefs) {
        
        SortedMap<String, City> ret = new TreeMap<String, City>();
        
        for (Pref pref : prefs.values()) {
            
            ParentChild pc = pm.getObjectById(
                    ParentChild.class, pref.getCode());

            if (pc == null) { continue; }
            
            for (String json : pc.getChildren()) {
                
                City city = City.fromJson(json);

                ret.put(city.getCode(), city);
            }
        }
        
        return ret;
    }
}
