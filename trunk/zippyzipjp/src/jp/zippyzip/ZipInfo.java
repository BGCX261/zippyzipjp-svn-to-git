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
 * 郵便番号データの更新情報。
 * 
 * @author Michinobu Maeda
 */
@PersistenceCapable
public class ZipInfo {
    
    /** キー */
    @PrimaryKey
    private String key;
    
    /** CSV データのタイムスタンプ */
    @Persistent
    private Date timestamp;
    
    /** 生成日時 */
    @Persistent
    private Date generated;

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
     * CSV データのタイムスタンプを取得する。
     * 
     * @return CSV データのタイムスタンプ
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * CSV データのタイムスタンプを設定する。
     * 
     * @param CSV データのタイムスタンプ
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * 生成日時を取得する。
     * 
     * @return 生成日時
     */
    public Date getGenerated() {
        return generated;
    }

    /**
     * 生成日時を設定する。
     * 
     * @param generated 生成日時
     */
    public void setGenerated(Date generated) {
        this.generated = generated;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((generated == null) ? 0 : generated.hashCode());
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        result = prime * result
                + ((timestamp == null) ? 0 : timestamp.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ZipInfo other = (ZipInfo) obj;
        if (generated == null) {
            if (other.generated != null)
                return false;
        } else if (!generated.equals(other.generated))
            return false;
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key))
            return false;
        if (timestamp == null) {
            if (other.timestamp != null)
                return false;
        } else if (!timestamp.equals(other.timestamp))
            return false;
        return true;
    }

}
