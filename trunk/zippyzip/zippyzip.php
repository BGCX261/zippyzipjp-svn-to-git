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
require_once dirname(__FILE__) . '/zippyzip-common.php';

log_info(__FILE__);
log_info("Start.");
mkdirs(array(PATH_TMP, PATH_VAR, PATH_DATA, PATH_ARC, PATH_CSV));
$updated = false;

/*
 * 全県データの処理
 */
if (capture(AREA_PAGE, AREA_URL, AREA, AREA_UTF8)) {

    if (!extract_x0402()) { exit(1); }
    if (!normalize_area()) { exit(1); }
    if (!archive($area_arc_list)) { exit(1); }
    if (!update_arc_index()) { exit(1); }
    move_file(PATH_TMP . "/" . AREA_UTF8, PATH_CSV);
    move_file(PATH_TMP . "/" . X0402_OUT, PATH_CSV);
    move_file(PATH_TMP . "/" . AREA_OUT, PATH_CSV);
    move_file(PATH_TMP . "/" . AREA . "_html_hash", PATH_VAR);
    move_file(PATH_TMP . "/" . AREA . "_lzh_hash", PATH_VAR);
    unlink_files(array(PATH_TMP . "/" . AREA . ".csv"));
    $updated = true;
    
} else {
    unlink_files(array(PATH_TMP . "/" . AREA . "_html_hash",
                       PATH_TMP . "/" . AREA . "_lzh_hash"));
}

/*
 * 事業所データの処理
 */
if (capture(FIRM_PAGE, FIRM_URL, FIRM, FIRM_UTF8)) {

    if (!normalize_firm()) { exit(1); }
    if (!archive($firm_arc_list)) { exit(1); }
    if (!update_arc_index()) { exit(1); }
    move_file(PATH_TMP . "/" . FIRM_UTF8, PATH_CSV);
    move_file(PATH_TMP . "/" . FIRM_OUT, PATH_CSV);
    move_file(PATH_TMP . "/" . FIRM . "_html_hash", PATH_VAR);
    move_file(PATH_TMP . "/" . FIRM . "_lzh_hash", PATH_VAR);
    unlink_files(array(PATH_TMP . "/" . FIRM . ".csv"));    
    $updated = true;
    
} else {
    unlink_files(array(PATH_TMP . "/" . FIRM . "_html_hash",
                       PATH_TMP . "/" . FIRM . "_lzh_hash"));
}
    
/*
 * 処理完了
 */
log_info("End.");
exit($updated ? 0 : 9);

/**
 * LZH 形式の郵便番号データが更新されていたらダウンロードして所定のフォーマットに変換する。
 */
function capture($page, $url, $name, $utf8) {
    if (!check($page, "${name}_html_hash")) { return false; }
    if (!check($url, "${name}_lzh_hash")) { return false; }
    if (!download($url, "${name}.lzh")) { return false; }
    if (!extract_lzh("${name}.lzh", "${name}.csv")) { return false; }
    if (!convert_to_utf8("${name}.csv", $utf8)) { return false; }
    return true;
}

/**
 * Web上のファイルの更新の有無を確認する。
 */
function check($url, $hash) {

    $org = PATH_VAR . "/" . $hash;
    $cmp = PATH_TMP . "/" . $hash;
    $orghash = is_file($org) ? file_get_contents($org) : "";
    $cmphash = md5_file($url);
    if ($orghash == $cmphash) { return false; }

    if (!file_put_contents($cmp, $cmphash)) {
        log_fatal("Faild to write file: $cmp");
        exit(1);
    }

    log_info("Updated  : $url");
    return true;
}

/**
 * Web上のファイルをダウンロードする。
 */
function download($url, $trg) {

    $trg = PATH_TMP . "/" . $trg;
    unlink_files(array($trg));
    $f = fopen($url, "rb");
    $t = fopen($trg, "wb");
    while (!feof($f)) { fwrite($t, fread($f, BUFSIZ)); }
    fclose($t);
    fclose($f);

    if (!is_file($trg)) {
        log_info("Failed to download: $trg");
        return false;
    }

    log_info("Downloaded: $trg");
    return true;
}

/**
 * LZH 形式の圧縮ファイルを解凍する。
 * LZH には、予めファイル名のわかっているファイルが1コだけ含まれる前提とする。
 * 解凍したファイルのファイル名に大文字が含まれる場合は、小文字に変換する。
 */
