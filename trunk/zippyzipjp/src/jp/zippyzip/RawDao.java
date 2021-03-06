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

/**
 *  バイナリデータのデータアクセスオブジェクトの宣言。
 * 
 * @author Michinobu Maeda
 */
public interface RawDao {

    /**
     * データを格納する。
     * 
     * @param raw データ
     * @param filename ファイル名
     */
    void store(byte[] raw, String filename);

    /**
     * データを格納する。
     * 
     * @param raw データ
     * @param offset 開始位置
     * @param size サイズ
     * @param filename ファイル名
     */
    void store(byte[] raw, int offset, int size, String filename);
    
    /**
     * データを取り出す。
     * 
     * @param filename ファイル名
     * @return raw データ
     */
    byte[] get(String filename);
    
    /**
     * データを消去する。
     * 
     * @param filename ファイル名
     */
    void remove(String filename);
}
