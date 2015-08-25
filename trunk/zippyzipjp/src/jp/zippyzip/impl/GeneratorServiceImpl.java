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
package jp.zippyzip.impl;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.jdo.JDOObjectNotFoundException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import jp.gr.java_conf.dangan.util.lha.LhaHeader;
import jp.gr.java_conf.dangan.util.lha.LhaInputStream;
import jp.zippyzip.City;
import jp.zippyzip.GeneratorService;
import jp.zippyzip.Lzh;
import jp.zippyzip.LzhDao;
import jp.zippyzip.ParentChild;
import jp.zippyzip.ParentChildDao;
import jp.zippyzip.Pref;
import jp.zippyzip.RawDao;
import jp.zippyzip.Zip;
import jp.zippyzip.ZipInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONWriter;

/**
 * 生成物サービスの実装。
 * 
 * @author Michinobu Maeda
 */
public class GeneratorServiceImpl implements GeneratorService {
    
    /** 改行 ( CR, LF ) */
    private final String CRLF = new String(new char[] {(char)13, (char)10});
    
    /** ファイル名用の日付フォーマット */
    public static SimpleDateFormat FILENAME_DATE_FORMAT =
        new SimpleDateFormat("yyyyMMddHHmmss");

    /** ログ　*/
    protected Logger log = Logger.getLogger(this.getClass().getName());
    {
        log.setLevel(Level.INFO);
    }
        
    /** LZH のデータアクセスオブジェクト */
    private LzhDao lzhDao;
    
    /** バイナリデータのデータアクセスオブジェクト */
    private RawDao rawDao;
    
    /** 親子関係エンティティのデータアクセスオブジェクト */
    private ParentChildDao parentChildDao;
    
    /** 番地を列挙する数の上限 */
    private int streetNumSplitMax;

    /**
     * LZH のデータアクセスオブジェクトを取得する。
     * 
     * @return LZH のデータアクセスオブジェクト
     */
    public LzhDao getLzhDao() {
        return lzhDao;
    }

    /**
     * LZH のデータアクセスオブジェクトを設定する。
     * 
     * @param lzhDao LZH のデータアクセスオブジェクト
     */
    public void setLzhDao(LzhDao lzhDao) {
        this.lzhDao = lzhDao;
    }

    /**
     * バイナリデータのデータアクセスオブジェクトを取得する。
     * 
     * @return バイナリデータのデータアクセスオブジェクト
     */
    public RawDao getRawDao() {
        return rawDao;
    }

    /**
     * バイナリデータのデータアクセスオブジェクトを設定する。
     * 
     * @param rawDao バイナリデータのデータアクセスオブジェクト
     */
    public void setRawDao(RawDao rawDao) {
        this.rawDao = rawDao;
    }
    /**
     * 親子関係エンティティを取得する。
     * 
     * @return 親子関係エンティティ
     */
    public ParentChildDao getParentChildDao() {
        return parentChildDao;
    }
    
    /**
     * 親子関係エンティティを設定する。
     * 
     * @param parentChildDao 親子関係エンティティ
     */
    public void setParentChildDao(ParentChildDao parentChildDao) {
        this.parentChildDao = parentChildDao;
    }

    /**
     * 番地を列挙する数の上限を取得する。
     * 
     * @return 番地を列挙する数の上限
     */
    public int getStreetNumSplitMax() {
        return streetNumSplitMax;
    }

    /**
     * 番地を列挙する数の上限を設定する。
     * 　
     * @param streetNumSplitMax 番地を列挙する数の上限
     */
    public void setStreetNumSplitMax(int streetNumSplitMax) {
        this.streetNumSplitMax = streetNumSplitMax;
    }

    /* (non-Javadoc)
     * @see jp.zippyzip.GeneratorService#check()
     */
    public boolean check() {
        
        Date date = new Date();

        long upd = (getLzhDao().getZipInfo() == null) ? 0
                : getLzhDao().getZipInfo().getTimestamp().getTime();
        long cmp = 0;
        
        for (Lzh lzh : getLzhDao().getLatest(false, date)) {
            if (cmp < lzh.getTimestamp().getTime()) {
                cmp = lzh.getTimestamp().getTime();
            }
        }
        
        for (Lzh lzh : getLzhDao().getLatest(true, date)) {
            if (cmp < lzh.getTimestamp().getTime()) {
                cmp = lzh.getTimestamp().getTime();
            }
        }
        
        return (cmp > upd);
    }

