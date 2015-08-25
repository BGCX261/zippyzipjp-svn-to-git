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
require_once dirname(__FILE__) . '/zippyzip-fragment.conf';
require_once dirname(__FILE__) . '/zippyzip-common.php';

log_info(__FILE__);
log_info("Start.");
mkdirs(array(PATH_TMP));
$updated = false;

/*
 * CSV データと JSON データの処理
 */
if (GENERATE_CSV_FRAGMENTS || GENERATE_JSON) {
    if (!update_x0402()) { exit(1); }
    if (!generate_work()) { exit(1); }
    if (GENERATE_CSV_FRAGMENTS) { if (!update_csv()) { exit(1); } }
    if (GENERATE_JSON) { if (!update_json()) { exit(1); } }
    rmdirs(array(PATH_TMP . "/work"));
}
    
/*
 * 処理完了
 */
log_info("End.");
exit(0);

/**
 * ワークファイルを生成する。
 */
function generate_work() {
    
    $rcnt = 0;
    $wcnt = 0;
    
    rmdirs(array(PATH_TMP . "/work"));
    mkdirs(array(PATH_TMP . "/work"));
    $src_list = array(PATH_CSV . "/" . AREA_OUT, PATH_CSV . "/" . FIRM_OUT);

    $trg = PATH_TMP . "/work/zip";
    mkdirs(array($trg));
    $ret = generate_work_files($src_list, $trg, "", 0, 2);
    if (!$ret) { return false; }
    $rcnt = $ret[0];
    $wcnt = $ret[1];
    log_info("Genetated: work/zip/???.csv read: $rcnt write: $wcnt");
    
    $trg = PATH_TMP . "/work/pref";
    mkdirs(array($trg));

    $ret = generate_work_files($src_list, $trg, "", 8, 10);
    if (!$ret) { return false; }
    $rcnt = $ret[0];
    $wcnt = $ret[1];
    log_info("Genetated: work/pref/???.csv read: $rcnt write: $wcnt");
    
    return true;
}

/**
 * 各レベルのワークファイルを生成する。
 */
function generate_work_files($src_list, $trg, $name, $col, $limit) {
    
    $rcnt = 0;
    $wcnt = 0;
    
    for ($i = 0; $i <= 9; ++ $i) {

        $out = "$trg/$name$i.csv";

        foreach ($src_list as $src) {

            $s = fopen($src, "rb");
            $t = fopen($out, "ab");
            while (!feof($s)) {
                $line = fgets($s);
                if (!$line) { continue; }
                ++ $rcnt;
                if (substr($line, $col, 1) != $i) { continue; }
                ++ $wcnt;
                fwrite($t, $line);
            }
            fclose($s);
            fclose($t);
        }

        if ($col == $limit) {
            if (0 == filesize($out)) { unlink_files(array($out)); }
        } else {
            $ret = generate_work_files(array($out), $trg, "$name$i", $col + 1, $limit);
            $rcnt += $ret[0];
            $wcnt += $ret[1];
            unlink_files(array($out));
        }
    }

    return array($rcnt, $wcnt);
}

/**
 * CSV データを作成する。
 */
function update_csv() {
    rmdirs(array(PATH_TMP_CSV));
    mkdirs(array(PATH_TMP_CSV, PATH_TMP_CSV_ZIP, PATH_TMP_CSV_PREF));
    if (!extract_csv_zip())  { return false; }
    if (!fill_csv_zip())     { return false; }
    if (!extract_csv_city()) { return false; }
    if (!fill_csv_pref())    { return false; }
    move_data(PATH_TMP_CSV_ZIP, PATH_CSV_ZIP);
    move_data(PATH_TMP_CSV_PREF, PATH_CSV_PREF);
    rmdirs(array(PATH_TMP_CSV));
    return true;
} 

/**
 * 各郵便番号上3桁について、郵便番号下4桁の CSV データを抽出する。
 */
function extract_csv_zip() {

    $rcnt = 0;
    $wcnt = 0;
    foreach (glob(PATH_TMP . "/work/zip/*") as $src) {
        $zip1 = substr(basename($src), 0, 3);
        $data = array();
        $s = fopen($src, "rb");
        while (!feof($s)) {
            $line = fgets($s);
            if (!$line) { continue; }
            ++ $rcnt;
            $zip2 = substr($line, 3, 4);
            array_push($data, $zip2);
        }
        fclose($s);
        
        $data = array_unique($data);
        sort($data);
        $t = fopen(PATH_TMP_CSV_ZIP . "/$zip1.csv", "ab");
        foreach ($data as $zip2) {
            ++ $wcnt;
            fwrite($t, "$zip2\n");
        }
        fclose($t);
    }
    
    log_info("Generated: ???.csv read: $rcnt write: $wcnt");
    return true;
}

