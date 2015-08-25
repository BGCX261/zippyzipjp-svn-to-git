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
package jp.zippyzip;

import java.sql.Timestamp;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

/**
 * 市区町村のエンティティ。
 * 
 * @author Michinobu Maeda
 */
public class City {

    /** ログ　*/
    protected static Logger log = Logger.getLogger(City.class.getName());
    
    /** 日付の最大値 */
    public static final Timestamp TIMESTAMP_MAX = Timestamp.valueOf("2999-12-31 23:59:59");

    /** コード */
    private String code;

    /** 名称 */
    private String name;

    /** 読み */
    private String yomi;
    
    /** 有効期限 */
    private Date expiration;

    /**
     * JSON からオブジェクトを生成する。
     * 
     * @param json JSON
     * @return オブジェクト
     */
    public static City fromJson(String json) {
        
        City ret = null;
        
        try {
            
            JSONObject jo = new JSONObject(json);
            
            ret = new City(
                    jo.optString("code", ""),
                    jo.optString("name", ""),
                    jo.optString("yomi", ""),
                    Timestamp.valueOf(jo.optString("expiration", ""))
                );
            
        } catch (JSONException e) {
            log.log(Level.WARNING, "", e);
        }
        
        return ret;
    }
    
    /**
     * デフォルトコンストラクタ。
     */
    public City() {
        this("", "", "", TIMESTAMP_MAX);
    }
    
    /**
     * プロパティで初期化する。
     * 
     * @param code コード
     * @param name 名称
     * @param yomi 読み
     */
    public City(String code, String name, String yomi) {
        this(code, name, yomi, TIMESTAMP_MAX);
    }
    
    /**
     * プロパティで初期化する。
     * 
     * @param code コード
     * @param name 名称
     * @param yomi 読み
     * @param expiration 有効期限
     */
    public City(String code, String name, String yomi, Date expiration) {
        
        this.code = code;
        this.name = name;
        this.yomi = yomi;
        setExpiration(expiration);
    }
    
    /**
     * プロパティで初期化する。
     * 
     * @param code コード
     * @param name 名称
     * @param yomi 読み
     * @param expiration 有効期限
     */
    public City(String code, String name, String yomi, long expiration) {
        
        this.code = code;
        this.name = name;
        this.yomi = yomi;
        setExpiration(expiration);
    }

    /**
     * コードを取得する。
     * 
     * @return コード
     */
    public String getCode() {
        return code;
    }

    /**
     * コードを設定する。
     * 
     * @param code コード
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * 名称を取得する。
     * 
     * @return 名称
     */
    public String getName() {
        return name;
    }

    /**
     * 名称を設定する。
     * 
     * @param name 名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 読みを取得する。
     * 
     * @return 読み
     */
    public String getYomi() {
        return yomi;
    }

    /**
     * 読みを設定する。
     * 
     * @param yomi 読み
     */
    public void setYomi(String yomi) {
        this.yomi = yomi;
    }

    /**
     * 有効期限を取得する。
     * 
     * @return 有効期限
     */
    public Date getExpiration() {
        return expiration;
    }

    /**
     * 有効期限を設定する。
     * 
     * @param expiration 有効期限
     */
    public void setExpiration(Date expiration) {
        this.expiration = new Timestamp(expiration.getTime());
    }

    /**
     * 有効期限を設定する。
     * 
     * @param expiration 有効期限
     */
    public void setExpiration(long expiration) {
        this.expiration = new Timestamp(expiration);
    }
    
    /**
     * JSON を取得する。
     * 
     * @returnJSON
     */
    public String toJson() {
        
        String ret = null;
        
        try {
            
            ret = new JSONStringer().object()
                .key("code").value(code)
                .key("name").value(name)
                .key("yomi").value(yomi)
                .key("expiration").value(expiration.toString())
            .endObject().toString();
            
        } catch (JSONException e) {
            log.log(Level.WARNING, "", e);
        }
        
        return ret;
    }
}

