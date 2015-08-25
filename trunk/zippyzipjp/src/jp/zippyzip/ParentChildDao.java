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

import java.util.Collection;
import java.util.LinkedList;

/**
 * 親子関係を保持するエンティティのデータアクセスオブジェクトの宣言。
 * 
 * @author Michinobu Maeda
 */
public interface ParentChildDao {

    /**
     * 親子関係エンティティを保存する。
     * 
     * @param pc 親子関係エンティティ
     */
    void store(ParentChild pc);
    
    /**
     * 親子関係エンティティを取得する。
     * 
     * @param key キー
     * @return 親子関係エンティティ
     */
    ParentChild get(String key);

    /**
     * 親子関係エンティティを保存する。
     * 
     * @param key キー
     * @param json JOSN
     */
    void addChild(String key, String json);

    /**
     * 親子関係エンティティを保存する。
     * 
     * @param key キー
     * @param jsons JOSN
     */
    void addChild(String key, LinkedList<String> jsons);
    
    /**
     * キーの一覧を取得する。
     * 
     * @return キーの一覧
     */
    Collection<String> getKeys();
}