function extract_lzh($lzh, $csv) {
    $cmd = LHA_COMMAND . PATH_TMP . "/" . $lzh;
    $return_var = 0;
    $output = array();
    exec($cmd);
    if ($return_var != 0) {
        log_fatal("Failed to extract lzh: $cmd");
        log_fatal($output);
        exit(1);
    }
    $is_csv = false;
    foreach (glob(PATH_TMP . "/*.{c,C}{s,S}{v,V}", GLOB_BRACE) as $file) {
        $file = basename($file);
        if (strtolower($file) != $csv) { continue; }
        $is_csv = true;
        log_info("Extracted: $csv");
        if ($file == $csv) { continue; }
        move_file(PATH_TMP . "/" . $file, PATH_TMP . "/" . $csv);
    }
    if ($is_csv) { return true; }
    log_info("Faild to extract: $csv");
    return false;
}

/**
 * Shift_JIS のファイルを UTF-8 に変換する。
 * 半角カナを全角カナに、全角英数を半角英数に、全角スペースを半角スペースに変換する。
 * 「(」「)」 の外側のスペースを削除する。
 */
function convert_to_utf8($src, $trg) {

    $rcnt = 0;
    $wcnt = 0;
    $s = fopen(PATH_TMP . "/$src", "rb");
    $t = fopen(PATH_TMP . "/$trg", "wb");
    while (!feof($s)) {
        $line = fgets($s);
        if (!$line) { continue; }
        ++ $rcnt;
        ++ $wcnt;
        fwrite($t,
            preg_replace("/\ \(/", "(",
            preg_replace("/\)\ /", ")",
            mb_convert_kana(
            preg_replace("/　/", " ",
            mb_convert_encoding($line,
            	"UTF-8", "Windows-31J")),
                "KVa", "UTF-8"))));
    }
    fclose($t);
    fclose($s);

    log_info("Converted to UTF-8: $trg read: $rcnt write: $rcnt");
    return true;
}

/**
 * 全県データから JIS X 0402 準拠の行政区コードを抽出する。
 */
function extract_x0402() {

    $rcnt = 0;
    $wcnt = 0;
    $trg = PATH_TMP . "/" . X0402_OUT;
    $code = "";
    $lines = array();
    $s = fopen(PATH_TMP . "/" . AREA_UTF8, "rb");
    while (($cols = fgetcsv($s, 1024, ",")) !== FALSE) {
        ++ $rcnt;
        if ($code == $cols[0]) { continue; }
        $code = $cols[0];
        array_push($lines, array($code, $cols[7], $cols[4]));
    }
    fclose($s);
    sort($lines);
    $t = fopen($trg, "wb");
    foreach ($lines as $line) { ++ $wcnt; fputcsv($t, $line); }
    fclose($t);
    log_info("Generated: $trg read: $rcnt write: $wcnt");
    return true;
}

/**
 * 全県データを正規化する。
 */
function normalize_area() {

    $rcnt = 0;
    $wcnt = 0;
    $trg = PATH_TMP . "/" . AREA_OUT;
    $t = fopen($trg, "wb");
    $numbers = false;
    $street = "";
    $streetkana = "";
    $s = fopen(PATH_TMP . "/" . AREA_UTF8, "rb");

    while ($cols = fgetcsv($s, 1024, ",")) {
        ++ $rcnt;
        if (0 < preg_match('/\(/', $cols[8])) { $numbers = true; }

        if ($numbers) {
            $street .= $cols[8];
            $streetkana .= $cols[5];
            if (0 < preg_match('/\)/', $cols[8])) { $numbers = false; }
            
            if (!$numbers) {
                $wcnt += split_area_row($t, $cols[2], $cols[0], $street, $streetkana);
                $street = "";
                $streetkana = "";
            }

        } else {
            $wcnt += split_area_row($t, $cols[2], $cols[0], $cols[8], $cols[5]);
        }
    }

    fclose($t);
    fclose($s);

    log_info("Generated: $trg read: $rcnt write: $wcnt");
    return true;
}

/**
 * 全県データの横持ちデータを1件1行に分割する。
 */
