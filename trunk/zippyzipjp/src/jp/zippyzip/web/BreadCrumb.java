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
public class BreadCrumb {

    /** キー */
    private String key;
    
    /** 名前 */
    private String name;
    
    /**
     * プロパティで初期化する。
     * 
     * @param key キー
     * @param name 名前
     */
    public BreadCrumb(String key, String name) {
        this.name = name;
        this.key = key;
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
}