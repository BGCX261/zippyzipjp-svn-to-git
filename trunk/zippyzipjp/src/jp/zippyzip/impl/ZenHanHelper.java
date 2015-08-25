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
package jp.zippyzip.impl;

import java.util.HashMap;

/**
 * 全角半角変換のためのヘルパ。
 * 
 * @author Michinobu Maeda
 */
public class ZenHanHelper {

    /** 半角数字を全角数字に変換するテーブル */
    private static final HashMap<String, String> hanZenTable =
        new HashMap<String, String>();
    static {
        hanZenTable.put("0", "０");
        hanZenTable.put("1", "１");
        hanZenTable.put("2", "２");
        hanZenTable.put("3", "３");
        hanZenTable.put("4", "４");
        hanZenTable.put("5", "５");
        hanZenTable.put("6", "６");
        hanZenTable.put("7", "７");
        hanZenTable.put("8", "８");
        hanZenTable.put("9", "９");
    }
    
    /** 半角カナをひらがなに変換するテーブル */
    private static final HashMap<String, String> kanaTable =
        new HashMap<String, String>();
    static {
        kanaTable.put("ｱ", "あ");
        kanaTable.put("ｲ", "い");
        kanaTable.put("ｳ", "う");
        kanaTable.put("ｴ", "え");
        kanaTable.put("ｵ", "お");
        kanaTable.put("ｶ", "か");
        kanaTable.put("ｷ", "き");
        kanaTable.put("ｸ", "く");
        kanaTable.put("ｹ", "け");
        kanaTable.put("ｺ", "こ");
        kanaTable.put("ｻ", "さ");
        kanaTable.put("ｼ", "し");
        kanaTable.put("ｽ", "す");
        kanaTable.put("ｾ", "せ");
        kanaTable.put("ｿ", "そ");
        kanaTable.put("ﾀ", "た");
        kanaTable.put("ﾁ", "ち");
        kanaTable.put("ﾂ", "つ");
        kanaTable.put("ﾃ", "て");
        kanaTable.put("ﾄ", "と");
        kanaTable.put("ﾅ", "な");
        kanaTable.put("ﾆ", "に");
        kanaTable.put("ﾇ", "ぬ");
        kanaTable.put("ﾈ", "ね");
        kanaTable.put("ﾉ", "の");
        kanaTable.put("ﾊ", "は");
        kanaTable.put("ﾋ", "ひ");
        kanaTable.put("ﾌ", "ふ");
        kanaTable.put("ﾍ", "へ");
        kanaTable.put("ﾎ", "ほ");
        kanaTable.put("ﾏ", "ま");
        kanaTable.put("ﾐ", "み");
        kanaTable.put("ﾑ", "む");
        kanaTable.put("ﾒ", "め");
        kanaTable.put("ﾓ", "も");
        kanaTable.put("ﾔ", "や");
        kanaTable.put("ﾕ", "ゆ");
        kanaTable.put("ﾖ", "よ");
        kanaTable.put("ﾗ", "ら");
        kanaTable.put("ﾘ", "り");
        kanaTable.put("ﾙ", "る");
        kanaTable.put("ﾚ", "れ");
        kanaTable.put("ﾛ", "ろ");
        kanaTable.put("ﾜ", "わ");
        kanaTable.put("ｦ", "を");
        kanaTable.put("ﾝ", "ん");
        kanaTable.put("ｧ", "ぁ");
        kanaTable.put("ｨ", "ぃ");
        kanaTable.put("ｩ", "ぅ");
        kanaTable.put("ｪ", "ぇ");
        kanaTable.put("ｫ", "ぉ");
        kanaTable.put("ｯ", "っ");
        kanaTable.put("ｬ", "ゃ");
        kanaTable.put("ｭ", "ゅ");
        kanaTable.put("ｮ", "ょ");
        kanaTable.put("ｶﾞ", "が");
        kanaTable.put("ｷﾞ", "ぎ");
        kanaTable.put("ｸﾞ", "ぐ");
        kanaTable.put("ｹﾞ", "げ");
        kanaTable.put("ｺﾞ", "ご");
        kanaTable.put("ｻﾞ", "ざ");
        kanaTable.put("ｼﾞ", "じ");
        kanaTable.put("ｽﾞ", "ず");
        kanaTable.put("ｾﾞ", "ぜ");
        kanaTable.put("ｿﾞ", "ぞ");
        kanaTable.put("ﾀﾞ", "だ");
        kanaTable.put("ﾁﾞ", "ぢ");
        kanaTable.put("ﾂﾞ", "づ");
        kanaTable.put("ﾃﾞ", "で");
        kanaTable.put("ﾄﾞ", "ど");
        kanaTable.put("ﾊﾞ", "ば");
        kanaTable.put("ﾋﾞ", "び");
        kanaTable.put("ﾌﾞ", "ぶ");
        kanaTable.put("ﾍﾞ", "べ");
        kanaTable.put("ﾎﾞ", "ぼ");
        kanaTable.put("ﾊﾟ", "ぱ");
        kanaTable.put("ﾋﾟ", "ぴ");
        kanaTable.put("ﾌﾟ", "ぷ");
        kanaTable.put("ﾍﾟ", "ぺ");
        kanaTable.put("ﾎﾟ", "ぽ");
        kanaTable.put("ｰ", "ー");
        kanaTable.put("-", "ー");
    }
    
