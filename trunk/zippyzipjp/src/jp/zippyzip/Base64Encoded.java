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

import java.util.LinkedList;
import java.util.logging.Logger;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * バイナリデータのエンティティ。
 * 
 * @author Michinobu Maeda
 */
@PersistenceCapable
public class Base64Encoded {
    
    protected static final int LINE_MAX_ENC_LEN = 500;    
    public static final int LINE_MAX_LEN = 360;
    public static final int BLOCK_MAX_LINE = 2000;

    /** ログ　*/
    protected Logger log = Logger.getLogger(this.getClass().getName());

    /** キー */
    @PrimaryKey
    private String key;

    /** データ */
    @Persistent
    private LinkedList<String> base64Encoded;

    /**
     * キーを生成する。
     * 
     * @param filename ファイル名
     * @param seq 順序
     * @return キー　
     */
    public static String getKey(String filename, int seq) {
        return filename + "\t" + seq;
    }

    /**
     * ファイル名を取得する。
     * 
     * @param key キー
     * @return ファイル名
     */
    public static String getFilename(String key) {
        if (key == null) { return null; }
        if (0 > key.indexOf("\t")) { return null; }
        return key.substring(0, key.indexOf("\t"));
    }

    /**
     * 順序を取得する。
     * 
     * @param key キー
     * @return 順序
     */
    public static int getSeq(String key) {
        if (key == null) { return -1; }
        if (0 > key.indexOf("\t")) { return -1; }
        return Integer.parseInt(key.substring(key.indexOf("\t") + 1));
    }
    
    /**
     * プロパティで初期化する。
     * 
     * @param filename ファイル名
     * @param seq 順序
     */
    public Base64Encoded(String filename, int seq) {
        this.key = getKey(filename, seq);
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
     * データを取得する。
     * 
     * @return データ
     */
    public LinkedList<String> getBae64Encoded() {
        return base64Encoded;
    }

    /**
     * データを設定する。
     * 
     * @param data データ
     */
    public void setBae64Encoded(LinkedList<String> base64Encoded) {
        this.base64Encoded = base64Encoded;
    }
    
    /**
     * Base64 データを追加する。
     * 
     * @param base64Encoded Base64 データ
     */
    public void addBase64Encoded(String base64Encoded) {

        if (base64Encoded.length() > LINE_MAX_ENC_LEN) {
            return;
        }
        
        if (this.base64Encoded == null) {
            this.base64Encoded = new LinkedList<String>();
        }
        
        if (this.base64Encoded.size() >= BLOCK_MAX_LINE) {
            return;
        }
                
        this.base64Encoded.add(base64Encoded);
    }
    
    /**
     * ファイル名を取得する。
     * 
     * @return ファイル名
     */
    public String getFilename() {
        return getFilename(key);
    }
    
    /**
     * 順序を取得する。
     * 
     * @return 順序
     */
    public int getSeq() {
        return getSeq(key);
    }

}