function split_area_row($t, $zip, $code, $strt, $kana) {

    $wcnt = 0;
    $note = "";
    $matches = array();
    $strt = preg_replace('/「/', "<", preg_replace('/」/', ">", $strt));

    if (0 < preg_match('/地階・階層不明/', $strt)) {
        $strt = preg_replace('/地階・階層不明/', "階層不明、地階", $strt);
        $kana = preg_replace('/チカイ・カイソウフメイ/', "カイソウフメイ、チカイ", $kana);
    }
    
    if (0 < preg_match('/〔/', $strt)) {
        $strt = preg_replace('/〔/', "、", $strt);
        $strt = preg_replace('/〕/', "", $strt);
        $kana = preg_replace('/^/', "、", $kana);
    }
    
    if (0 < preg_match('/(.*)\((.*)\)(.+)/', $strt, $matches)) {
        $strt = $matches[1] . "{" . $matches[2] . "}" . $matches[3];
    }

    while (0 < preg_match('/(.*<[^>]*)、([^<]*>.*)/', $strt, $matches)) {
        $strt = $matches[1] . ";" . $matches[2];
    }
    while (0 < preg_match('/(.*<[^>]*)、([^<]*>.*)/', $kana, $matches)) {
        $kana = $matches[1] . ";" . $matches[2];
    }
    while (0 < preg_match('/(.*<[^>]*)～([^<]*>.*)/', $strt, $matches)) {
        $strt = $matches[1] . ":" . $matches[2];
    }

    if (0 < preg_match('/～.*\(/', $strt)) {
        $note = $strt;
        $strt = "";
    }

    $org = $strt;
    while (0 < preg_match('/((\(|、)(\d+))～(\d+)/', $strt, $matches)) {

        if (($matches[3] < $matches[4]) &&
            (($matches[4] - $matches[3]) < SPLIT_MAX)) {
            
            $list = $matches[1];
            
            for ($i = $matches[3] + 1; $i <= $matches[4]; ++ $i) {
                $list .= "、" . $i;
            } 

        } else {
            $list = $matches[1] . ":" . $matches[4];
        }
        $matches[1] = preg_replace('/\(/', '\(', $matches[1]);
        $strt = preg_replace('/' . $matches[1] . '～' . $matches[4] . '/',
                             $list, $strt, 1);
        $kana = preg_replace('/' . $matches[1] . '-' . $matches[4] . '/',
                             $list, $kana, 1);

        if ($org == $strt) { break; }
    }

    $strts = preg_split('/(\(|、|\))/', $strt);
    $kanas = preg_split('/(\(|、|\))/', $kana);
    
    if ((0 == preg_match('/\(/', $strt)) && 
        (0 < preg_match('/、/', $strt))) {
        array_unshift($strts, "");
        array_unshift($kanas, "");
    }

    if (!$strts[count($strts) - 1]) {
        array_pop($strts);
        array_pop($kanas);
    }

    if (0 == count($strts)) {
        $strts = array("");
        $kanas = array("");
    }

    if (1 == count($strts)) {
        $wcnt += puts_area_line($t, $zip, $code, $strts[0], "", $kanas[0], "", $note);
    } elseif (2 < count($strts)) {
          
        $prefix = "";
        for ($i = 1; $i < count($strts); ++ $i) {
            if (0 < preg_match('/([^:～]*[^\d\-:～])[\d\-]+(<.*>)*$/',
                                $strts[$i], $matches)) {
                $prefix = $matches[1];
            } elseif (0 < preg_match('/^\d/', $strts[$i])) {
                $strts[$i] = $prefix . $strts[$i];
            } else {
                $prefix = "";
            }
        }
          
        $sufix = "";
        for ($i = count($strts) - 1; 0 < $i; -- $i) {
            $buff = preg_replace('/<.*>/', "", $strts[$i]);
            if (0 < preg_match('/[\d\-]+([^\d\-]+)$/', $buff, $matches)) {
                $sufix = preg_replace('/以上$/', "", $matches[1]);
            } elseif (0 < preg_match('/\d$/', $buff)) {
                $strts[$i] .= $sufix;
            } else {
                $sufix = "";
            }
        }
    }
    
    if (2 < count($kanas)) {

        $prefix = "";
        for ($i = 1; $i < count($kanas); ++ $i) {
            if ((0 < preg_match(
            		'/([^～]*[^\d\-～])[\d\-]+(<.*>)*$/', $kanas[$i], $matches)) &&
                (0 < preg_match('/([^:～]*[^\d\-:～])[\d\-]+(「.*」)*$/', $strts[$i]))) {
                $prefix = $matches[1];
            } else {
                $kanas[$i] = $prefix . $kanas[$i];
            }
        }
              
        $sufix = "";
        for ($i = count($kanas) - 1; 0 < $i; -- $i) {
            $buff = preg_replace('/<.*>/', "", $kanas[$i]);
            if (0 < preg_match('/[\d\-]+([^\d\-]+)$/', $buff, $matches)) {
                $sufix = $matches[1];
            } else {
                $kanas[$i] .= $sufix;
            }
        }
    }

    if (!isset($kanas[0])) {
        $kanas[0] = "";
    }
    for ($i = 1; $i < count($strts); ++ $i) {
        if (!isset($kanas[$i])) {
            $kanas[$i] = "";
        }
        $wcnt += puts_area_line(
            $t, $zip, $code, $strts[0], $strts[$i], $kanas[0], $kanas[$i], $note);
    }
    
    return $wcnt;
}

/**
 * 正規化済みの全県データを所定のフォーマットで保存する。
 */
