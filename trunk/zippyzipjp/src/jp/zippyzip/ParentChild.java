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
import java.util.LinkedList;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * 親子関係を保持するエンティティ。
 * 
 * @author Michinobu Maeda
 */
@PersistenceCapable
public class ParentChild {

    /** キー */
    @PrimaryKey
    private String key;

    /** 親 */
    @Persistent
    private LinkedList<String> parents;

    /** 子 */
    @Persistent
    private LinkedList<String> children;
    
    /** 更新日時 */
    private Date timestamp;

    /**
     * プロパティで初期化する。
     * 
     * @param key キー
     * @param timestamp 更新日時
     */
    public ParentChild(String key, Date timestamp) {
        this(key, timestamp, new LinkedList<String>(),
                new LinkedList<String>());
    }

    /**
     * プロパティで初期化する。
     * 
     * @param key キー
     * @param timestamp 更新日時
     * @param parents 親
     */
    public ParentChild(String key, Date timestamp,
            LinkedList<String> parents) {
        this(key, timestamp, parents, new LinkedList<String>());
    }
    
    /**
     * プロパティで初期化する。
     * 
     * @param key キー
     * @param timestamp 更新日時
     * @param parents 親
     * @param children 子
     */
    public ParentChild(String key, Date timestamp,
            LinkedList<String> parents, LinkedList<String> children) {
        this.key = key;
        this.timestamp = timestamp;
        this.parents = parents;
        this.children = children;
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
     * 親を取得する。
     * 
     * @return 親
     */
    public LinkedList<String> getParents() {
        return parents;
    }

    /**
     * 親を設定する。
     * 
     * @param parents 親
     */
    public void setParents(LinkedList<String> parents) {
        this.parents = parents;
    }

    /**
     * 子を取得する。
     * 
     * @return 子
     */
    public LinkedList<String> getChildren() {
        return children;
    }

    /**
     * 子を設定する。
     * 
     * @param children 子
     */
    public void setChildren(LinkedList<String> children) {
        this.children = children;
    }

    /**
     * 更新日時取得する。
     * 
     * @return 更新日時
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * 更新日時設定する。
     * 
     * @param timestamp 更新日時
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
