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
 * 郵便番号のエンティティ。
 * 
 * @author Michinobu Maeda
 */
public class Zip {

    /** ログ　*/
    protected static Logger log = Logger.getLogger(Zip.class.getName());

    /** コード */
    private String code;

    /** 市区町村コード */
    private String x0402;

    /** 住所１ */
    private String add1;

    /** 住所２ */
    private String add2;

    /** 事業所 */
    private String corp;

    /** 住所１読み */
    private String add1Yomi;

    /** 住所読み */
    private String add2Yomi;

    /** 事業所読み */
    private String corpYomi;

    /** 備考 */
    private String note;

    /**
     * JSON からオブジェクトを生成する。
     * 
     * @param json JSON
     * @return オブジェクト
     */
    public static Zip fromJson(String json) {
        
        Zip ret = null;
        
        try {
            
            JSONObject jo = new JSONObject(json);
            
            ret = new Zip(
                    jo.optString("code", ""),
                    jo.optString("x0402", ""),
                    jo.optString("add1", ""),
                    jo.optString("add2", ""),
                    jo.optString("corp", ""),
                    jo.optString("add1Yomi", ""),
                    jo.optString("add2Yomi", ""),
                    jo.optString("corpYomi", ""),
                    jo.optString("note", "")
                );
            
        } catch (JSONException e) {
            log.log(Level.WARNING, "", e);
        }
        
        return ret;
    }

    /**
     * デフォルトコンストラクタ。
     */
    public Zip() {
        this("", "", "", "", "", "", "", "", "");
    }
    
    /**
     * プロパティで初期化する。
     * 
     * @param code コード
     * @param x0402 市区町村コード
     * @param add1 住所１
     * @param add2 住所２
     * @param corp 事業所
     * @param corpYomi 事業所読み
     */
    public Zip(String code, String x0402, String add1, String add2, String corp,
            String corpYomi) {
        this(code, x0402, add1, add2, corp, "", "", corpYomi, "");
    }
    
    /**
     * プロパティで初期化する。
     * 
     * @param code コード
     * @param x0402 市区町村コード
     * @param add1 住所１
     * @param add2 住所２
     * @param add1Yomi 住所１読み
     * @param add2Yomi 住所２読み
     * @param note 備考
     */
    public Zip(String code, String x0402, String add1, String add2,
            String add1Yomi, String add2Yomi, String note) {
        this(code, x0402, add1, add2, "", add1Yomi, add2Yomi, "", note);
    }
    
    /**
     * プロパティで初期化する。
     * 
     * @param code コード
     * @param x0402 市区町村コード
     * @param add1 住所１
     * @param add2 住所２
     * @param corp 事業所
     * @param add1Yomi 住所１読み
     * @param add2Yomi 住所２読み
     * @param corpYomi 事業所読み
     * @param note 備考
     */
    public Zip(String code, String x0402, String add1, String add2, String corp,
            String add1Yomi, String add2Yomi, String corpYomi, String note) {
        
        this.code = code;
        this.x0402 = x0402;
        this.add1 = add1;
        this.add2 = add2;
        this.corp = corp;
        this.add1Yomi = add1Yomi;
        this.add2Yomi = add2Yomi;
        this.corpYomi = corpYomi;
        this.note = note;
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
     * 市区町村コードを取得する。
     * 
     * @return 市区町村コード
     */
    public String getX0402() {
        return x0402;
    }

    /**
     * 市区町村コードを設定する。
     * 
     * @param x0402 市区町村コード
     */
    public void setX0402(String x0402) {
        this.x0402 = x0402;
    }

    /**
     * 住所１を取得する。
     * 
     * @return 住所１
     */
    public String getAdd1() {
        return add1;
    }

    /**
     * 住所１を設定する。
     * 
     * @param add1 住所１
     */
    public void setAdd1(String add1) {
        this.add1 = add1;
    }

    /**
     * 住所２を取得する。
     * 
     * @return 住所２
     */
    public String getAdd2() {
        return add2;
    }

    /**
     * 住所２を設定する。
     * 
     * @param add2 住所２
     */
    public void setAdd2(String add2) {
        this.add2 = add2;
    }

    /**
     * 事業所を取得する。
     * 
     * @return 事業所
     */
    public String getCorp() {
        return corp;
    }

    /**
     * 事業所を設定する。
     * 
     * @param corp 事業所
     */
    public void setCorp(String corp) {
        this.corp = corp;
    }

    /**
     * 住所１読みを取得する。
     * 
     * @return 住所１読み
     */
    public String getAdd1Yomi() {
        return add1Yomi;
    }

    /**
     * 住所１読みを設定する。
     * 
     * @param add1Yomi 住所１読み
     */
    public void setAdd1Yomi(String add1Yomi) {
        this.add1Yomi = add1Yomi;
    }

    /**
     * 住所２読みを取得する。
     * 
     * @return 住所２読み
     */
    public String getAdd2Yomi() {
        return add2Yomi;
    }

    /**
     * 住所２読みを設定する。
     * 
     * @param add2Yomi 住所２読み
     */
    public void setAdd2Yomi(String add2Yomi) {
        this.add2Yomi = add2Yomi;
    }

    /**
     * 事業所読みを取得する。
     * 
     * @return 事業所読み
     */
    public String getCorpYomi() {
        return corpYomi;
    }

    /**
     * 事業所読みを設定する。
     * 
     * @param corpYomi 事業所読み
     */
    public void setCorpYomi(String corpYomi) {
        this.corpYomi = corpYomi;
    }

    /**
     * 備考を取得する。
     * 
     * @return 備考
     */
    public String getNote() {
        return note;
    }

    /**
     * 備考を設定する。
     * 
     * @param note 備考
     */
    public void setNote(String note) {
        this.note = note;
    }
    
    /**
     * JSON を取得する。
     * 
     * @returnJSON
     */
    public String toJson() {
        
        String ret = null;
        
        try {
            
            JSONStringer jo = new JSONStringer();
            
            jo.object();
            
            if (code.length() > 0) { jo.key("code").value(code); }
            if (x0402.length() > 0) { jo.key("x0402").value(x0402); }
            if (add1.length() > 0) { jo.key("add1").value(add1); }
            if (add2.length() > 0) { jo.key("add2").value(add2); }
            if (corp.length() > 0) { jo.key("corp").value(corp); }
            if (add1Yomi.length() > 0) { jo.key("add1Yomi").value(add1Yomi); }
            if (add2Yomi.length() > 0) { jo.key("add2Yomi").value(add2Yomi); }
            if (corpYomi.length() > 0) { jo.key("corpYomi").value(corpYomi); }
            if (note.length() > 0) { jo.key("note").value(note); }
            
            ret = jo.endObject().toString();
            
        } catch (JSONException e) {
            log.log(Level.WARNING, "", e);
        }
        
        return ret;
    }
}
