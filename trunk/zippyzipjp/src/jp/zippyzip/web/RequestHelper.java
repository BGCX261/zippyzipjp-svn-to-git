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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

/**
 * アップロードされたファイルを処理するためのヘルパ。
 * 
 * @author Michinobu Maeda
 */
public class RequestHelper {

    /** ログ　*/
    protected static Logger log = Logger.getLogger(RequestHelper.class.getName());
    
    /** このアプリケーションの標準の SQL で使われることが多い日付フォーマット */
    public static SimpleDateFormat SQL_DATE_FORMAT =
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    /** ファイル名で使われることが多い日付フォーマット */
    public static SimpleDateFormat FILENAME_DATE_FORMAT =
        new SimpleDateFormat("yyyyMMddHHmmss");
    
    /**
     * 文字列としてパラメタ値を取得する。
     * 
     * @param request HTTP リクエスト
     * @param name パラメタ名
     * @return 値
     */
    public static String getStringParam(
            HttpServletRequest request, String name) {
                
        return getFirstValue(request, name);
    }
    
    /**
     * 整数としてパラメタ値を取得する。
     * 
     * @param request HTTP リクエスト
     * @param name パラメタ名
     * @return 値
     */
    public static Integer getIntParam(
            HttpServletRequest request, String name) {
        
        Integer ret = null;
        String str = getFirstValue(request, name);
        
        try {
            ret = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            log.log(Level.WARNING, "", e);
        }
        
        return ret;
    }
    
    /**
     * boolean としてパラメタ値を取得する。
     * 
     * @param request HTTP リクエスト
     * @param name パラメタ名
     * @return 値
     */
    public static Boolean getBooleanParam(
            HttpServletRequest request, String name) {
        
        return Boolean.parseBoolean(getFirstValue(request, name));
    }
    
    /**
     * 日時としてパラメタ値を取得する。
     * 
     * @param request HTTP リクエスト
     * @param name パラメタ名
     * @return 値
     */
    public static Date getDateParam(
            HttpServletRequest request, String name) {
        
        Date ret = null;
        String str = getFirstValue(request, name);
        
        try {
            ret = SQL_DATE_FORMAT.parse(str);
        } catch (ParseException e) {
            log.log(Level.WARNING, "", e);
       }
        
        return ret;
    }
    
    /**
     * 日時としてパラメタ値を取得する。
     * 
     * @param request HTTP リクエスト
     * @param name パラメタ名
     * @return 値
     */
    public static Date getDateParam(
            HttpServletRequest request, String name,
            SimpleDateFormat format) {
        
        Date ret = null;
        String str = getFirstValue(request, name);
        
        try {
            ret = format.parse(str);
        } catch (ParseException e) {
            log.log(Level.WARNING, "", e);
        }
        
        return ret;
    }

    /**
     * 先頭のパラメタ値を取得する。
     * 
     * @param request HTTP リクエスト
     * @param name パラメタ名
     * @return 値
     */
    static String getFirstValue(
            HttpServletRequest request, String name) {
        
        String ret = null;
        
        if (containsName(request, name)) {
            
            String[] values = request.getParameterValues(name);
            
            if ((values != null) && (0 < values.length)) {
                
                ret = values[0];
            }
        }
        
        return ret;
    }

    /**
     * パラメタの有無を取得する。
     * 
     * @param request HTTP リクエスト
     * @param name パラメタ名
     * @return パラメタの有無
     */
    static boolean containsName(
            HttpServletRequest request, String name) {
        
        boolean ret = false;
        
        @SuppressWarnings("rawtypes")
        Enumeration names =  request.getParameterNames();
        
        while (names.hasMoreElements()) {
            
            if (!names.nextElement().equals(name)) { continue; }
            ret = true;
        }
        
        return ret;
    }
    