    /* (non-Javadoc)
     * @see jp.zippyzip.GeneratorService#updateArea()
     */
    public void updateArea() {
        
        Date date = new Date();
        LinkedList<String> prefs = new LinkedList<String>();
        LinkedList<String> cities = new LinkedList<String>();
        LinkedList<String> zips = new LinkedList<String>();
        LinkedList<Lzh> lzhs = getLzhDao().getLatest(false, date);
        ZipInfo zipInfo = getLzhDao().updateArea(lzhs, date);
        long timestamp = zipInfo.getTimestamp().getTime();
        String rec = null;
        final Pattern add2NameWithPrefix = Pattern.compile(".*[^0-9A-Z\\-～][0-9A-Z]+");
        final Pattern add2NameWithoutPrefix = Pattern.compile("^[0-9A-Z\\-].*");
        final Pattern add2YomiWithPrefix = Pattern.compile(".*[0-9A-Z]$");
        final Pattern add2YomiWithoutPrefix = Pattern.compile("^[0-9A-Z].*");
        final Pattern noDigit = Pattern.compile("[^0-9]");
        final Pattern startsWithDigit = Pattern.compile("[0-9].*");
        final Pattern add2NameNoName = Pattern.compile("[0-9A-Z\\-～]+$");
        final Pattern add2YomiNoName = Pattern.compile("[0-9A-Z\\-]+$");
        final Pattern endsWithDigit = Pattern.compile(".*[0-9]$");
        int cityCount = 0;
        int zipCount = 0;
        
        try {
            
            String prefCode = "";
            String cityCode = "";
            
            for (Lzh lzh : lzhs) {
                
                byte[] raw = rawDao.get(lzh.getKey());
                
                LhaInputStream lis =
                    new LhaInputStream(new ByteArrayInputStream(raw));
                LhaHeader header = lis.getNextEntry();
                
                if (timestamp < header.getLastModified().getTime()) {
                    timestamp = header.getLastModified().getTime();
                }
                
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(lis, "MS932"));
                rec = reader.readLine();
                String zipCode  = "";
                String prefName = "";
                String cityName = "";
                String add1Name  = "";
                String add2Name  = "";
                String prefYomi = "";
                String cityYomi = "";
                String add1Yomi  = "";
                String add2Yomi  = "";
                String note  = "";
                boolean splited = false;
                
                while (rec != null) {
                    
                    rec = rec.trim();
               
                    // 01101,"060  ","0600000"
                    // ,"ﾎｯｶｲﾄﾞｳ","ｻｯﾎﾟﾛｼﾁｭｳｵｳｸ","ｲｶﾆｹｲｻｲｶﾞﾅｲﾊﾞｱｲ"
                    // ,"北海道","札幌市中央区","以下に掲載がない場合"
                    // ,0,0,0,0,0,0
                    int str = 0;
                    int end = 0;
                    int col = 0;
                    String code    = null;
                    String addName = "";
                    String addYomi = "";
                    
                    end = rec.indexOf("\",\"", str);
                    
                    while (0 < end) {
                        
                        ++ col;
                        String val = rec.substring(str, end);
                        
                        switch (col) {
                        case 1: code     = val; break;
                        case 2: zipCode  = val; break;
                        case 3: prefYomi = val; break;
                        case 4: cityYomi = val; break;
                        case 5: addYomi  = val; break;
                        case 6: prefName = val; break;
                        case 7: cityName = val; break;
                        default: break;
                        }
                        
                        str = end + 3;
                        end = rec.indexOf("\",\"", str);
                    }
                    
                    addName = rec.substring(str);
                    addName = ZenHanHelper.convertZH(addName.substring(0, addName.indexOf("\",")));
                    code = code.substring(0, code.indexOf(","));
                    
                    if (!cityCode.equals(code)) {
                        
                        if (cityCode.length() > 0) {
                                
                            LinkedList<String> parent = new LinkedList<String>();
                            parent.add(prefs.getLast());
                            parent.add(cities.getLast());
                            
                            getParentChildDao().store(
                                    new ParentChild(cityCode,
                                            new Date(timestamp),
                                            parent, zips));
                        }
                        
                        if (!prefCode.equals(code.substring(0, 2))) {
                            
                            if (prefCode.length() > 0) {
                                
                                LinkedList<String> parent = new LinkedList<String>();
                                parent.add(prefs.getLast());
                                
                                getParentChildDao().store(
                                        new ParentChild(prefCode,
                                                new Date(timestamp),
                                                parent, cities));
                            }
                            
                            prefCode = code.substring(0, 2);
                            prefs.add(new Pref(prefCode, prefName,
                                    ZenHanHelper.convertKana(prefYomi)
                                ).toJson());
                            cities = new LinkedList<String>();
                        }

                        cityCode = code;
                        cities.add(new City(cityCode, cityName,
                                ZenHanHelper.convertKana(cityYomi)).toJson());
                        ++cityCount;
                        zips = new LinkedList<String>();
                    }
                    
                    if ((!splited) && (0 <= addName.lastIndexOf("("))) {
                        
                        add1Name = addName.substring(0, addName.lastIndexOf("("));
                        add2Name = addName.substring(addName.lastIndexOf("(") + 1);
                        
                        if (0 <= addYomi.indexOf("(")) {
                            add1Yomi = addYomi.substring(0, addYomi.lastIndexOf("("));
                            add2Yomi = addYomi.substring(addYomi.lastIndexOf("(") + 1);
                        } else {
                            add1Yomi = addYomi;
                            add2Yomi = "";
                        }
                        
                        splited = true;
                        
                    } else if (splited) {
                        
                        add2Name = add2Name + addName;
                        if (add2Yomi.length() > 0) {
                            add2Yomi = add2Yomi + addYomi;
                        }
                        
                    } else {
                        
                        add1Name = addName;
                        add2Name = "";
                        add1Yomi = addYomi;
                        add2Yomi = "";
                    }

                    if (splited && (0 <= add2Name.lastIndexOf(")"))) {
                        
                        add2Name = add2Name.substring(0, add2Name.lastIndexOf(")"));
                        
                        if (0 <= add2Yomi.lastIndexOf(")")) {
                            add2Yomi = add2Yomi.substring(0, add2Yomi.lastIndexOf(")"));
                        }
                        
                        splited = false;
                    }
                    
                    if (!splited) {
                        
                        if (add1Name == null) { add1Name = ""; }
                        if (add2Name == null) { add2Name = ""; }
                        if (add1Yomi == null) { add1Yomi = ""; }
                        if (add2Yomi == null) { add2Yomi = ""; }

                        if (add1Name.equals("以下に掲載がない場合")
                                || add1Name.endsWith("村一円")
                                || add1Name.endsWith("町一円")) {
                            
                            note = add1Name;
                            add1Name = "";
                            add1Yomi = "";
                            
                        } else if (0 <= add1Name.indexOf("～")) {
                            
                            if (add1Name.endsWith("地割")) {
                                
                                for (int j = 0; j < 1; ++j) {
                                    
                                    String [] add1fragments = add1Name.split("～");
                                    if (add1fragments.length != 2) { continue; }
                                    String prefixName = startsWithDigit.matcher(add1fragments[0]).replaceFirst("");
                                    String prefixYomi = startsWithDigit.matcher(add1Yomi).replaceFirst("");
                                    add1fragments[0] = noDigit.matcher(add1fragments[0]).replaceAll("");
                                    add1fragments[1] = noDigit.matcher(add1fragments[1]).replaceAll("");
                                    int numStr = -1;
                                    try {
                                        numStr = Integer.parseInt(add1fragments[0]);
                                    } catch (NumberFormatException e) { continue; }
                                    int numEnd = -1;
                                    try {
                                        numEnd = Integer.parseInt(add1fragments[1]);
                                    } catch (NumberFormatException e) { continue; }
                                    if (numStr >= numEnd) { continue; }
                                    if (getStreetNumSplitMax() < (numEnd - numStr)) { continue; }
                                    add1Name = prefixName + numStr + "地割";
                                    add1Yomi = prefixYomi + numStr + "ﾁﾜﾘ";
                                    for (int k = numStr + 1; k <= numEnd; ++k) {
                                        add1Name = add1Name + "、" + prefixName + k + "地割";
                                        add1Yomi = add1Yomi + "､" + prefixYomi + k + "ﾁﾜﾘ";
                                    }
                                }

                            } else {
                                
                                note = add1Name + " " + add2Name;
                                add1Name = "";
                                add1Yomi = "";
                                add2Name = "";
                                add2Yomi = "";
                            }

                        } else if (add2Name.equals("その他")
                                || add2Name.equals("丁目")
                                || add2Name.equals("次のビルを除く")
                                || (0 <= add2Name.indexOf("○"))
                                || (0 <= add2Name.indexOf("無番地"))
                                || ((0 <= add2Name.indexOf("丁"))
                                        && (0 <= add2Name.indexOf("・"))
                                        && (0 <= add2Name.indexOf("～")))) {
                            
                            note = add2Name;
                            add2Name = "";
                            add2Yomi = "";
                            
                        }
                        
                        String [] add1Names = add1Name.split("、");
                        String [] add1Yomis = add1Yomi.split("､");
                        
                        for (int i1 = 0; i1 < add1Names.length; ++i1) {
                            if (0 < i1) {
                                if ((0 <= add1Names[0].indexOf("町"))
                                        && (0 > add1Names[i1].indexOf("町"))) {
                                    add1Names[i1] = add1Names[0].substring(
                                            0, add1Names[0].indexOf("町") + 1)
                                            + add1Names[i1];
                                    if (i1 < add1Yomis.length) {
                                        if ((0 <= add1Yomis[0].indexOf("ﾁｮｳ"))
                                                && (0 > add1Yomis[i1].indexOf("ﾁｮｳ"))) {
                                            add1Yomis[i1] = add1Yomis[0].substring(
                                                    0, add1Yomis[0].indexOf("ﾁｮｳ") + 3)
                                                    + add1Yomis[i1];
                                        }
                                    }
                                }
                            }
                            
                            String [] add2Names;
                            String [] add2Yomis;
                            
                            if (0 < add2Name.indexOf("「")) {
                                
                                boolean kagi = false;
                                StringBuilder add2NameBuilder = new StringBuilder(add2Name);
                                for (int i2 = 0; i2 < add2NameBuilder.length(); ++ i2) {
                                    if (!kagi) {
                                        if ((add2NameBuilder.charAt(i2) == '、')
                                                || (add2NameBuilder.charAt(i2) == '・')) {
                                            add2NameBuilder.setCharAt(i2, ',');
                                        } else if (add2NameBuilder.charAt(i2) == '「') {
                                            kagi = true;
                                        }
                                    } else if (add2NameBuilder.charAt(i2) == '」') {
                                        kagi = false;
                                    }
                                }

                                kagi = false;
                                StringBuilder add2YomiBuilder = new StringBuilder(add2Yomi);
                                for (int i2 = 0; i2 < add2YomiBuilder.length(); ++ i2) {
                                    if (!kagi) {
                                        if ((add2YomiBuilder.charAt(i2) == '､')
                                                || (add2YomiBuilder.charAt(i2) == '･')) {
                                            add2YomiBuilder.setCharAt(i2, ',');
                                        } else if (add2YomiBuilder.charAt(i2) == '<') {
                                            kagi = true;
                                        }
                                    } else if (add2YomiBuilder.charAt(i2) == '>') {
                                        kagi = false;
                                    }
                                }
                                
                                add2Names = add2NameBuilder.toString().split(",");
                                add2Yomis = add2YomiBuilder.toString().split(",");

                            } else {

                                add2Names = add2Name.split("、|・");
                                add2Yomis = add2Yomi.split("､|･");
                            }
                            
                            boolean isFragment = false;
                            for (int j = 0; j < add2Names.length; ++j) {
                                if (0 > add2Names[j].indexOf("～")) { continue; }
                                String [] add2fragments = add2Names[j].split("～");
                                if (add2fragments.length != 2) { continue; }
                                int numStr = -1;
                                try {
                                    numStr = Integer.parseInt(add2fragments[0]);
                                } catch (NumberFormatException e) { continue; }
                                String suffixName = "";
                                String suffixYomi = "";
                                if (add2fragments[1].endsWith("番地")) {
                                    suffixName = "番地";
                                    suffixYomi = "ﾊﾞﾝﾁ";
                                    add2fragments[1] = add2fragments[1].substring(0, add2fragments[1].length() - 2);
                                } else if (add2fragments[1].endsWith("番")) {
                                    suffixName = "番";
                                    suffixYomi = "ﾊﾞﾝ";
                                    add2fragments[1] = add2fragments[1].substring(0, add2fragments[1].length() - 1);
                                } else if (add2fragments[1].endsWith("丁目")) {
                                    suffixName = "丁目";
                                    suffixYomi = "ﾁｮｳﾒ";
                                    add2fragments[1] = add2fragments[1].substring(0, add2fragments[1].length() - 2);
                                }
                                int numEnd = -1;
                                try {
                                    numEnd = Integer.parseInt(add2fragments[1]);
                                } catch (NumberFormatException e) { continue; }
                                if (numStr >= numEnd) { continue; }
                                if (getStreetNumSplitMax() < (numEnd - numStr)) { continue; }
                                if (j >= add2Yomis.length) { continue; }
                                isFragment = true;
                                StringBuilder add2NameBuilder = new StringBuilder("").append(numStr).append(suffixName);
                                StringBuilder add2YomiBuilder = new StringBuilder("").append(numStr).append(suffixYomi);
                                add2Yomis[j] = "" + numStr + suffixYomi;
                                for (int k = numStr + 1; k <= numEnd; ++k) {
                                    add2NameBuilder.append(",").append(k).append(suffixName);
                                    add2YomiBuilder.append(",").append(k).append(suffixYomi);
                                }
                                add2Names[j] = add2NameBuilder.toString();
                                add2Yomis[j] = add2YomiBuilder.toString();
                            }
                            
                            if (isFragment) {
                                
                                StringBuilder add2NameBuilder = new StringBuilder("");
                                for (String fragment : add2Names) {
                                    if (add2NameBuilder.length() > 0) {
                                        add2NameBuilder.append(",");
                                    }
                                    add2NameBuilder.append(fragment);
                                }
                                add2Names = add2NameBuilder.toString().split(",");
                                
                                StringBuilder add2YomiBuilder = new StringBuilder("");
                                for (String fragment : add2Yomis) {
                                    if (add2YomiBuilder.length() > 0) {
                                        add2YomiBuilder.append(",");
                                    }
                                    add2YomiBuilder.append(fragment);
                                }
                                add2Yomis = add2YomiBuilder.toString().split(",");
                            }

                            String prefix = "";
                            for (int j = 0; j < add2Names.length; ++j) {
                                if (add2NameWithPrefix.matcher(add2Names[j]).matches()) {
                                    prefix = add2NameNoName.matcher(add2Names[j]).replaceFirst("");
                                } else if (add2NameWithoutPrefix.matcher(add2Names[j]).matches()) {
                                    if (0 < prefix.length()) { add2Names[j] = prefix + add2Names[j]; }
                                } else {
                                    prefix = "";
                                }
                            }

                            prefix = "";
                            for (int j = 0; j < add2Yomis.length; ++j) {
                                if (add2YomiWithPrefix.matcher(add2Yomis[j]).matches()) {
                                    prefix = add2YomiNoName.matcher(add2Yomis[j]).replaceFirst("");
                                } else if (add2YomiWithoutPrefix.matcher(add2Yomis[j]).matches()) {
                                    if (0 < prefix.length()) { add2Yomis[j] = prefix + add2Yomis[j]; }
                                } else {
                                    prefix = "";
                                }
                            }

                            String suffix = "";
                            for (int j = add2Names.length - 1; 0 <= j; --j) {
                                if (add2Names[j].endsWith("番地")
                                        || add2Names[j].endsWith("番地以上")
                                        || (0 <= add2Names[j].indexOf("番地〔"))) {
                                    suffix = "番地";
                                } else if (add2Names[j].endsWith("番")) {
                                    suffix = "番";
                                } else if (add2Names[j].endsWith("丁目")) {
                                    suffix = "丁目";
                                } else if (endsWithDigit.matcher(add2Names[j]).matches()) {
                                    if (0 < suffix.length()) { add2Names[j] = add2Names[j] + suffix; }
                                } else {
                                    suffix = "";
                                }
                            }

                            suffix = "";
                            for (int j = add2Yomis.length - 1; 0 <= j; --j) {
                                if (add2Yomis[j].endsWith("ﾊﾞﾝﾁ")) {
                                    suffix = "ﾊﾞﾝﾁ";
                                } else if (add2Yomis[j].endsWith("ﾊﾞﾝ")) {
                                    suffix = "ﾊﾞﾝ";
                                } else if (add2Yomis[j].endsWith("ﾊﾞﾝﾁｲｼﾞｮｳ")) {
                                    suffix = "ﾊﾞﾝﾁ";
                                } else if (add2Yomis[j].endsWith("ﾁｮｳﾒ")) {
                                    suffix = "ﾁｮｳﾒ";
                                } else if (endsWithDigit.matcher(add2Yomis[j]).matches()) {
                                    if (0 < suffix.length()) { add2Yomis[j] = add2Yomis[j] + suffix; }
                                } else {
                                    suffix = "";
                                }
                            }

                            for (int i2 = 0; i2 < add2Names.length; ++i2) {
                                
                                String noteFragment = note;
                                int kagiStr = add2Names[i2].indexOf('「');
                                if (0 <= add2Names[i2].indexOf("～")) {
                                    if (noteFragment.length() > 0) { noteFragment = noteFragment + "、"; }
                                    noteFragment = noteFragment + add2Names[i2];
                                    add2Names[i2] = "";
                                    add2Yomis[i2] = "";
                                } else if (0 <= kagiStr) {
                                    String addNote = add2Names[i2].substring(kagiStr);
                                    if (noteFragment.length() > 0) { noteFragment = noteFragment + "、"; }
                                    noteFragment = noteFragment + addNote;
                                    add2Names[i2] = add2Names[i2].substring(0, kagiStr);
                                }
                                if (i2 < add2Yomis.length) {
                                    int note2YomiStr = add2Yomis[i2].indexOf('<');
                                    if (0 <= note2YomiStr) {
                                        add2Yomis[i2] = add2Yomis[i2].substring(0, note2YomiStr);
                                    }
                                }
                                if (add2Names[i2].endsWith("以上")) {
                                    noteFragment = add2Names[i2];
                                    add2Names[i2] = "";
                                    if (i2 < add2Yomis.length) {
                                        add2Yomis[i2] = "";
                                    }
                                }

                                zips.add(new Zip(zipCode,
                                        cityCode,
                                        add1Names[i1],
                                        add2Names[i2],
                                        (i1 < add1Yomis.length) ?
                                                ZenHanHelper.convertKana(add1Yomis[i1]) : "",
                                        (i2 < add2Yomis.length) ?
                                                ZenHanHelper.convertKana(add2Yomis[i2]) : "",
                                        noteFragment
                                    ).toJson()
                                );
                                ++zipCount;
                                
                                noteFragment = "";
                            }
                       }
                        
                        add1Name = "";
                        add1Yomi = "";
                        add2Name = "";
                        add2Yomi = "";
                        note = "";
                    }

                    rec = reader.readLine();
                }
                
            }
            
            log.info(" prefs:" + prefs.size()
                    + " cities:" + cityCount
                    + " areas:" + zipCount);

            zipInfo.setTimestamp(new Date(timestamp));
            getLzhDao().store(zipInfo);
            getParentChildDao().store(new ParentChild("prefs",
                    new Date(timestamp), new LinkedList<String>(), prefs));
            
            LinkedList<String> parent = new LinkedList<String>();
            parent.add(prefs.getLast());
            getParentChildDao().store(new ParentChild(prefCode,
                    new Date(timestamp), parent, cities));
            
            parent = new LinkedList<String>();
            parent.add(prefs.getLast());
            parent.add(cities.getLast());
            getParentChildDao().store(new ParentChild(cityCode,
                    new Date(timestamp), parent, zips));
        
        } catch (IOException e) {
            log.log(Level.WARNING, rec, e);
        }
        