/**
 * 郵便番号ごとの CSV データを保存する。
 */
function fill_csv_zip() {

    global $x0401;
    global $x0402;

    $rcnt = 0;
    $wcnt = 0;
    foreach (glob(PATH_TMP . "/work/zip/*") as $src) {
        
        $zip1 = substr(basename($src), 0, 3);
        mkdirs(array(PATH_TMP_CSV_ZIP . "/$zip1"));

        $data = array();
        $s = fopen($src, "rb");
        while ($cols = fgetcsv($s, 1024, ",")) {
            ++ $rcnt;
            $zip2 = substr($cols[0], 3, 4);
            if (!isset($data[$zip2])) {
                $data[$zip2] = array();
            }
            array_push($data[$zip2], $cols);
        }
        fclose($s);
        
        foreach ($data as $zip2 => $lines) {

            $t = fopen(PATH_TMP_CSV_ZIP . "/$zip1/$zip2.csv", "wb");
            
            foreach ($lines as $cols) {

                if (!isset($x0402[$cols[1]])) {
                    log_error("Failed to query JIS X 0402 code: ${cols[1]}");
                    continue;
                }
                ++ $wcnt;
                fputcsv($t, array($cols[1], $x0401[substr($cols[1], 0, 2)],
                        $x0402[$cols[1]], $cols[2], $cols[3], $cols[4], $cols[8]));
            }
            
            fclose($t);
        }
    }

    log_info("Generated: ???/????.csv read: $rcnt write: $wcnt");
    return true;
}

/**
 * 各県について、行政区の CSV データを抽出する。
 */
function extract_csv_city() {

    global $x0401;
    global $x0402;

    $wcnt = 0;
    foreach ($x0401 as $pref_code => $pref_name) {
        
        $data = array();
        foreach ($x0402 as $city_code => $city_name) {
            if (substr($city_code, 0, 2) != $pref_code) { continue; }
            $data[$city_code] = $city_name;
        }

        $t = fopen(PATH_TMP_CSV_PREF . "/$pref_code.csv", "ab");
        foreach ($data as $code => $name) {
            ++ $wcnt;
            fputcsv($t, array($code, $name));
        }
        fclose($t);
        $wcnt += count($data);
    }

    log_info("Generated: ??.csv write: $wcnt");
    return true;
}

/**
 * 行政区ごとの CSV データを保存する。
 */
function fill_csv_pref() {

    $rcnt = 0;
    $wcnt = 0;
    foreach (glob(PATH_TMP . "/work/pref/*") as $src) {
        
        $pref = substr(basename($src), 0, 2);
        mkdirs(array(PATH_TMP_CSV_PREF . "/$pref"));

        $data = array();
        $s = fopen($src, "rb");
        while ($cols = fgetcsv($s, 1024, ",")) {
            ++ $rcnt;
            $code = substr($cols[1], 2, 3);
            if (!isset($data[$code])) {
                $data[$code] = array();
            }
            array_push($data[$code], $cols);
        }
        fclose($s);
        
        foreach ($data as $code => $lines) {

            $t = fopen(PATH_TMP_CSV_PREF . "/$pref/$code.csv", "wb");
            
            foreach ($lines as $cols) {
                ++ $wcnt;
                fputcsv($t, array($cols[0], $cols[2], $cols[3], $cols[4], $cols[8]));
            }
            
            fclose($t);
        }
    }

    log_info("Generated: ??/???.csv read: $rcnt write: $wcnt");
    return true;
}

/**
 * JSON データを作成する。
 */
function update_json() {
    rmdirs(array(PATH_TMP_JSON));
    mkdirs(array(PATH_TMP_JSON, PATH_TMP_JSON_ZIP, PATH_TMP_JSON_PREF));
    if (!extract_json_zip())  { return false; }
    if (!fill_json_zip())     { return false; }
    if (!fill_json_prefs())   { return false; }
    if (!extract_json_city()) { return false; }
    if (!fill_json_pref())    { return false; }
    move_data(PATH_TMP_JSON, PATH_JSON);
    return true;
} 

/**
 * 各郵便番号上3桁について、郵便番号下4桁の JSON データを抽出する。
 */
function extract_json_zip() {

    $rcnt = 0;
    $wcnt = 0;
    foreach (glob(PATH_TMP . "/work/zip/*") as $src) {
        $zip1 = substr(basename($src), 0, 3);
        $data = array();
        $s = fopen($src, "rb");
        while (!feof($s)) {
            $line = fgets($s);
            if (!$line) { continue; }
            ++ $rcnt;
            $zip2 = substr($line, 3, 4);
            array_push($data, $zip2);
        }
        fclose($s);
        
        $data = array_unique($data);
        sort($data);
        $t = fopen(PATH_TMP_JSON_ZIP . "/$zip1.json", "ab");
        fwrite($t, json_encode(array(
        	"zip1" => $zip1,
        	"zip2list" => $data,
        )));
        $wcnt += count($data);
        fclose($t);
    }
    
    log_info("Generated: ???.json read: $rcnt write: $wcnt");
    return true;
}

