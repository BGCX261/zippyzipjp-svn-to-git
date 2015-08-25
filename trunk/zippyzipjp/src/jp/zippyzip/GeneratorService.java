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
 * 生成物サービスの宣言。
 * 
 * @author Michinobu Maeda
 */
public interface GeneratorService {

    /**
     * 更新の必要性の有無を確認する。
     * 
     * @return 更新の有無
     */
    boolean check();

    /**
     * 住所データを更新する。
     */
    void updateArea();

    /**
     * 事業所データを更新する。
     */
    void updateCorp();
    
    /**
     * 郵便番号データを準備する。
     */
    void preZips();
    
    /**
     * 郵便番号データを更新する。
     */
    void updateZips();

    /**
     * ビル一覧データを更新する。
     */
    void updateBuilding();
    
    /**
     * 都道府県データを格納する。
     */
    void storeX0401Zip();
    
    /**
     * 市区町村データを格納する。
     */
    void storeX0402Zip();
    
    /**
     * 住所データタブ区切り形式を格納する。
     */
    void storeAreaText();
    
    /**
     * 住所データ CSV 形式を格納する。
     */
    void storeAreaCsv();
    
    /**
     * 住所データ JSON 形式を格納する。
     */
    void storeAreaJson();
    
    /**
     * 住所データ XML 形式を格納する。
     */
    void storeAreaXml();
    
    /**
     * 住所データ Windows IME 用辞書データを格納する。
     */
    void storeAreaIme();
    
    /**
     * 事業所データタブ区切り形式を格納する。
     */
    void storeCorpText();
    
    /**
     * 事業所データ CSV 形式を格納する。
     */
    void storeCorpCsv();
    
    /**
     * 事業所データ JSON 形式を格納する。
     */
    void storeCorpJson();
    
    /**
     * 事業所データ XML 形式を格納する。
     */
    void storeCorpXml();
    
    /**
     * 事業所データ Windows IME 用辞書データを格納する。
     */
    void storeCorpIme();
    
    /**
     * 都道府県市区町村 JSONデータを格納する。
     */
    void storeJsonPrefCity();
    
    /**
     * 住所 JSONデータを格納する。
     */
    void storeJsonArea();
    
    /**
     * 事業所 JSONデータを格納する。
     */
    void storeJsonCorp();
    
    /**
     * 郵便番号別 JSONデータを格納する。
     * 
     * @param digits 上一桁
     * @return 次の上一桁
     */
    String storeJsonZips(String digits);
    
    /**
     * データ生成の完了を記録する。
     */
    void complete();
}
