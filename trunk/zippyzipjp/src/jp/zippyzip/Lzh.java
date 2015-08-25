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

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * LZHのエンティティ。
 * 
 * @author Michinobu Maeda
 */
@PersistenceCapable
public class Lzh {

    /** キー */
    @PrimaryKey
    private String key;
    
    /** 更新日時 */
    @Persistent
    private Date timestamp;
    
    /** ファイル名 */
    @Persistent
    private String filename;
    
    /** 格納日時 */
    @Persistent
    Date stored;

    /** ユーザによりアップロードされたデータか否かの情報 */
    @Persistent
    private boolean uploaded;
    
    /** デフォルトコンストラクタは使用しない。 */
    protected Lzh() {}

    /**
     * プロパティを設定して初期化する。
     * キーの値は timestamp と filename から生成する。
     * 
     * @param timestamp 更新日時
     * @param filename ファイル名
     * @param uploaded ユーザによりアップロードされたデータか否かの情報
     */
    public Lzh(Date timestamp, String filename, boolean uploaded) {
        
        this.timestamp = timestamp;
        this.filename = filename;
        this.key = new SimpleDateFormat("yyyyMMddHHmmss").format(timestamp
                ) + "-" + filename.replaceFirst("(\\.csv|\\.CSV)", ".lzh");
        this.stored = new Date();
        this.uploaded = uploaded;
    }
    
    /**
     * キーを取得する。
     * @return キー
     */
    public String getKey() {
        return key;
    }

    /**
     * キーを設定する。
     * @param key キー
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * 更新日時を取得する。
     * @return 更新日時
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * 更新日時を設定する。
     * @param timestamp 更新日時
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * ファイル名を取得する。
     * @return ファイル名
     */
    public String getFilename() {
        return filename;
    }

    /**
     * ファイル名を設定する。
     * @param filename ファイル名
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * 格納日時を取得する。
     * @return 格納日時
     */
    public Date getStored() {
        return stored;
    }

    /**
     * 格納日時を設定する。
     * @param stored 格納日時
     */
    public void setStored(Date stored) {
        this.stored = stored;
    }

    /**
     * ユーザによりアップロードされたデータか否かの情報を取得する。
     * @return ユーザによりアップロードされたデータか否かの情報
     */
    public boolean isUploaded() {
        return uploaded;
    }

    /**
     * ユーザによりアップロードされたデータか否かの情報を設定する。
     * @param uploaded ユーザによりアップロードされたデータか否かの情報
     */
    public void setUploaded(boolean uploaded) {
        this.uploaded = uploaded;
    }

}