/**
 * 郵便番号ごとの JSON データを保存する。
 */
function fill_json_zip() {

    global $x0401;
    global $x0402;

    $rcnt = 0;
    $wcnt = 0;
    foreach (glob(PATH_TMP . "/work/zip/*") as $src) {
        
        $zip1 = substr(basename($src), 0, 3);
        mkdirs(array(PATH_TMP_JSON_ZIP . "/$zip1"));

        $data = array();
        $s = fopen($src, "rb");
        while ($cols = fgetcsv($s, 1024, ",")) {
            ++ $rcnt;
            if (!isset($x0402[$cols[1]])) {
                log_error("Failed to query JIS X 0402 code: ${cols[1]}");
                continue;
            }
            $zip2 = substr($cols[0], 3, 4);
            if (!isset($data[$zip2])) {
                $data[$zip2] = array();
            }
            array_push($data[$zip2], array(
                "x0402" => $cols[1],
                "pref" => $x0401[substr($cols[1], 0, 2)],
                "city" => $x0402[$cols[1]],
                "add1" => $cols[2],
                "add2" => $cols[3],
				"bldg" => $cols[4],
				"note" => $cols[8],
            ));
        }
        fclose($s);
        
        foreach ($data as $zip2 => $recs) {

            $t = fopen(PATH_TMP_JSON_ZIP . "/$zip1/$zip2.json", "wb");
            fwrite($t, json_encode(array(
            	"zip1" => $zip1,
            	"zip2" => $zip2,
                "addresslist" => $recs,
            )));
            fclose($t);
            $wcnt += count($recs);
        }
    }

    log_info("Generated: ???/????.json read: $rcnt write: $wcnt");
    return true;
}

/**
 * 各県について、行政区の JSON データを抽出する。
 */
function extract_json_city() {

    global $x0401;
    global $x0402;

    $wcnt = 0;
    foreach ($x0401 as $pref_code => $pref_name) {
        
        $data = array();
        foreach ($x0402 as $city_code => $city_name) {
            if (substr($city_code, 0, 2) != $pref_code) { continue; }
            array_push($data, array(
            	"x0402" => $city_code,
                "city" => $city_name,
            ));
        }

        $t = fopen(PATH_TMP_JSON_PREF . "/$pref_code.json", "ab");
        fwrite($t, json_encode(array(
            "x0401" => $pref_code,
            "citylist" => $data,
        )));
        fclose($t);
        $wcnt += count($data);
    }

    log_info("Generated: ??.json write: $wcnt");
    return true;
}

/**
 * 都道府県の JSON データを作成する。
 */
function fill_json_prefs() {

    global $x0401;
    
    $data = array();
    foreach ($x0401 as $code => $name) {
        array_push($data, array(
                    "x0401" => $code,
                    "name" => $name,
        ));
    }

    $t = fopen(PATH_TMP_JSON_PREF . "/prefs.json", "wb");
    fwrite($t, json_encode($data));
    fclose($t);
    
    log_info("Generated: prefs.json");
    return true;
}

/**
 * 行政区ごとの JSON データを保存する。
 */
function fill_json_pref() {

    global $x0401;
    global $x0402;
    
    $rcnt = 0;
    $wcnt = 0;
    foreach (glob(PATH_TMP . "/work/pref/*") as $src) {
        
        $pref = substr(basename($src), 0, 2);
        mkdirs(array(PATH_TMP_JSON_PREF . "/$pref"));

        $data = array();
        $s = fopen($src, "rb");
        while ($cols = fgetcsv($s, 1024, ",")) {
            ++ $rcnt;
            if (!isset($data[$cols[1]])) {
                $data[$cols[1]] = array();
            }
            array_push($data[$cols[1]],  array(
                "zip" => $cols[0],
                "add1" => $cols[2],
                "add2" => $cols[3],
				"bldg" => $cols[4],
				"note" => $cols[8],
            ));
        }
        fclose($s);
        
        foreach ($data as $code => $recs) {

            if (!isset($x0402[$code])) { continue; }
            $t = fopen(
                PATH_TMP_JSON_PREF . "/$pref/" . substr($code, 2, 3) . ".json", "wb");
            fwrite($t, json_encode(array(
                "x0402" => $code,
                "pref" => $x0401[substr($code, 0, 2)],
                "city" => $x0402[$code],
                "addresslist" => $recs,
            )));
            fclose($t);
            $wcnt += count($recs);
        }
    }

    log_info("Generated: ??/???.json read: $rcnt write: $wcnt");
    return true;
}
