<?php
/*
 * Copyright 2008,2009 Michinobu Maeda.
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

/*
 * 初期設定
 */
require_once dirname(__FILE__) . '/zippyzip.conf';
require_once dirname(__FILE__) . '/zippyzip-ime.conf';
require_once dirname(__FILE__) . '/zippyzip-common.php';

log_info(__FILE__);
log_info("Start.");

mkdirs(array(PATH_TMP_IME));
mkdirs(array(PATH_IME));
update_x0402();

/*
 * 郵便番号辞書（住所）を出力する。
 */
$s = fopen(PATH_CSV . "/" . AREA_OUT, "rb");
$t = fopen(PATH_TMP_IME_AREA, "ab");
while ($cols = fgetcsv($s, 1024, ",")) {
    fwrite($t, mb_convert_encoding(mb_convert_kana(
            substr($cols[0], 0, 3) . "-" . substr($cols[0], 3, 7) . "\t" .
            $x0401[substr($cols[1], 0, 2)] . $x0402[$cols[1]] . $cols[2] . $cols[3] . $cols[4] .
            "\t地名その他\r\n", "A", "UTF-8"), "Windows-31J", "UTF-8"));
}
fclose($t);
fclose($s);


/*
 * 郵便番号辞書（事業所）を出力する。
 */
$s = fopen(PATH_CSV . "/" . FIRM_OUT, "rb");
$t = fopen(PATH_TMP_IME_FIRM, "ab");
while ($cols = fgetcsv($s, 1024, ",")) {
    if (!isset($x0402[$cols[1]])) {
        continue;
    }
    fwrite($t, mb_convert_encoding(mb_convert_kana(
            substr($cols[0], 0, 3) . "-" . substr($cols[0], 3, 7) . "\t" .
            $x0401[substr($cols[1], 0, 2)] . $x0402[$cols[1]] . $cols[2] . $cols[3] . $cols[4] .
            "\t地名その他\r\n", "A", "UTF-8"), "Windows-31J", "UTF-8"));
}
fclose($t);
fclose($s);

move_data(PATH_TMP_IME_AREA,
    PATH_IME . "/" . date("YmdHis", filemtime(PATH_TMP_IME_AREA)) . PATH_IME_AREA);
move_data(PATH_TMP_IME_FIRM,
    PATH_IME . "/" . date("YmdHis", filemtime(PATH_TMP_IME_FIRM)) . PATH_IME_FIRM);

// 目次ページを更新する。    
$tmp = PATH_TMP_IME . "/index.html";
$trg = PATH_IME . "/index.html";
$t = fopen($tmp, "wb");
eval(preg_replace(HTML_BEGIN, 'fwrite(\$t, <<<EOP',
     preg_replace(HTML_END, "\nEOP\n);", IME_INDEX)));
fclose($t);
move_file($tmp, $trg);
log_info("Updated  : $trg");

rmdirs(array(PATH_TMP_IME));

/*
 * 処理完了
 */
log_info("End.");
exit(0);
