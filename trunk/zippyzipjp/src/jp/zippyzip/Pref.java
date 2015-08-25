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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

/**
 * 都道府県のエンティティ。
 * 
 * @author Michinobu Maeda
 */
public class Pref {

    /** ログ　*/
    protected static Logger log = Logger.getLogger(Pref.class.getName());

    /** コード */
    private String code;

    /** 名称 */
    private String name;

    /** 読み */
    private String yomi;

    /**
     * JSON からオブジェクトを生成する。
     * 
     * @param json JSON
     * @return オブジェクト
     */
    public static Pref fromJson(String json) {
        
        Pref ret = null;
        
        try {
            
            JSONObject jo = new JSONObject(json);
            
            ret = new Pref(
                    jo.optString("code", ""),
                    jo.optString("name", ""),
                    jo.optString("yomi", "")
                );
            
        } catch (JSONException e) {
            log.log(Level.WARNING, "", e);
        }
        
        return ret;
    }

    /**
     * デフォルトコンストラクタ。
     */
    public Pref() {
        this("", "", "");
    }
    
    /**
     * プロパティで初期化する。
     * 
     * @param code コード
     * @param name 名称
     * @param yomi 読み
     */
    public Pref(String code, String name, String yomi) {
        
        this.code = code;
        this.name = name;
        this.yomi = yomi;
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
            .endObject().toString();
            
        } catch (JSONException e) {
            log.log(Level.WARNING, "", e);
        }
        
        return ret;
    }
}
