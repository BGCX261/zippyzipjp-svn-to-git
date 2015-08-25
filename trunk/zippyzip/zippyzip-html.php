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
require_once dirname(__FILE__) . '/zippyzip-html.conf';
require_once dirname(__FILE__) . '/zippyzip-common.php';

log_info(__FILE__);
log_info("Start.");
mkdirs(array(PATH_TMP));

/*
 * HTML データの処理
 */
if (!update_x0402()) { exit(1); }
if (!update_html(PATH_HTML, LINES_HTML)) { exit(1); }
if (!update_html(PATH_MOBILE, LINES_MOBILE)) { exit(1); }

/*
 * 処理完了
 */
log_info("End.");
exit(0);

/**
 * HTML データを作成する。
 */
function update_html($trg, $lines) {

    global $x0401;
    global $x0402;

    $tmp = PATH_TMP . "/" . preg_replace("/.*\//", "", $trg);
    rmdirs(array($tmp));
    mkdirs(array($tmp));

    update_html_prefs($tmp);
    update_html_cities($tmp);
    update_html_meta($tmp);
    
    foreach ($x0402 as $code => $city_name) {
        update_html_detail($tmp, $code, $city_name, $lines);
    }
    log_info("Updated  : " . $tmp . "/??/???.html");

    move_data($tmp, $trg);
    return true;
}

/**
 * HTML 都道府県一覧を作成する。
 */
function update_html_prefs($tmp) {
    
    global $states;
    
    $t = fopen("$tmp/index.html", "wb");
    eval(preg_replace(HTML_BEGIN, 'fwrite(\$t, <<<EOP',
         preg_replace(HTML_END, "\nEOP\n);", HTML_INDEX)));
    fclose($t);
    log_info("Updated  : $tmp/index.html");
}

/**
 * HTML 市区町村一覧を作成する。
 */
function update_html_cities($tmp) {

    global $x0401;
    global $x0402;
        
    foreach ($x0401 as $pref_code => $pref_name) {
        mkdirs(array("$tmp/$pref_code"));
        $t = fopen("$tmp/$pref_code/index.html", "wb");
        eval(preg_replace(HTML_BEGIN, 'fwrite(\$t, <<<EOP',
             preg_replace(HTML_END, "\nEOP\n);", PREF_INDEX)));
        fclose($t);
    }
    log_info("Updated  : $tmp/??/index.html");
}

/**
 * HTML 詳細メタデータを作成する。
 */
function update_html_meta($tmp) {

    $t = FALSE;
    $code = "";
    $s = fopen(PATH_CSV . "/" . AREA_UTF8, "rb");
    while ($cols = fgetcsv($s, 1024, ",")) {
        if ($code != $cols[0]) {
            $code = $cols[0];
            if ($t !== FALSE) { fclose($t); }
            $trg = "$tmp/" . substr($code, 0, 2) . "/" . substr($code,2, 3) . ".csv";
            $t = fopen($trg, "ab");
        }
        $zip = substr($cols[2], 0, 3) . "-" . substr($cols[2], 3, 4);
        fputcsv($t, array($zip, $cols[8], "", "", $cols[5]));
    }
    fclose($s);
    fclose($t);
    
    $t = FALSE;
    $code = "";
    $s = fopen(PATH_CSV . "/" . FIRM_UTF8, "rb");
    while ($cols = fgetcsv($s, 1024, ",")) {
        if ($code != $cols[0]) {
            $code = $cols[0];
            if ($t !== FALSE) { fclose($t); }
            $trg = "$tmp/" . substr($code, 0, 2) . "/" . substr($code,2, 3) . ".csv";
            $t = fopen($trg, "ab");
        }
        $zip = substr($cols[7], 0, 3) . "-" . substr($cols[7], 3, 4);
        fputcsv($t, array($zip, $cols[5], $cols[6], $cols[2], $cols[1]));
    }
    fclose($s);
    fclose($t);
}

/**
 * HTML 詳細ページを作成する。
 */