        return;
    }
    
    /* (non-Javadoc)
     * @see jp.zippyzip.GeneratorService#updateCorp()
     */
    public void updateCorp() {

        Date date = new Date();
        SortedMap<String, Pref> prefs = getPrefMap();
        SortedMap<String, City> cities = getCityMap();
        Set<String> cityCodes = new HashSet<String>();
        LinkedList<String> zips = new LinkedList<String>();
        LinkedList<Lzh> lzhs = getLzhDao().getLatest(true, date);
        ZipInfo zipInfo = getLzhDao().updateCorp(lzhs, date);
        long timestamp = zipInfo.getTimestamp().getTime();
        String rec = null;
        int cityCount = 0;
        int corpCount = 0;
        
        for (City city : getCities()) {
            cityCodes.add(city.getCode());
        }
        
        try {
            
            String cityCode = "";
                
            for (Lzh lzh : lzhs) {
                
                byte[] raw = rawDao.get(lzh.getKey());
                
                LhaInputStream lis =
                    new LhaInputStream(new ByteArrayInputStream(raw));
                LhaHeader header = lis.getNextEntry();
                
                if (timestamp < header.getLastModified().getTime()) {
                    timestamp = header.getLastModified().getTime();
                }
                
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(lis, "MS932"));
                rec = reader.readLine();
                
                while (rec != null) {
                    
                    rec = rec.trim();
                    
                    int str = 0;
                    int end = 0;
                    int col = 0;
                    String code     = "";
                    String corpYomi = "";
                    String corpName = "";
                    String cityName = "";
                    String add1Name  = "";
                    String add2Name  = "";
                    String zipCode   = "";
                                
                    end = rec.indexOf("\",\"", str);
                    
                    while (0 < end) {
                        
                        ++ col;
                        String val = rec.substring(str, end);
                        
                        switch (col) {
                        case 1: corpYomi = val; break;
                        case 2: corpName = val; break;
                        case 4: cityName = val; break;
                        case 5: add1Name = val; break;
                        case 6: add2Name = val; break;
                        case 7: zipCode  = val; break;
                        default: break;
                        }
                        
                        str = end + 3;
                        end = rec.indexOf("\",\"", str);
                    }
                    
                    if (corpYomi != null) {
                    
                        int sep = corpYomi.indexOf(",");
                        if (0 < sep) {
                            code = corpYomi.substring(0, sep);
                            corpYomi = corpYomi.substring(sep + 2);
                        }
                        
                        if (!cityCode.equals(code)) {
                            
                            if (cityCode.length() > 0) {
                                
                                if (prefs.get(cityCode.substring(0, 2)) != null) {
                                    
                                    LinkedList<String> parent = new LinkedList<String>();
                                    parent.add(prefs.get(cityCode.substring(0, 2)).toJson());
                                    parent.add(cities.get(cityCode).toJson());
                                    
                                    getParentChildDao().store(
                                            new ParentChild(cityCode + "c",
                                                    new Date(timestamp), parent, zips));
                                }
                                
                                zips = new LinkedList<String>();
                            }
                            
                            cityCode = code;

                            if (prefs.get(cityCode.substring(0, 2)) != null) {
                                
                                if (!cityCodes.contains(cityCode)) {
                                    
                                    City city = new City(cityCode, cityName, "", new Date(0));
                                    
                                    cityCodes.add(cityCode);
                                    cities.put(cityCode, city);
                                    getParentChildDao().addChild(
                                            cityCode.substring(0, 2), city.toJson());
                                    
                                    ++cityCount;
                                }
                            }
                        }
                   }
                    
                    if (0 < corpName.indexOf("，")) { corpName = corpName.replace("，", " "); }
                    if (0 < corpYomi.indexOf(".")) { corpYomi = corpYomi.replace(".", " "); }
                    
                    zips.add(new Zip(
                            zipCode,
                            cityCode,
                            ZenHanHelper.convertZH(add1Name),
                            ZenHanHelper.convertZH(add2Name),
                            ZenHanHelper.convertZH(corpName
                                    ).replace(") ", ")").replace(" (", "("),
                            ZenHanHelper.convertKana(corpYomi
                                    ).replace(") ", ")").replace(" (", "(")
                        ).toJson()
                    );
                    ++corpCount;

                    rec = reader.readLine();
                }
                
            }
            
            log.info("city:" + cityCodes.size() + " +" + cityCount
                    + " corp:" + corpCount);

            zipInfo.setTimestamp(new Date(timestamp));
            getLzhDao().store(zipInfo);
            
            if (prefs.get(cityCode.substring(0, 2)) != null) {

                LinkedList<String> parent = new LinkedList<String>();
                parent.add(prefs.get(cityCode.substring(0, 2)).toJson());
                parent.add(cities.get(cityCode).toJson());
                getParentChildDao().store(new ParentChild(cityCode + "c",
                        new Date(timestamp), parent, zips));
            }
            
        } catch (IOException e) {
            log.log(Level.WARNING, rec, e);
        }

        return;
    }
    
    /* (non-Javadoc)
     * @see jp.zippyzip.GeneratorService#preZips()
     */
    public void preZips() {
        
        Date timestamp = getLzhDao().getZipInfo().getTimestamp();
        LinkedList<Pref> prefs = getPrefs();
        LinkedList<City> cities = getCities();
        
        try {
            
            for (Pref pref : prefs) {
                
                TreeMap<String, TreeSet<String>> zips =
                    new TreeMap<String, TreeSet<String>>();
                    
                for (City city : cities) {
                    
                    if (!city.getCode().startsWith(pref.getCode())) { continue; }
    
                    ParentChild data = getParentChildDao().get(city.getCode());
                    
                    if (data != null) {
                        
                        for (String json : data.getChildren()) {
                            
                            String zip = new JSONObject(json).optString("code", "");
    
                            if (!zips.containsKey(zip)) {
                                zips.put(zip, new TreeSet<String>());
                            }
                            
                            zips.get(zip).add(city.getCode());
                        }
                    }
                    
                    data = getParentChildDao().get(city.getCode() + "c");
                    
                    if (data != null) {
                        
                        for (String json : data.getChildren()) {
                            
                            String zip = new JSONObject(json).optString("code", "");
    
                            if (!zips.containsKey(zip)) {
                                zips.put(zip, new TreeSet<String>());
                            }
                            
                            zips.get(zip).add(city.getCode() + "c");
                        }
                    }
                }
                
                StringBuilder rec = new StringBuilder("[");
                LinkedList<String> list = new LinkedList<String>();
                
                for (String zip : zips.keySet()) {
                    
                    for (String key : zips.get(zip)) {
                        
                        rec.append(new JSONStringer().object()
                                .key("zip").value(zip)
                                .key("key").value(key)
                            .endObject().toString());
                        
                        if (rec.length() > 400) {
                            rec.append("]");
                            list.add(rec.toString());
                            rec = new StringBuilder("[");
                        } else {
                            rec.append(",");
                        }
                    }
                }
                
                if (rec.length() > 1) {
                    rec.append("]");
                    list.add(rec.toString());
                }
                
                getParentChildDao().store(new ParentChild(
                        "pre" + pref.getCode(), timestamp,
                        new LinkedList<String>(), list));
                log.info(pref.getCode() + ":" + list.size());
            }
            
        } catch (JSONException e) {
            log.log(Level.WARNING, "", e);
        }
        
        return;
    }
    
    /* (non-Javadoc)
     * @see jp.zippyzip.GeneratorService#updateZips()
     */
    public void updateZips() {
        
        Date timestamp = getLzhDao().getZipInfo().getTimestamp();
        LinkedList<Pref> prefs = getPrefs();
        TreeMap<String, TreeSet<String>> zips =
            new TreeMap<String, TreeSet<String>>();
        int cnt = 0;
        
        try {
            
            for (Pref pref : prefs) {
                
                ParentChild data = getParentChildDao(
                        ).get("pre" + pref.getCode());
                
                if (data != null) {
                    
                    for (String json : data.getChildren()) {
                        
                        JSONArray ja = new JSONArray(json);
                        
                        for (int i = 0; i < ja.length(); ++i) {
                        
                            JSONObject jo = ja.getJSONObject(i);
                            String zip = jo.optString("zip", "");
                            String key = jo.optString("key", "");
    
                            if (!zips.containsKey(zip)) {
                                zips.put(zip, new TreeSet<String>());
                            }
                            
                            zips.get(zip).add(key);
                        }
                    }
                }
            }
            
            ParentChild data = null;
            LinkedList<String> zip1s = new LinkedList<String>();
            String prev = null;
            
            for (String code : zips.keySet()) {
                
                String zip1 = code.substring(0, 3);
                String zip2 = code.substring(3);
                
                if (!zip1.equals(prev)) {
                    
                    if (data != null) {
                        getParentChildDao().store(data);
                    }
                    
                    LinkedList<String> parents = new LinkedList<String>();
                    
                    parents.add(zip1);
                    data = new ParentChild(zip1, timestamp, parents);
                    zip1s.add(new JSONStringer().object()
                            .key("zip1").value(zip1)
                        .endObject().toString());
                    prev = zip1;
                }
                
                for (String key : zips.get(code)) {
                
                    data.getChildren().add(new JSONStringer().object()
                            .key("zip2").value(zip2)
                            .key("key").value(key)
                        .endObject().toString());
                    ++cnt;
                }
            }
            
            if (data != null) {
                getParentChildDao().store(data);
            }
            
            getParentChildDao().store(new ParentChild(
                    "zip1s", timestamp, new LinkedList<String>(), zip1s));
            
            log.info("count:" + cnt);
            
        } catch (JSONException e) {
            log.log(Level.WARNING, "", e);
        }
        
        return;
    }
    
    /* (non-Javadoc)
     * @see jp.zippyzip.GeneratorService#updateBuilding()
     */
    public void updateBuilding() {
        
        Collection<String> keys = getParentChildDao().getKeys();
        LinkedList<String> list = new LinkedList<String>();
        String preName = "";
        String preYomi = "";
        
        for (String key : keys) {
            
            if (key.length() != 5) { continue; }
            
            ParentChild data = getParentChildDao().get(key);
            
            for (String json : data.getChildren()) {

                if (!json.startsWith("{")) { continue; }

                Zip zip = Zip.fromJson(json);

                if (zip.getAdd2() == null) { continue; }
                if ((zip.getNote() != null)
                        && zip.getNote().equals("次のビルを除く")) {
                    preName = zip.getAdd1();
                    preYomi = zip.getAdd1Yomi();
                }
                if (!zip.getAdd2().equals("階層不明")) { continue; }
                
                if (zip.getAdd1().startsWith(preName)) {
                    zip.setAdd1(preName + " "
                            + zip.getAdd1().substring(preName.length()));
                }
                if (zip.getAdd1Yomi().startsWith(preYomi)) {
                    zip.setAdd1Yomi(preYomi + " "
                            + zip.getAdd1Yomi().substring(preYomi.length()));
                }
                
                list.add(zip.toJson());
            }
        }
        
        log.info("count:" + list.size());
        getParentChildDao().store(new ParentChild(
                "building", new Date(), new LinkedList<String>(), list));
    }
    
    /**
     * 都道府県のリストを取得する。
     * 
     * @return 都道府県のリスト
     */
    LinkedList<Pref> getPrefs() {
        
        LinkedList<Pref> ret = new LinkedList<Pref>();
        
        for (String json : getParentChildDao().get("prefs").getChildren()) {
            ret.add(Pref.fromJson(json));
        }
        
        return ret;
    }
    
    /**
     * 都道府県のリストを取得する。
     * 
     * @return 都道府県のリスト
     */
    SortedMap<String, Pref> getPrefMap() {
        
        SortedMap<String, Pref> ret = new TreeMap<String, Pref>();
        
        for (String json : getParentChildDao().get("prefs").getChildren()) {
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
    SortedMap<String, City> getCityMap() {
        
        SortedMap<String, City> ret = new TreeMap<String, City>();
        
        for (Pref pref : getPrefs()) {
            
            ParentChild pc = getParentChildDao().get(pref.getCode());

            if (pc == null) { continue; }
            
            for (String json : pc.getChildren()) {
                
                City city = City.fromJson(json);

                ret.put(city.getCode(), city);
            }
        }
        
        return ret;
    }
    
    /**
     * 市区町村のリストを取得する。
     * 
     * @return 市区町村のリスト
     */
    LinkedList<City> getCities() {
        
        LinkedList<City> ret = new LinkedList<City>();
        
        for (Pref pref : getPrefs()) {
            
            ParentChild pc = getParentChildDao().get(pref.getCode());

            if (pc == null) { continue; }
            
            for (String json : pc.getChildren()) {

                ret.add(City.fromJson(json));
            }
        }
        
        return ret;
    }

    /* (non-Javadoc)
     * @see jp.zippyzip.GeneratorService#storeX0401Zip()
     */
    public void storeX0401Zip() {
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        long timestamp = getLzhDao().getZipInfo().getTimestamp().getTime();
        ZipOutputStream out = new ZipOutputStream(baos);
        Collection<Pref> prefs = getPrefs();
        ZipEntry tsv = new ZipEntry(FILENAME_DATE_FORMAT.format(
                new Date(timestamp)) + "_x0401_utf8.txt");
        ZipEntry csv = new ZipEntry(FILENAME_DATE_FORMAT.format(
                new Date(timestamp)) + "_x0401_sjis.csv");
        ZipEntry json = new ZipEntry(FILENAME_DATE_FORMAT.format(
                new Date(timestamp)) + "_x0401_utf8.json");
        ZipEntry xml = new ZipEntry(FILENAME_DATE_FORMAT.format(
                new Date(timestamp)) + "_x0401_utf8.xml");
        
        tsv.setTime(timestamp);
        csv.setTime(timestamp);
        json.setTime(timestamp);
        xml.setTime(timestamp);
        
        try {
            
            out.putNextEntry(tsv);
            
            for (Pref pref : prefs) {
               
                out.write(new String(
                        pref.getCode() + "\t"
                        + pref.getName() + "\t"
                        + pref.getYomi() + CRLF
                    ).getBytes("UTF-8")
                );
            }
            
            out.closeEntry();
            out.putNextEntry(csv);
            
            for (Pref pref : prefs) {
                
                out.write(new String(
                        pref.getCode() + ","
                        + pref.getName()
                        + "," + pref.getYomi() + CRLF
                    ).getBytes("MS932")
                );
            }
            
            out.closeEntry();
            out.putNextEntry(json);

            OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8");
            JSONWriter jwriter = new JSONWriter(writer);
            
            jwriter.array();

            for (Pref pref : prefs) {

                jwriter.object()
                    .key("code").value(pref.getCode())
                    .key("name").value(pref.getName())
                    .key("yomi").value(pref.getYomi())
                .endObject();
            }
            
            jwriter.endArray();
            writer.flush();
            out.closeEntry();
            out.putNextEntry(xml);

            XMLStreamWriter xwriter =
                XMLOutputFactory.newInstance().createXMLStreamWriter(
                            new OutputStreamWriter(out, "UTF-8"));
            
            xwriter.writeStartDocument("UTF-8", "1.0");
            xwriter.writeStartElement("x0401s");
        
            for (Pref pref : prefs) {
        
                xwriter.writeStartElement("x0401");
                xwriter.writeAttribute("code", pref.getCode());
                xwriter.writeAttribute("name", pref.getName());
                xwriter.writeAttribute("yomi", pref.getYomi());
                xwriter.writeEndElement();
            }
            
            xwriter.writeEndElement();
            xwriter.writeEndDocument();
            xwriter.flush();
            out.closeEntry();
            out.finish();            
            baos.flush();
            getRawDao().store(baos.toByteArray(), "x0401.zip");
            log.info("prefs: " + prefs.size());
            
        } catch (JSONException e) {
            log.log(Level.WARNING, "", e);
        } catch (XMLStreamException e) {
            log.log(Level.WARNING, "", e);
        } catch (FactoryConfigurationError e) {
            log.log(Level.WARNING, "", e);
        } catch (IOException e) {
            log.log(Level.WARNING, "", e);
        }
    }

    /* (non-Javadoc)
     * @see jp.zippyzip.GeneratorService#storeX0402Zip()
     */
    public void storeX0402Zip() {
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        long timestamp = getLzhDao().getZipInfo().getTimestamp().getTime();
        ZipOutputStream out = new ZipOutputStream(baos);
        Collection<City> cities = getCities();
        ZipEntry tsv = new ZipEntry(FILENAME_DATE_FORMAT.format(
                new Date(timestamp)) + "_x0402_utf8.txt");
        ZipEntry csv = new ZipEntry(FILENAME_DATE_FORMAT.format(
                new Date(timestamp)) + "_x0402_sjis.csv");
        ZipEntry json = new ZipEntry(FILENAME_DATE_FORMAT.format(
                new Date(timestamp)) + "_x0402_utf8.json");
        ZipEntry xml = new ZipEntry(FILENAME_DATE_FORMAT.format(
                new Date(timestamp)) + "_x0402_utf8.xml");
        
        tsv.setTime(timestamp);
        csv.setTime(timestamp);
        json.setTime(timestamp);
        xml.setTime(timestamp);
        
        try {
            
            out.putNextEntry(tsv);
            
            for (City city : cities) {
                
                out.write(new String(
                        city.getCode() + "\t"
                        + city.getName() + "\t"
                        + city.getYomi() + "\t"
                        + ((city.getExpiration().getTime()
                                < new Date().getTime()) ? "旧" : "")
                        + CRLF
                    ).getBytes("UTF-8")
                );
            }
            
            out.closeEntry();
            out.putNextEntry(csv);
            
            for (City city : cities) {
                
                out.write(new String(
                        city.getCode() + ","
                        + city.getName() + ","
                        + city.getYomi() + ","
                        + ((city.getExpiration().getTime()
                                < new Date().getTime()) ? "旧" : "")
                        + CRLF
                    ).getBytes("MS932")
                );
            }
            
            out.closeEntry();
            out.putNextEntry(json);

            OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8");
            JSONWriter jwriter = new JSONWriter(writer);
            
            jwriter.array();

            for (City city : cities) {

                jwriter.object()
                    .key("code").value(city.getCode())
                    .key("name").value(city.getName())
                    .key("yomi").value(city.getYomi())
                    .key("expired").value((city.getExpiration().getTime()
                            < new Date().getTime()) ? "true" : "false")
                .endObject();
            }
            
            jwriter.endArray();
            writer.flush();
            out.closeEntry();
            out.putNextEntry(xml);

            XMLStreamWriter xwriter =
                XMLOutputFactory.newInstance().createXMLStreamWriter(
                        new OutputStreamWriter(out, "UTF-8"));
        
            xwriter.writeStartDocument("UTF-8", "1.0");
            xwriter.writeStartElement("x0402s");
        
            for (City city : cities) {
        
                xwriter.writeStartElement("x0402");
                xwriter.writeAttribute("code", city.getCode());
                xwriter.writeAttribute("name", city.getName());
                xwriter.writeAttribute("yomi", city.getYomi());
                xwriter.writeAttribute("expired",
                        (city.getExpiration().getTime()
                                < new Date().getTime()) ? "true" : "false");
                xwriter.writeEndElement();
            }
            
            xwriter.writeEndElement();
            xwriter.writeEndDocument();
            xwriter.flush();
            out.closeEntry();
            out.finish();
            baos.flush();
            getRawDao().store(baos.toByteArray(), "x0402.zip");
            log.info("cities: " + cities.size());
            
        } catch (JSONException e) {
            log.log(Level.WARNING, "", e);
        } catch (XMLStreamException e) {
            log.log(Level.WARNING, "", e);
        } catch (FactoryConfigurationError e) {
            log.log(Level.WARNING, "", e);
        } catch (IOException e) {
            log.log(Level.WARNING, "", e);
        }
    }
    
    /* (non-Javadoc)
     * @see jp.zippyzip.GeneratorService#storeAreaText()
     */
    public void storeAreaText() {
        storeText("\t", "area", "", "UTF-8", "utf8", "txt");
    }
    
    /* (non-Javadoc)
     * @see jp.zippyzip.GeneratorService#storeAreaCsv()
     */
    public void storeAreaCsv() {
        storeText(",", "area", "", "MS932", "sjis", "csv");
    }
    
    /* (non-Javadoc)
     * @see jp.zippyzip.GeneratorService#storeAreaJson()
     */
    public void storeAreaJson() {
        storeJson("area", "");
    }
    
    /* (non-Javadoc)
     * @see jp.zippyzip.GeneratorService#storeAreaXml()
     */
    public void storeAreaXml() {
        storeXml("area", "");
    }
    
    /* (non-Javadoc)
     * @see jp.zippyzip.GeneratorService#storeAreaIme()
     */
    public void storeAreaIme() {
        storeIme("area", "");
    }
    
    /* (non-Javadoc)
     * @see jp.zippyzip.GeneratorService#storeCorpText()
     */
    public void storeCorpText() {
        storeText("\t", "corp", "c", "UTF-8", "utf8", "txt");
    }
    
    /* (non-Javadoc)
     * @see jp.zippyzip.GeneratorService#storeCorpCsv()
     */
    public void storeCorpCsv() {
        storeText(",", "corp", "c", "MS932", "sjis", "csv");
    }
    
    /* (non-Javadoc)
     * @see jp.zippyzip.GeneratorService#storeCorpJson()
     */
    public void storeCorpJson() {
        storeJson("corp", "c");
    }

    /* (non-Javadoc)
     * @see jp.zippyzip.GeneratorService#storeCorpXml()
     */
    public void storeCorpXml() {
        storeXml("corp", "c");
    }
    
    /* (non-Javadoc)
     * @see jp.zippyzip.GeneratorService#storeCorpIme()
     */
    public void storeCorpIme() {
        storeIme("corp", "c");
    }
    
    /**
     * テキストデータを保存する。
     * 
     * @param sep "\t" / ","
     * @param name "area" / "corp"
     * @param suffix "" / "c"
     * @param chaset "UTF-8" / "MS932"
     * @param enc "utf8" / "sjis"
     * @param ext "txt" / "csv"
     */
    void storeText(String sep, String name, String suffix, String chaset, String enc, String ext) {
        
        long timestamp = getLzhDao().getZipInfo().getTimestamp().getTime();
        Collection<City> cities = getCities();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        ZipEntry entry = new ZipEntry(FILENAME_DATE_FORMAT.format(
                new Date(timestamp)) + "_" + name + "_" + enc + "." + ext);
        entry.setTime(timestamp);
        int cnt = 0;
        
        try {
            
            byte[] tab = sep.getBytes("MS932");
            byte[] crlf = CRLF.getBytes("MS932");
            
            zos.putNextEntry(entry);
            
            for (City city : cities) {
                
                ParentChild pc = getParentChildDao().get(city.getCode() + suffix);
                
                if (pc == null) { continue; }
                
                for (String json : pc.getChildren()) {
                    
                    Zip zip = Zip.fromJson(json);
                    zos.write(zip.getCode().getBytes(chaset));
                    zos.write(tab);
                    zos.write(zip.getX0402().getBytes(chaset));
                    zos.write(tab);
                    zos.write(zip.getAdd1().getBytes(chaset));
                    zos.write(tab);
                    zos.write(zip.getAdd2().getBytes(chaset));
                    zos.write(tab);
                    zos.write(zip.getCorp().getBytes(chaset));
                    zos.write(tab);
                    zos.write(zip.getAdd1Yomi().getBytes(chaset));
                    zos.write(tab);
                    zos.write(zip.getAdd2Yomi().getBytes(chaset));
                    zos.write(tab);
                    zos.write(zip.getCorpYomi().getBytes(chaset));
                    zos.write(tab);
                    zos.write(zip.getNote().getBytes(chaset));
                    zos.write(crlf);
                    ++cnt;
                }
            }
            
            zos.closeEntry();
            zos.finish();
            getRawDao().store(baos.toByteArray(), name + "_" + enc + "_" + ext + ".zip");
            log.info("count: " + cnt);
                        
        } catch (IOException e) {
            log.log(Level.WARNING, "", e);
        }
    }
    
    /**
     * JSON データを保存する。
     * 
     * @param name "area" / "corp"
     * @param suffix "" / "c"
     */
    void storeJson(String name, String suffix) {
        
        long timestamp = getLzhDao().getZipInfo().getTimestamp().getTime();
        Collection<City> cities = getCities();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        ZipEntry entry = new ZipEntry(FILENAME_DATE_FORMAT.format(
                new Date(timestamp)) + "_" + name + "_utf8.json");
        entry.setTime(timestamp);
        int cnt = 0;
        
        try {
            
            zos.putNextEntry(entry);
            zos.write("[".getBytes("UTF-8"));
            
            for (City city : cities) {
                
                ParentChild pc = getParentChildDao().get(city.getCode() + suffix);
                
                if (pc == null) { continue; }
                
                for (String json : pc.getChildren()) {

                    if (0 < cnt) { zos.write(",".getBytes("UTF-8")); }
                    zos.write(json.getBytes("UTF-8"));
                    ++cnt;
                }
            }
            
            zos.write("]".getBytes("UTF-8"));
            zos.closeEntry();
            zos.finish();
            getRawDao().store(baos.toByteArray(), name + "_utf8_json.zip");
            log.info("count: " + cnt);
                        
        } catch (IOException e) {
            log.log(Level.WARNING, "", e);
        }
    }
    
    /**
     * XML データを保存する。
     * 
     * @param name "area" / "corp"
     * @param suffix "" / "c"
     */
    void storeXml(String name, String suffix) {
        
        long timestamp = getLzhDao().getZipInfo().getTimestamp().getTime();
        Collection<City> cities = getCities();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        ZipEntry entry = new ZipEntry(FILENAME_DATE_FORMAT.format(
                new Date(timestamp)) + "_" + name + "_utf8.xml");
        entry.setTime(timestamp);
        int cnt = 0;
        
        try {
            
            zos.putNextEntry(entry);

            OutputStreamWriter writer = new OutputStreamWriter(zos, "UTF-8");
            XMLStreamWriter xwriter = XMLOutputFactory.newInstance(
                    ).createXMLStreamWriter(writer);
            
            xwriter.writeStartDocument("UTF-8", "1.0");
            xwriter.writeStartElement("zips");
            xwriter.writeAttribute("type", name);
            
            for (City city : cities) {
                
                ParentChild pc = getParentChildDao().get(city.getCode() + suffix);
                
                if (pc == null) { continue; }
                
                for (String json : pc.getChildren()) {

                    Zip zip = Zip.fromJson(json);
                    
                    xwriter.writeStartElement("zip");
                    xwriter.writeAttribute("zip"     , zip.getCode());
                    xwriter.writeAttribute("x0402"   , zip.getX0402());
                    xwriter.writeAttribute("add1"    , zip.getAdd1());
                    xwriter.writeAttribute("add2"    , zip.getAdd2());
                    xwriter.writeAttribute("corp"    , zip.getCorp());
                    xwriter.writeAttribute("add1Yomi", zip.getAdd1Yomi());
                    xwriter.writeAttribute("add2Yomi", zip.getAdd2Yomi());
                    xwriter.writeAttribute("corpYomi", zip.getCorpYomi());
                    xwriter.writeAttribute("note"    , zip.getNote());
                    xwriter.writeEndElement();
                    ++cnt;
                }
            }
            
            xwriter.writeEndElement();
            xwriter.writeEndDocument();
            xwriter.flush();
            zos.closeEntry();
            zos.finish();
            getRawDao().store(baos.toByteArray(), name + "_utf8_xml.zip");
            log.info("count: " + cnt);
                        
        } catch (XMLStreamException e) {
            log.log(Level.WARNING, "", e);
        } catch (FactoryConfigurationError e) {
            log.log(Level.WARNING, "", e);
        } catch (IOException e) {
            log.log(Level.WARNING, "", e);
        }
    }
    
    /**
     * IME用辞書データを保存する。
     * 
     * @param name "area" / "corp"
     * @param suffix "" / "c"
     */
    void storeIme(String name, String suffix) {
        
        long timestamp = getLzhDao().getZipInfo().getTimestamp().getTime();
        SortedMap<String, Pref> prefs = getPrefMap();
        Collection<City> cities = getCities();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        ZipEntry entry = new ZipEntry(FILENAME_DATE_FORMAT.format(
                new Date(timestamp)) + "_" + name + "_ime_dic.txt");
        entry.setTime(timestamp);
        int cnt = 0;
        
        try {
            
            byte[] tab = "\t".getBytes("MS932");
            byte[] sonota = "\t地名その他".getBytes("MS932");
            byte[] crlf = CRLF.getBytes("MS932");
            
            zos.putNextEntry(entry);
            
            for (City city : cities) {
                
                ParentChild pc = getParentChildDao().get(city.getCode() + suffix);
                
                if (pc == null) { continue; }
                
                String prefName = prefs.get(city.getCode().substring(0, 2)
                        ).getName();
                String cityName = city.getName();
                
                for (String json : pc.getChildren()) {

                    Zip zip = Zip.fromJson(json);
                    zos.write(ZenHanHelper.convertZipHankakuZenkaku(
                            zip.getCode()).getBytes("MS932"));
                    zos.write(tab);
                    zos.write(prefName.getBytes("MS932"));
                    zos.write(cityName.getBytes("MS932"));
                    zos.write(zip.getAdd1().getBytes("MS932"));
                    zos.write(zip.getAdd2().getBytes("MS932"));
                    zos.write(zip.getCorp().getBytes("MS932"));
                    zos.write(sonota);
                    zos.write(crlf);
                    ++cnt;
                }
            }
            
            zos.closeEntry();
            zos.finish();
            getRawDao().store(baos.toByteArray(), name + "_ime_dic.zip");
            log.info("count: " + cnt);
                        
        } catch (IOException e) {
            log.log(Level.WARNING, "", e);
        }
    }
    
    /* (non-Javadoc)
     * @see jp.zippyzip.GeneratorService#storeJsonPrefCity()
     */
    public void storeJsonPrefCity() {
        
        long timestamp = getLzhDao().getZipInfo().getTimestamp().getTime();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        int cnt = 0;
        
        try {
            
            ParentChild data = getParentChildDao().get("prefs");
            String json = toJsonPrefs(data);
            ZipEntry entry = new ZipEntry("prefs.json");
            entry.setTime(timestamp);
            zos.putNextEntry(entry);
            zos.write(json.getBytes("UTF-8"));
            zos.closeEntry();
            ++cnt;
            
            for (Pref pref : getPrefs()) {
                
                data = getParentChildDao().get(pref.getCode());
                
                if (data == null) { continue; }
                
                json = toJsonCities(data);
                entry = new ZipEntry(pref.getCode() + ".json");
                entry.setTime(timestamp);
                zos.putNextEntry(entry);
                zos.write(json.getBytes("UTF-8"));
                zos.closeEntry();
                ++cnt;
            }
            
            zos.finish();
            getRawDao().store(baos.toByteArray(), "json_prefcity.zip");
            log.info("count:" + cnt);
        
        } catch (JSONException e) {
            log.log(Level.WARNING, "", e);
        } catch (IOException e) {
            log.log(Level.WARNING, "", e);
        }
    }
    
    /* (non-Javadoc)
     * @see jp.zippyzip.GeneratorService#storeJsonArea()
     */
    public void storeJsonArea() {
        
        LinkedList<City> cities = getCities();
        long timestamp = getLzhDao().getZipInfo().getTimestamp().getTime();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        int cnt = 0;
        
        try {
            
            for (City city : cities) {
                
                ParentChild data = getParentChildDao().get(city.getCode());
                
                if (data == null) { continue; }
                
                SortedSet<String> children = new TreeSet<String>();
                String json = toJsonAdd1s(data, children);
                ZipEntry entry = new ZipEntry(city.getCode() + ".json");
                entry.setTime(timestamp);
                zos.putNextEntry(entry);
                zos.write(json.getBytes("UTF-8"));
                zos.closeEntry();
                ++cnt;
                
                for (String add1 : children) {
                    
                    json = toJsonAdd2s(data, add1);
                    entry = new ZipEntry(
                            city.getCode() + "-" + toHex(add1) + ".json");
                    entry.setTime(timestamp);
                    zos.putNextEntry(entry);
                    zos.write(json.getBytes("UTF-8"));
                    zos.closeEntry();
                    ++cnt;
                }
            }
            
            zos.finish();
            getRawDao().store(baos.toByteArray(), "json_area.zip");
            log.info("count:" + cnt);
            
        } catch (JSONException e) {
            log.log(Level.WARNING, "", e);
        } catch (IOException e) {
            log.log(Level.WARNING, "", e);
        }
        
        return;
    }
    
    /*
     * (non-Javadoc)
     * @see jp.zippyzip.GeneratorService#storeJsonCorp()
     */
    public void storeJsonCorp() {
        
        LinkedList<City> cities = getCities();
        long timestamp = getLzhDao().getZipInfo().getTimestamp().getTime();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        int cnt = 0;
        
        try {
            
            for (City city : cities) {
                
                String key = city.getCode() + "c";
                ParentChild data = getParentChildDao().get(key);
                
                if (data == null) { continue; }
                
                String json = toJsonCorps(data);
                ZipEntry entry = new ZipEntry(key + ".json");
                entry.setTime(timestamp);
                zos.putNextEntry(entry);
                zos.write(json.getBytes("UTF-8"));
                zos.closeEntry();
                ++cnt;
            }
            
            zos.finish();
            getRawDao().store(baos.toByteArray(), "json_corp.zip");
            log.info("count:" + cnt);
            
        } catch (JSONException e) {
            log.log(Level.WARNING, "", e);
        } catch (IOException e) {
            log.log(Level.WARNING, "", e);
        }
        
        return;
    }
    
    /* (non-Javadoc)
     * @see jp.zippyzip.GeneratorService#storeJsonZips(java.lang.String)
     */
    public String storeJsonZips(String digits) {

        if ((digits == null) || (digits.length() == 0)) { digits = "0"; }
        long timestamp = getLzhDao().getZipInfo().getTimestamp().getTime();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        int cnt = 0;
        
        try {
            
            for (int i = 0; i < 100; ++i) {
                
                String zip1 = digits + String.format("%02d", i);
                ParentChild data = getParentChildDao().get(zip1);
                
                if (data == null) { continue; }

                Map<String, ParentChild> pcs =
                    new HashMap<String, ParentChild>();
                SortedSet<String> zip2s = new TreeSet<String>();
                
                for (String json : data.getChildren()) {
                    
                    JSONObject jo = new JSONObject(json);
                    String key = jo.optString("key", "");
                    String zip2 = jo.optString("zip2", "");
                    
                    if (key.length() == 0) { continue; }
                    if (zip2.length() == 0) { continue; }
                    
                    if (!pcs.containsKey(jo.optString("key", ""))) {
                        pcs.put(key, getParentChildDao().get(key));
                    }
                    
                    zip2s.add(zip2);
                }
                
                for (String zip2: zip2s) {
                    
                    String zip = zip1 + zip2;
                    String json = toJsonZips(data, zip, pcs);
                    ZipEntry entry = new ZipEntry(zip + ".json");
                    entry.setTime(timestamp);
                    zos.putNextEntry(entry);
                    zos.write(json.getBytes("UTF-8"));
                    zos.closeEntry();
                    ++cnt;
                }
            }
            
            if (cnt == 0) {
                
                ZipEntry entry = new ZipEntry("empty.txt");
                entry.setTime(timestamp);
                zos.putNextEntry(entry);
                zos.write("empty".getBytes("UTF-8"));
                zos.closeEntry();
            }
            
            zos.finish();
            getRawDao().store(baos.toByteArray(), "json_zip" + digits + ".zip");
            log.info(digits + ":" + cnt);
            
        } catch (JSONException e) {
            log.log(Level.WARNING, "", e);
        } catch (IOException e) {
            log.log(Level.WARNING, "", e);
        }

        return digits.equals("9") ? null : "" + (Integer.parseInt(digits) + 1);
    }
    
    /* (non-Javadoc)
     * @see jp.zippyzip.GeneratorService#complete()
     */
    public void complete() {

        ZipInfo zipInfo = getLzhDao().getZipInfo();

        zipInfo.setGenerated(new Date());
        getLzhDao().store(zipInfo);
        log.info("Complete.");
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
    
    String toJsonAdd1s(ParentChild data, SortedSet<String> children
            ) throws JSONException {
        
        LinkedList<String> zips = new LinkedList<String>();
        Set<String> add1s = new HashSet<String>();
        Set<String> add2s = new HashSet<String>();
        
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
                
                if (!children.contains(add1)) {
                    
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
                    children.add(add1);
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
    
    String toJsonAdd2s(ParentChild data, String name
            ) throws JSONException {
        
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
        ret.append(",\"add1\":{\"code\":\"");
        ret.append(toHex(name));
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
    
    String toJsonZips(ParentChild data, String zip,
            Map<String, ParentChild> pcs) throws JSONException {
        
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
                
                ParentChild zips = pcs.get(key);
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
}