    /**
     * ファイルのデータの先頭位置を取得する。
     * 
     * @param buff データ
     * @param size データのサイズ
     * @param boun バウンダリ
     * @param fileUploadKey パラメタ名
     * @return 先頭位置
     */
    public static int getOffset(byte[] buff, int size, byte[] boun, String fileUploadKey) {
        
        int ret = 0;
        int next = 0;
        
        while (0 <= next) {
        
            int headerStart = boun.length;
            int headerEnd = getHeaderEnd(buff, headerStart, size);
            
            if (headerEnd <= headerStart) { break; }
            
            next = getNextBoundery(buff, headerStart, size, boun);
            
            if (next < 0) { break; }
            
            String header = new String(buff, headerStart, headerEnd - headerStart
                    ).replaceAll("\n", "").replaceAll("\r", "");
            String filename = getHeaderAttr(header, "filename");
            String name = getHeaderAttr(header, "name");
            
            if ((filename == null) || (name == null)
                    || (!filename.toLowerCase().endsWith(".lzh"))
                    || (!name.equals(fileUploadKey))) {
                
                size -= next;
                for (int k = 0; k < size; ++k) { buff[k] = buff[k + headerEnd]; }
                continue;
            }
            
            ret = headerEnd;
            break;
        }
        
        return ret;
    }
    
    /**
     * バウンダリを取得する。
     * 
     * @param buff データ
     * @param size データのサイズ
     * @return バウンダリ
     */
    public static byte[] getFirstBoundery(byte[] buff, int size) {
        
        byte[] boun = null;
        
        for (int i = 0; i < size; ++i) {
                
            if ((buff[i] != (byte)13) && (buff[i] != (byte)10)) { continue; }                
            boun = new byte[i + 2];
            break;
        }
        
        if (boun == null) { return null; }
        
        for (int i = 0; i < boun.length - 2; ++i) { boun[i] = buff[i]; }
        boun[boun.length - 2] = (byte)'-';
        boun[boun.length - 1] = (byte)'-';
        
        return boun;
    }
    
    /**
     * 次のバウンダリを検索して、先頭位置を返す。
     * @param buff データ
     * @param start 検索開始位置
     * @param size データのサイズ
     * @param boun バウンダリ
     * @return バウンダリの先頭位置。バウダリが見つからない場合は -1 を返す。
     */
    public static int getNextBoundery(byte[] buff, int start, int size, byte[] boun) {
        
        int ret = -1;
        int offset = 0;
        boolean isBoundery = false;
        
        for (int k = start; k < size - boun.length; ++k) {
            
            if ((buff[k] == (byte)13) || (buff[k] == (byte)10)) {
                
                isBoundery = true;
                offset = 1;
                if ((buff[k] == (byte)13) || (buff[k + 1] == (byte)10)) {
                    ++ offset;
                }
                for (int m = 0; m < boun.length; ++m) {
                    if (boun[m] != buff[k + offset + m]) {
                        isBoundery = false;
                        break;
                    }
                }
                if (isBoundery) {
                    ret = k;
                    break;
                }
            }
        }
        
        return ret;
    }
    
    /**
     * ヘッダの終了位置を検索する。
     * 
     * @param buff データ
     * @param start 検索開始位置
     * @param size データのサイズ
     * @return ヘッダの終了位置。終了位置が見つからない場合は 0 を返す。
     */
    static int getHeaderEnd(byte[] buff, int start, int size) {
        
        int ret = 0;
        
        for (int i = start; i < size; ++i) {
            
            if ((buff[i] == (byte)13) || (buff[i] == (byte)10)) { continue; }
           
            if ((buff[i - 2] == (byte)10) && (buff[i - 1] == (byte)10)) {
                ret = i;
                break;
            }
            if ((buff[i - 2] == (byte)13) && (buff[i - 1] == (byte)13)) {
                ret = i;
                break;
            }
            if ((buff[i - 2] == (byte)10) && (buff[i - 1] == (byte)13)) {
                ret = i;
                break;
            }
            if ((buff[i - 2] == (byte)13) && (buff[i - 1] == (byte)10)) {
                if ((buff[i - 3] == (byte)13) || (buff[i - 3] == (byte)10)) {
                    ret = i;
                    break;
                }
            }
        }
        
        return ret;
    }

    /**
     * ヘッダに設定された属性の値を取得する。
     * 
     * @param header ヘッダ
     * @param name 属性の名称
     * @return 属性の値
     */
    static String getHeaderAttr(String header, String name) {
        
        if (!header.matches(".*\\s" + name + "=\".*")) {
            return null;
        }
        
        return header.replaceFirst(
                ".*\\s" + name + "=\"", "").replaceFirst("\".*", "");
    }

}
