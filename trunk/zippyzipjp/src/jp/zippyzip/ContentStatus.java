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

import java.util.Date;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * 更新情報のエンティティ。
 * 
 * @author Michinobu Maeda
 */
@PersistenceCapable
public class ContentStatus {
    
    /** ID */
    @PrimaryKey
    private String id;
    
    /** 依存する更新情報のID */
    @Persistent
    private String depends;
    
    /** URL */
    @Persistent
    private String url;

    /** コンテンツの更新日時 */
    @Persistent
    private Date lastUpdate;
    
    /** 最終確認日時 */
    @Persistent
    private Date lastCheck;
    
    /** コンテンツのハッシュ値 */
    @Persistent
    private String hash;
    
    /** コンテンツが LZH であるか否かの情報 */
    @Persistent
    private boolean lzh;
    
    /** コンテンツが事業所データであるか否かの情報 */
    @Persistent
    private boolean corp;

    /** デフォルトコンストラクタは使用しない。 */
    protected ContentStatus() {}
    
    /**
     * プロパティを設定して初期化する。
     * 
     * @param id ID
     * @param depends 依存する更新情報の ID
     * @param url URL
     * @param lastUpdate コンテンツの更新日時
     * @param lastCheck 最終確認日時
     * @param lzh コンテンツが LZH であるか否かの情報
     * @param corp コンテンツが事業所データであるか否かの情報
     */
    public ContentStatus(String id, String depends, String url, Date lastUpdate,
            Date lastCheck, boolean lzh, boolean corp) {

        this.id = id;
        this.depends = depends;
        this.url = url;
        this.lastUpdate = lastUpdate;
        this.lastCheck = lastCheck;
        this.hash = null;
        this.lzh = lzh;
        this.corp = corp;
    }

    /**
     * ID を取得する。
     * @return ID
     */
    public String getId() {
        return id;
    }

    /**
     * ID を設定する。
     * @param id ID
     */
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * 依存する更新情報の ID を取得する。
     * @return 依存する更新情報の ID
     */
    public String getDepends() {
        return depends;
    }

    /**
     * 依存する更新情報のIDを設定する。
     * @param depends 依存する更新情報の ID
     */
    public void setDepends(String depends) {
        this.depends = depends;
    }

    /**
     * URL を取得する。
     * @return URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * URL を設定する。
     * @param url URL
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * コンテンツの更新日時を取得する。
     * @return コンテンツの更新日時
     */
    public Date getLastUpdate() {
        return lastUpdate;
    }

    /**
     * コンテンツの更新日時を設定する。
     * @param lastUpdate コンテンツの更新日時
     */
    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    /**
     * 最終確認日時を取得する。
     * @return 最終確認日時
     */
    public Date getLastCheck() {
        return lastCheck;
    }

    /**
     * 最終確認日時を設定する。
     * @param lastCheck 最終確認日時
     */
    public void setLastCheck(Date lastCheck) {
        this.lastCheck = lastCheck;
    }

    /**
     * コンテンツのハッシュ値を取得する。
     * @return コンテンツのハッシュ値
     */
    public String getHash() {
        return hash;
    }

    /**
     * コンテンツのハッシュ値を設定する。
     * @param hash コンテンツのハッシュ値
     */
    public void setHash(String hash) {
        this.hash = hash;
    }

    /**
     * コンテンツが LZH であるか否かの情報を取得する。
     * @return コンテンツが LZH であるか否かの情報
     */
    public boolean isLzh() {
        return lzh;
    }

    /**
     * コンテンツが LZH であるか否かの情報を設定する。
     * @param lzh コンテンツが LZH であるか否かの情報
     */
    public void setLzh(boolean lzh) {
        this.lzh = lzh;
    }

    /**
     * コンテンツが事業所データであるか否かの情報を取得する。
     * @return コンテンツが事業所データであるか否かの情報
     */
    public boolean isCorp() {
        return corp;
    }

    /**
     * コンテンツが事業所データであるか否かの情報を設定する。
     * @param corp コンテンツが事業所データであるか否かの情報
     */
    public void setCorp(boolean corp) {
        this.corp = corp;
    }

}
