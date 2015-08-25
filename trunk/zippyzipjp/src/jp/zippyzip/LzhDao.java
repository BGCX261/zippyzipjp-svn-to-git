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

import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * LZH のデータアクセスオブジェクトの宣言。
 * 
 * @author Michinobu Maeda
 */
public interface LzhDao {

    /**
     * 全てをクリアする。
     */
    void initAll();

    /**
     * 更新情報をクリアする。
     */
    void initContentStatus();

    /**
     * 更新情報を保存する。
     * 
     * @param stt 更新情報
     */
    void store(ContentStatus stt);
    
    /**
     * 更新情報を取得する。
     * 
     * @return 更新情報のリスト
     */
    List<ContentStatus> getAll();
    
    /**
     * 依存コンテンツの更新を確認する。
     */
    boolean checkDepends();
    
    /**
     * コンテンツの更新を確認する。
     */
    boolean checkContents();
    
    /**
     * LZH を格納する。
     * 
     * @param raw バイナリデータ
     * @param offset バイナリデータの先頭
     * @param size バイナリデータのサイズ
     * @param uploaded ユーザによりアップロードされたデータか否かの情報
     * @return LZH 情報
     */
    Lzh store(byte[] raw, int offset, int size, boolean uploaded);

    /**
     * LZH ファイルをダウンロードする。
     * 
     * @param url URL
     * @return LZH ファイル
     */
    byte[] download(URL url);
    
    /**
     * LZH 情報を取得する。
     * 
     * @return LZH 情報のリスト
     */
    Collection<Lzh> getInfoAll();
    
    /**
     * LZH 情報を削除する。
     * 
     * @param key キー
     */
    void remove(String key);

    /**
     * 最新の LZH のリストを取得する。
     * 
     * @param corp 事業所データか否か
     * @param checkPoint 比較対象の日時
     * @return LZH のリスト
     */
    LinkedList<Lzh> getLatest(boolean corp, Date checkPoint);

    /**
     * 住所データを更新する。
     * 
     * @param lzhs LZH 情報のリスト
     * @param generated 設定するタイムスタンプ
     * @return 郵便番号データ情報
     */
    ZipInfo updateArea(LinkedList<Lzh> lzhs, Date generated);

    /**
     * 事業所データを更新する。
     * 
     * @param lzhs LZH 情報のリスト
     * @param generated 設定するタイムスタンプ
     * @return 郵便番号データ情報
     */
    ZipInfo updateCorp(LinkedList<Lzh> lzhs, Date generated);
    
    /**
     * 郵便番号データの更新情報を取得する。
     * 
     * @return 郵便番号データの更新情報
     */
    ZipInfo getZipInfo();
    
    /**
     * 郵便番号データの更新情報を格納する。
     * 
     * @param zipInfo 郵便番号データの更新情報
     */
    void store(ZipInfo zipInfo);
}
