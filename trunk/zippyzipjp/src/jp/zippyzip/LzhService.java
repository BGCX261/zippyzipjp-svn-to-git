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
import java.util.List;


/**
 * LZH 管理サービスの宣言。
 * 
 * @author Michinobu Maeda
 */
public interface LzhService {

    /**
     * 全てを初期化する。
     */
    void initAll();

    /**
     * 更新情報を初期化する。
     */
    void initStatus();
    
    /**
     * 更新情報を取得する。
     * 
     * @return 更新情報
     */
    List<ContentStatus> getAll();

    /**
     * 更新情報のエントリを更新する。
     * 
     * @param stt 更新情報のエントリ
     */
    void update(ContentStatus stt);

    /**
     * 依存コンテンツの更新を確認する。
     * 
     * @return 更新の有無
     */
    boolean checkDepends();
    
    /**
     * コンテンツの更新を確認する。
     * 
     * @return 確認対象の有無
     */
    boolean checkContents();
    
    /**
     * コンテンツの更新の有無を取得する。
     * 
     * @return 更新の有無
     */
    boolean isStatusUpdated();

    /**
     * LZH 情報を取得する。
     * 
     * @return LZH 情報
     */
    Collection<Lzh> getInfoAll();

    /**
     * アップロードした LZH を格納する。
     * 
     * @param buff LZH データ
     * @param offset オフセット
     * @param size サイズ
     * @return 格納の有無
     */
    boolean store(byte[] buff, int offset, int size);

    /**
     * LZH を削除する。
     * 
     * @param key LZH 情報のキー
     */
    void delete(String key);

    /**
     * 最新のデータの有無を確認し、ダウンロードする。
     * 
     * @return ダウンロードの有無
     */
    boolean fetch();
}