    /**
     * 半角の郵便番号を全角に変換する。
     * 
     * @param str 半角の郵便番号
     * @return 全角の郵便番号
     */
    public static String convertZipHankakuZenkaku(String str) {
        
        if (str.length() != 7) { return ""; }
        
        StringBuilder ret = new StringBuilder("");
        
        ret.append(hanZenTable.get(str.substring(0, 1)));
        ret.append(hanZenTable.get(str.substring(1, 2)));
        ret.append(hanZenTable.get(str.substring(2, 3)));
        ret.append("\uFF0D");
        ret.append(hanZenTable.get(str.substring(3, 4)));
        ret.append(hanZenTable.get(str.substring(4, 5)));
        ret.append(hanZenTable.get(str.substring(5, 6)));
        ret.append(hanZenTable.get(str.substring(6, 7)));
        
        return ret.toString();
    }
    
    /**
     * 全角英数字を半角に変換する。
     * 
     * @param str 処理対象
     * @return 処理結果
     */
    public static String convertZH(String str) {
        
        if (str == null) { return str; }

        StringBuilder buff = new StringBuilder(str);
        
        for (int i = 0; i < buff.length(); ++i) {
            
            switch (buff.charAt(i)) {
            case '０': buff.setCharAt(i, '0'); break;
            case '１': buff.setCharAt(i, '1'); break;
            case '２': buff.setCharAt(i, '2'); break;
            case '３': buff.setCharAt(i, '3'); break;
            case '４': buff.setCharAt(i, '4'); break;
            case '５': buff.setCharAt(i, '5'); break;
            case '６': buff.setCharAt(i, '6'); break;
            case '７': buff.setCharAt(i, '7'); break;
            case '８': buff.setCharAt(i, '8'); break;
            case '９': buff.setCharAt(i, '9'); break;
            case '－': buff.setCharAt(i, '-'); break;
            case 'Ａ': buff.setCharAt(i, 'A'); break;
            case 'Ｂ': buff.setCharAt(i, 'B'); break;
            case 'Ｃ': buff.setCharAt(i, 'C'); break;
            case 'Ｄ': buff.setCharAt(i, 'D'); break;
            case 'Ｅ': buff.setCharAt(i, 'E'); break;
            case 'Ｆ': buff.setCharAt(i, 'F'); break;
            case 'Ｇ': buff.setCharAt(i, 'G'); break;
            case 'Ｈ': buff.setCharAt(i, 'H'); break;
            case 'Ｉ': buff.setCharAt(i, 'I'); break;
            case 'Ｊ': buff.setCharAt(i, 'J'); break;
            case 'Ｋ': buff.setCharAt(i, 'K'); break;
            case 'Ｌ': buff.setCharAt(i, 'L'); break;
            case 'Ｍ': buff.setCharAt(i, 'M'); break;
            case 'Ｎ': buff.setCharAt(i, 'N'); break;
            case 'Ｏ': buff.setCharAt(i, 'O'); break;
            case 'Ｐ': buff.setCharAt(i, 'P'); break;
            case 'Ｑ': buff.setCharAt(i, 'Q'); break;
            case 'Ｒ': buff.setCharAt(i, 'R'); break;
            case 'Ｓ': buff.setCharAt(i, 'S'); break;
            case 'Ｔ': buff.setCharAt(i, 'T'); break;
            case 'Ｕ': buff.setCharAt(i, 'U'); break;
            case 'Ｖ': buff.setCharAt(i, 'V'); break;
            case 'Ｗ': buff.setCharAt(i, 'W'); break;
            case 'Ｘ': buff.setCharAt(i, 'X'); break;
            case 'Ｙ': buff.setCharAt(i, 'Y'); break;
            case 'Ｚ': buff.setCharAt(i, 'Z'); break;
            case 'ａ': buff.setCharAt(i, 'a'); break;
            case 'ｂ': buff.setCharAt(i, 'b'); break;
            case 'ｃ': buff.setCharAt(i, 'c'); break;
            case 'ｄ': buff.setCharAt(i, 'd'); break;
            case 'ｅ': buff.setCharAt(i, 'e'); break;
            case 'ｆ': buff.setCharAt(i, 'h'); break;
            case 'ｇ': buff.setCharAt(i, 'g'); break;
            case 'ｈ': buff.setCharAt(i, 'h'); break;
            case 'ｉ': buff.setCharAt(i, 'i'); break;
            case 'ｊ': buff.setCharAt(i, 'j'); break;
            case 'ｋ': buff.setCharAt(i, 'k'); break;
            case 'ｌ': buff.setCharAt(i, 'l'); break;
            case 'ｍ': buff.setCharAt(i, 'm'); break;
            case 'ｎ': buff.setCharAt(i, 'n'); break;
            case 'ｏ': buff.setCharAt(i, 'o'); break;
            case 'ｐ': buff.setCharAt(i, 'p'); break;
            case 'ｑ': buff.setCharAt(i, 'q'); break;
            case 'ｒ': buff.setCharAt(i, 'r'); break;
            case 'ｓ': buff.setCharAt(i, 's'); break;
            case 'ｔ': buff.setCharAt(i, 't'); break;
            case 'ｕ': buff.setCharAt(i, 'u'); break;
            case 'ｖ': buff.setCharAt(i, 'v'); break;
            case 'ｗ': buff.setCharAt(i, 'w'); break;
            case 'ｘ': buff.setCharAt(i, 'x'); break;
            case 'ｙ': buff.setCharAt(i, 'y'); break;
            case 'ｚ': buff.setCharAt(i, 'z'); break;
            case '．': buff.setCharAt(i, '.'); break;
            case '，': buff.setCharAt(i, ','); break;
            case '‘': buff.setCharAt(i, '\''); break;
            case '’': buff.setCharAt(i, '\''); break;
            case '“': buff.setCharAt(i, '"'); break;
            case '”': buff.setCharAt(i, '"'); break;
            case '　': buff.setCharAt(i, ' '); break;
            case '（': buff.setCharAt(i, '('); break;
            case '）': buff.setCharAt(i, ')'); break;
            }
        }
        
        return buff.toString();
    }
    
    /**
     * 半角カナをひらがなに変換する。
     * 
     * @param str 処理対象
     * @return 処理結果
     */
    public static String convertKana(String str) {
        
        StringBuilder ret = new StringBuilder(str);
        
        for (int i = ret.length() - 1; 0 <= i; --i) {
            
            String key = ret.substring(i, i + 1).toString();
            
            if (kanaTable.containsKey(key)) {
                
                ret.replace(i, i + 1, kanaTable.get(key));
                continue;
            }
            
            if (0 == i) { break; }
            
            key = ret.substring(i - 1, i + 1).toString();
            
            if (kanaTable.containsKey(key)) {
                
                ret.replace(i - 1, i + 1, kanaTable.get(key));
                continue;
            }
        }
        
        return ret.toString();
    }

}