function update_html_detail($tmp, $code, $city_name, $lines) {

    global $x0401;
    global $x0402;
    global $yomikubun;
    global $yomikubun_s;
    
    $pref_code = substr($code, 0, 2);
    $pref_name = $x0401[$pref_code];
    $file_name = substr($code, 2, 3);
    $areas = array();
    $firms = array();
    $appendix = 0;
    $firmindex = array();
    
    generate_html_detail($tmp, $code, $city_name, $areas, $firms, $appendix);
    
    $category = array(
        BUNKYO_TITLE   => 0,
        IRYO_TITLE     => 0,
        KANTYO_TITLE   => 0,
        KINYU_TITLE    => 0,
        EISUKANA_TITLE => 0,
        KANJI_TITLE    => 0,
        MISC_TITLE     => 0,
    );
    
    for ($i = 0; $i < count($firms); ++ $i) {
        if (0 < preg_match("/(".IRYO_INCLUDE.")/", $firms[$i]["bldg"])) {
            $cat = IRYO_TITLE;
        } elseif ((0 < preg_match("/(".BUNKYO_INCLUDE.")/", $firms[$i]["bldg"])) &&
                  (0 == preg_match("/(".BUNKYO_EXCLUDE.")/", $firms[$i]["bldg"]))) {
            $cat = BUNKYO_TITLE;
        } elseif ((0 < preg_match("/(".KINYU_INCLUDE.")/", $firms[$i]["bldg"])) &&
                  (0 == preg_match("/(".KINYU_EXCLUDE.")/", $firms[$i]["bldg"]))) {
            $cat = KINYU_TITLE;
        } elseif (0 < preg_match("/(".KAISHA_INCLUDE.")/", $firms[$i]["bldg"])) {
            if ((0 < preg_match("/^(".KABUSIKIGAISYA.")*\ *(".ZENKAKU_KATAKANA
                    ."|".ZENKAKU_HIRAGANA."|[0-1A-Za-z])/", $firms[$i]["bldg"]))) {                            
                $cat = EISUKANA_TITLE;
            } else {
                $cat = KANJI_TITLE;
            }
        } elseif ((((0 < preg_match("/(".KANTYO_INCLUDE.")/", $firms[$i]["bldg"])) &&
             (0 == preg_match("/(".KANTYO_EXCLUDE.")/", $firms[$i]["bldg"]))) ||
             (0 < preg_match("/(".KANTYO_INC_EX1.")/", $firms[$i]["bldg"])))) {
            $cat = KANTYO_TITLE;
        } else {    
            $cat = MISC_TITLE;
        }
        $firms[$i]['category'] = $cat;
        ++ $category[$cat];
    }

    $yomiindex = array();
    $firmindex = array();
        
    if ($lines < count($areas)) {
        
        $cnt = 0;
        foreach ($areas as $item) {
            if (($item['yomi'] != "a") && ($item['yomi'] != "k")) { continue; }
            ++ $cnt;
        }
        
        if ($cnt <= $lines) {
            
            for ($i = 0; $i < count($areas); ++$i) {
                $areas[$i]['yomi'] = $yomikubun_s[$areas[$i]['yomi']][1];
                if ($areas[$i]['yomi'] == "@") { continue; }
                if (isset($yomiindex[$areas[$i]['yomi']])) { continue; }
                $yomiindex[$areas[$i]['yomi']] = $yomikubun_s[$areas[$i]['yomi']][0];
            }
        } else {
            foreach ($areas as $item) {
                if ($item['yomi'] == "@") { continue; }
                if (isset($yomiindex[$item['yomi']])) { continue; }
                $yomiindex[$item['yomi']] = $yomikubun[$item['yomi']][0];
            }
        }
    }
    
    $is_firm_page = true;
    
    if (((count($areas) + count($firms)) <= $lines) || (0 == count($firms))) {
        
        $is_firm_page = false;
        
    } elseif (count($firms) <= $lines) {
        ++ $appendix;
        array_push($firmindex, array(
            'ttl' => "事業所",
            'url' => "$file_name-$appendix.html",
            'cat' => array(
                BUNKYO_TITLE   => $category[BUNKYO_TITLE],
                IRYO_TITLE     => $category[IRYO_TITLE],
                KANTYO_TITLE   => $category[KANTYO_TITLE],
                KINYU_TITLE    => $category[KINYU_TITLE],
                EISUKANA_TITLE => $category[EISUKANA_TITLE],
                KANJI_TITLE    => $category[KANJI_TITLE],
                MISC_TITLE     => $category[MISC_TITLE],
            )
        ));
    } else {
        $cnt = $category[KINYU_TITLE] + $category[EISUKANA_TITLE]
             + $category[KANJI_TITLE];

        if ($cnt == 0) {
        
        } elseif ($cnt <= $lines) {
            ++ $appendix;
            array_push($firmindex, array(
                'ttl' => KAISHA_TITLE,
                'url' => "$file_name-$appendix.html",
                'cat' => array(
                    KINYU_TITLE    => $category[KINYU_TITLE],
                    EISUKANA_TITLE => $category[EISUKANA_TITLE],
                    KANJI_TITLE    => $category[KANJI_TITLE],
                )
            ));
        } else {
            if (0 < $category[KINYU_TITLE]) {
                ++ $appendix;
                array_push($firmindex, array(
                    'ttl' => KINYU_TITLE,
                    'url' => "$file_name-$appendix.html",
                    'cat' => array(KINYU_TITLE    => $category[KINYU_TITLE])
                ));
            }
            if (0 < $category[EISUKANA_TITLE]) {
                ++ $appendix;
                array_push($firmindex, array(
                    'ttl' => EISUKANA_TITLE,
                    'url' => "$file_name-$appendix.html",
                    'cat' => array(EISUKANA_TITLE => $category[EISUKANA_TITLE])
                ));
            }
            
            if (0 < $category[KANJI_TITLE]) {
                ++ $appendix;
                array_push($firmindex, array(
                    'ttl' => KANJI_TITLE,
                    'url' => "$file_name-$appendix.html",
                    'cat' => array(KANJI_TITLE    => $category[KANJI_TITLE])
                ));
            }
        }

        $cnt = $category[BUNKYO_TITLE] + $category[IRYO_TITLE]
             + $category[KANTYO_TITLE];

        if ($cnt == 0) {
        
        } elseif ($cnt <= $lines) {
            ++ $appendix;
            array_push($firmindex, array(
                'ttl' => KOKYO_TITLE,
                'url' => "$file_name-$appendix.html",
                'cat' => array(
                    BUNKYO_TITLE   => $category[BUNKYO_TITLE],
                    IRYO_TITLE     => $category[IRYO_TITLE],
                    KANTYO_TITLE   => $category[KANTYO_TITLE],
                )
            ));
        } else {
            $cnt = $category[BUNKYO_TITLE] + $category[IRYO_TITLE];

            if ($cnt == 0) {
            
            } elseif ($cnt <= $lines) {
                ++ $appendix;
                array_push($firmindex, array(
                    'ttl' => BUNKYO_TITLE."・".IRYO_TITLE,
                    'url' => "$file_name-$appendix.html",
                    'cat' => array(
                        BUNKYO_TITLE   => $category[BUNKYO_TITLE],
                        IRYO_TITLE     => $category[IRYO_TITLE],
                    )
                ));
            } else {
                if (0 < $category[BUNKYO_TITLE]) {
                    ++ $appendix;
                    array_push($firmindex, array(
                        'ttl' => BUNKYO_TITLE,
                        'url' => "$file_name-$appendix.html",
                        'cat' => array(BUNKYO_TITLE   => $category[BUNKYO_TITLE])
                    ));
                }
                if (0 < $category[IRYO_TITLE]) {
                    ++ $appendix;
                    array_push($firmindex, array(
                        'ttl' => IRYO_TITLE,
                        'url' => "$file_name-$appendix.html",
                        'cat' => array(IRYO_TITLE     => $category[IRYO_TITLE])
                    ));
                }
            }
            if (0 < $category[KANTYO_TITLE]) {
                ++ $appendix;
                array_push($firmindex, array(
                    'ttl' => KANTYO_TITLE,
                    'url' => "$file_name-$appendix.html",
                    'cat' => array(KANTYO_TITLE   => $category[KANTYO_TITLE])
                ));
            }
        }
        if (0 < $category[MISC_TITLE]) {
            ++ $appendix;
            array_push($firmindex, array(
                'ttl' => MISC_TITLE,
                'url' => "$file_name-$appendix.html",
                'cat' => array(MISC_TITLE     => $category[MISC_TITLE])
            ));
        }
    }
        
    foreach ($yomiindex as $yomi_key => $yomi_ttl) {
        $t = fopen("$tmp/$pref_code/$file_name-$yomi_key.html", "wb");
        eval(preg_replace(HTML_BEGIN, 'fwrite(\$t, <<<EOP',
             preg_replace(HTML_END, "\nEOP\n);", CITY_YOMI)));
        fclose($t);
    }

    $yomi_key = "@";
    $t = fopen("$tmp/$pref_code/$file_name.html", "wb");
    eval(preg_replace(HTML_BEGIN, 'fwrite(\$t, <<<EOP',
         preg_replace(HTML_END, "\nEOP\n);", CITY_DETAIL)));
    fclose($t);
    
    if ($is_firm_page) {

        foreach ($firmindex as $item) {
            
            $title    = $item['ttl'];
            $url      = $item['url'];
            $category = $item['cat'];
    
            $t = fopen("$tmp/$pref_code/$url", "wb");
            eval(preg_replace(HTML_BEGIN, 'fwrite(\$t, <<<EOP',
                 preg_replace(HTML_END, "\nEOP\n);", FIRM_DETAIL)));
            fclose($t);
        }
        
        foreach ($firms as $item) {
            $t = fopen("$tmp/$pref_code/{$item['zip']}.html", "wb");
            eval(preg_replace(HTML_BEGIN, 'fwrite(\$t, <<<EOP',
                 preg_replace(HTML_END, "\nEOP\n);", FIRM_ADDRESS)));
            fclose($t);
        }
    }
}