function puts_area_line($t, $zip, $code, $strt1, $strt2, $kana1, $kana2, $note) {

    $strt1 = preg_replace('/</', "「", preg_replace('/>/', "」", $strt1));
    $strt1 = preg_replace('/\{/', "(", preg_replace('/\}/', ")", $strt1));
    $strt2 = preg_replace('/</', "「", preg_replace('/>/', "」", $strt2));
    $strt2 = preg_replace('/;/', "、", preg_replace('/:/', "～", $strt2));
    $kana1 = preg_replace('/<[^<>]*>/', "", $kana1);
    $kana2 = preg_replace('/<[^<>]*>/', "", $kana2);
    $kana2 = preg_replace('/;/', "、", preg_replace('/:/', "-", $kana2));
    $note  = preg_replace('/;/', "、", preg_replace('/:/', "～", $note));
    
    $matches = array();
    if (0 < preg_match('/(」～|を含む|以上)/', $strt2)) {
        $note .= $strt2;
        $strt2 = "";
    }
    if (0 < preg_match('/「.*/', $strt1, $matches)) {
        $note .= $matches[0];
        $strt1 = preg_replace('/「.*/', "", $strt1);
    }
    if (0 < preg_match('/「.*/', $strt2, $matches)) {
        $note .= $matches[0];
        $strt2 = preg_replace('/「.*/', "", $strt2);
    }
    if (0 < preg_match('/「.*/', $kana1)) {
        $kana1 = preg_replace('/「.*/', "", $kana1);
    }
    if (0 < preg_match('/「.*/', $kana2)) {
        $kana2 = preg_replace('/「.*/', "", $kana2);
    }
    if (0 < preg_match('/(～|・|階層不明|その他|次のビルを除く)/', $strt2)) {
        $note .= $strt2;
        $strt2 = "";
        $kana2 = "";
    }
    if (0 < preg_match('/以下に掲載がない場合/', $strt1)) {
        $note .= $strt1;
        $strt1 = "";
        $kana1 = "";
    }
    if (0 < preg_match('/.+一円$/', $strt1)) {
        $strt1 = "";
        $kana1 = "";
    }

    fputcsv($t, array($zip, $code, $strt1, $strt2, "", $kana1, $kana2, "", $note));
    return 1;
}

/**
 * 事業所データを正規化して所定のフォーマットで保存する。
 */
function normalize_firm() {

    $rcnt = 0;
    $wcnt = 0;
    $trg = PATH_TMP . "/" . FIRM_OUT;
    $t = fopen($trg, "wb");
    $s = fopen(PATH_TMP . "/" . FIRM_UTF8, "rb");
    while ($cols = fgetcsv($s, 1024, ",")) {
        ++ $rcnt;
        $zip = $cols[7];
        $code = $cols[0];
        $strt1 = $cols[5];
        $strt2 = $cols[6];
        $bldg = preg_replace('/\ +\(/', "(", preg_replace('/\)\ +/', ")", $cols[2]));
        $bldn = preg_replace('/\ +\(/', "(", preg_replace('/\)\ +/', ")", $cols[1]));
        ++ $wcnt;
        fputcsv($t, array($zip, $code, $strt1, $strt2, $bldg, "", "", $bldn, ""));
    }
    fclose($s);
    fclose($t);
    log_info("Generated: $trg read: $rcnt write: $wcnt");
    return true;
}

/**
 * 作成したデータを履歴として保存する。
 */
function archive($list) {

    $mtime = date("YmdHis", filemtime(PATH_TMP . "/" . $list[0]));
    foreach ($list as $file) {

        if (substr($file, -4) == ".lzh") {
            move_file(PATH_TMP . "/$file", PATH_ARC . "/${mtime}_$file");
        } else {

            $trg = PATH_ARC . "/${mtime}_${file}.gz";
            $gz = gzopen($trg, "wb");
            $s = fopen(PATH_TMP . "/$file", 'rb');
            while (!feof($s)) {
                $buf = fread($s, 512);
                gzwrite($gz, $buf);
            }
            gzclose($gz);

            file_put_contents("$trg.md5.txt", md5_file($trg));
            file_put_contents("$trg.sha1.txt", sha1_file($trg));

            log_info("Archived : $file");
        }
    }
    return true;
}

/**
 * 履歴データの目次ページを更新する。
 */
function update_arc_index() {
    
    $tmp = PATH_TMP . "/index.html";
    $trg = PATH_ARC . "/index.html";
    $t = fopen($tmp, "wb");
    eval(preg_replace(HTML_BEGIN, 'fwrite(\$t, <<<EOP',
         preg_replace(HTML_END, "\nEOP\n);", ARC_INDEX)));
    fclose($t);
    move_file($tmp, $trg);
    log_info("Updated  : $trg");
    return true;
}
