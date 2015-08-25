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

import java.util.ListResourceBundle;

public class MessageResourceBundle extends ListResourceBundle {

    @Override
    protected Object[][] getContents() {
        return new Object[][] {
                {"null",            ""},
                {"dep.updated",     "依存コンテンツの更新がありました。"},
                {"dep.notupdated",  "依存コンテンツの更新はありません。"},
                {"con.checked",     "コンテンツの更新を確認しました。"},
                {"con.nonchecked",  "未確認のコンテンツはありません。"},
                {"con.updated",     "更新されているコンテンツがあります。"},
                {"con.notupdated",  "更新されているコンテンツはありません。"},
                {"upload.success",  "ファイルのアップロードが完了しました。"},
                {"upload.failed",   "ファイルのアップロードに失敗しました。"},
                {"upload.empty",    "アップロードするファイルが設定されていません。"},
                {"lzh.update",      "LZH ファイルを更新しました。"},
                {"lzh.cancel",      "LZH ファイルは最新です。"},
                {"lzh.corp",        "事業所データの LZH ファイルを更新しました。"},
                {"zip.old",         "郵便番号データの更新が必要です。"},
                {"zip.new",         "郵便番号データは最新です。"},
                {"priv.admin",      "管理者権限が必要です。"},
                {"adminEmail",      "info@zippyzipjp.appspotmail.com"},
                {"invalidUrl",      "不正な URL です。"},
        };
    }

}
