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

/**
 * ブレッドクラムの要素。
 * 
 * @author Michinobu Maeda
 */
public class ListItem {

    /** キー */
    private String key;
    
    /** 郵便番号 上3桁 */
    private String zip1;
    
    /** 郵便番号 下4桁 */
    private String zip2;
    
    /** 名前 */
    private String name;
    
    /** 読み */
    private String yomi;
    
    /** 備考 */
    private String note;
    
    /**
     * プロパティで初期化する。
     * 
     * @param key キー
     * @param zip1 郵便番号 上3桁
     * @param zip2 郵便番号 下4桁
     * @param name 名前
     * @param yomi 読み
     * @param note 備考
     */
    public ListItem(String key, String zip1, String zip2, String name, String yomi, String note) {
        this.key = key;
        this.zip1 = zip1;
        this.zip2 = zip2;
        this.name = name;
        this.yomi = yomi;
        this.note = note;
    }

    /**
     * キーを取得する。
     * 
     * @return キー
     */
    public String getKey() {
        return key;
    }

    /**
     * キーを設定する。
     * 
     * @param key キー
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * 郵便番号 上3桁を取得する。
     * 
     * @return 郵便番号 上3桁
     */
    public String getZip1() {
        return zip1;
    }

    /**
     * 郵便番号 上3桁を設定する。
     * 
     * @param zip1 郵便番号 上3桁
     */
    public void setZip1(String zip1) {
        this.zip1 = zip1;
    }

    /**
     * 郵便番号 下4桁を取得する。
     * 
     * @return 郵便番号 下4桁
     */
    public String getZip2() {
        return zip2;
    }

    /**
     * 郵便番号 下4桁を設定する。
     * 
     * @param zip2 郵便番号 下4桁
     */
    public void setZip2(String zip2) {
        this.zip2 = zip2;
    }

    /**
     * 名前を取得する。
     * 
     * @return 名前
     */
    public String getName() {
        return name;
    }

    /**
     * 名前を設定する。
     * 
     * @param name 名前
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
}