/**
 * HTML 詳細データを作成する。
 */
function generate_html_detail($tmp, $code, $city_name, &$areas, &$firms, &$appendix) {
    
    global $yomikubun;

    $pref_code = substr($code, 0, 2);
    $file_name = substr($code, 2, 3);
    $floors = array();
    $add1_shrink = "";
    
    $numbers = false;
    $street = "";
    $src = "$tmp/$pref_code/$file_name.csv";
    $s = fopen($src, "rb");
    
    while ($cols = fgetcsv($s, 1024, ",")) {

        if (0 < preg_match('/\(/', $cols[1])) {

            $numbers = true;
        
            foreach ($yomikubun as $key => $val) {
                if (0 == preg_match($val[1], $cols[4])) { continue; }
                $yomi = $key;
                break;
            }
        } elseif ($numbers) {
        } else {
            
            foreach ($yomikubun as $key => $val) {
                if (0 == preg_match($val[1], $cols[4])) { continue; }
                $yomi = $key;
                break;
            }
        }

        $matches = array();
        if (0 < preg_match('/(.*)(\([^\(\)]*\))$/', $cols[1], $matches)) {
            $numbers = false;
            if (($add1_shrink != "") && ($matches[1] != $add1_shrink)) {
                $add1_shrink = "";
                update_html_floores($tmp, $code, $city_name, $floors, $appendix);
            }
            if (0 < preg_match("/階層不明/", $matches[2])) {
                $add1_shrink = $matches[1];
                ++ $appendix;
                $floors = array();
                array_push($areas, array(
                    "link" => "$file_name-$appendix.html",
                    "add1" => $matches[1],
                    "yomi" => $yomi,
                ));
            }
            if ($add1_shrink != "") {
                array_push($floors, array(
                    "zip" => $cols[0],
                    "add1" => $matches[1],
                    "add2" => preg_replace("/[\(\)]/", "", $matches[2]),
                	"yomi" => $yomi,
                ));
            } else {
                array_push($areas, array(
                    "zip" => $cols[0],
                    "add1" => $matches[1],
                    "add2" => $matches[2],
                    "yomi" => $yomi,
                ));
            }
        } elseif ($cols[3] != "") {
            array_push($firms, array(
                "zip" => $cols[0],
                "add1" => $cols[1],
                "add2" => $cols[2],
                "bldg" => preg_replace("/\ /", "", $cols[3]),
                "yomi" => $yomi,
            ));
        } elseif ($numbers) {
            $street .= $cols[1];
            if (0 < preg_match('/\)/', $cols[1])) { $numbers = false; }
            
            if (!$numbers) {
                $matches = array();
                if (0 < preg_match('/(.*)(\([^\(\)]*\))$/', $street, $matches)) {
                    array_push($areas, array(
                        "zip" => $cols[0],
                        "add1" => $matches[1],
                        "add2" => $matches[2],
                        "yomi" => $yomi,
                    ));
                } else {                        
                    array_push($areas, array(
                        "zip" => $cols[0],
                        "add1" => $street,
                        "add2" => "",
                        "yomi" => $yomi,
                    ));
                }
                $street = "";
            }

        } else {
            array_push($areas, array(
                "zip" => $cols[0],
                "add1" => $cols[1],
                "add2" => "",
                "yomi" => $yomi,
            ));
        }
    }
    
    fclose($s);
    unlink_files(array($src));

    if (0 < count($floors)) {
        update_html_floores($tmp, $code, $city_name, $floors, $appendix);
    }
}

/**
 * HTML フロア毎ページを作成する。
 */
function update_html_floores($tmp, $code, $city_name, $floors, $appendix) {

    global $x0401;
    global $x0402;
        
    $pref_code = substr($code, 0, 2);
    $pref_name = $x0401[$pref_code];
    $file_name = substr($code, 2, 3);
    $t = fopen("$tmp/$pref_code/$file_name-$appendix.html", "wb");
    eval(preg_replace(HTML_BEGIN, 'fwrite(\$t, <<<EOP',
         preg_replace(HTML_END, "\nEOP\n);", FLOORS_DETAIL)));
    fclose($t);
}
