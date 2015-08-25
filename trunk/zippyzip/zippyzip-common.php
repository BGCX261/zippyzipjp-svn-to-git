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

define("BUFSIZ",  8192);

// 都道府県コード
$x0401 = array(
  "01" => "北海道",
  "02" => "青森県",
  "03" => "岩手県",
  "04" => "宮城県",
  "05" => "秋田県",
  "06" => "山形県",
  "07" => "福島県",
  "08" => "茨城県",
  "09" => "栃木県",
  "10" => "群馬県",
  "11" => "埼玉県",
  "12" => "千葉県",
  "13" => "東京都",
  "14" => "神奈川県",
  "15" => "新潟県",
  "16" => "富山県",
  "17" => "石川県",
  "18" => "福井県",
  "19" => "山梨県",
  "20" => "長野県",
  "21" => "岐阜県",
  "22" => "静岡県",
  "23" => "愛知県",
  "24" => "三重県",
  "25" => "滋賀県",
  "26" => "京都府",
  "27" => "大阪府",
  "28" => "兵庫県",
  "29" => "奈良県",
  "30" => "和歌山県",
  "31" => "鳥取県",
  "32" => "島根県",
  "33" => "岡山県",
  "34" => "広島県",
  "35" => "山口県",
  "36" => "徳島県",
  "37" => "香川県",
  "38" => "愛媛県",
  "39" => "高知県",
  "40" => "福岡県",
  "41" => "佐賀県",
  "42" => "長崎県",
  "43" => "熊本県",
  "44" => "大分県",
  "45" => "宮崎県",
  "46" => "鹿児島県",
  "47" => "沖縄県",
);

// 市区町村コード
$x0402 = array();

/**
 * 市区町村コードを格納した変数を更新する。
 */
function update_x0402() {

    global $x0402;

    $s = fopen(PATH_CSV . "/" . X0402_OUT, "rb");
    while ($cols = fgetcsv($s, 1024, ",")) {
        $x0402[$cols[0]] = $cols[1];
    }
    fclose($s);
    ksort($x0402);
    return true;
}

/**
 * ログを出力する。
 */
function log_common($message, $level) {
    $level = substr($level . "        ", 0, 8);
    $ts = date("Y/m/d H:i:s");
    echo "$ts $level $message\n";
}

/**
 * 「情報」レベルのログを出力する。
 */
function log_info($message) {
    log_common($message, "INFO");
}

/**
 * 「警告」レベルのログを出力する。
 */
function log_warn($message) {
    log_common($message, "WARN");
}

/**
 * 「エラー」レベルのログを出力する。
 */
function log_error($message) {
    log_common($message, "ERROR");
}

/**
 * 「致命的」レベルのログを出力する。
 */
function log_fatal($message) {
    log_common($message, "FATAL");
}

/**
 * ディレクトリが存在しなければ作成する。
 */
function mkdirs($dirs) {
    foreach ($dirs as $dir) {
        if (!is_dir($dir)) {
            if (!mkdir($dir, 0777, true)) {
                log_fatal("Faild to mkdir: $dir");
                exit(1);
            }
        }
    }
}

/**
 * ディレクトリが存在すれば削除する。
 * 内容のあるディレクトリも削除するために OS のコマンドを使用する。
 */
function rmdirs($dirs) {
    foreach ($dirs as $dir) {
        if (is_dir($dir)) {
            $return_var = 0;
            $output = array();
            exec(RMDIR_COMMAND . $dir, $output, $return_var);
            if ($return_var != 0) {
                log_fatal("Faild to rmdir: $dir");
                exit(1);
            }
        }
    }
}

/**
 * ファイルが存在すれば削除する。
 */
function unlink_files($files) {
    foreach ($files as $file) {
        if (is_file($file)) {
            if (!unlink($file)) {
                log_fatal("Faild to unlink: $file");
                exit(1);
            }
        }
    }
}

/**
 * ファイルを移動する。
 * 移動元がファイルで、移動先がディレクトリの場合は、そのディレクトリの下にファイルを移動する。
 */
function move_file($src, $trg) {
    
    if (!is_file($src) && !is_dir($src)) {
        log_fatal("Faild to rename: $src => $trg -- missing $src");
        exit(1);
    }
    if (is_file($src) && is_dir($trg)) {
        $base_name = basename($src);
        $trg .= "/$base_name";
    }
    if (is_dir($trg)) {
        rmdirs(array($trg));
    } elseif (is_file($trg)) {
        unlink_files(array($trg));
    }
    if (!rename($src, $trg)) {
        log_fatal("Faild to rename: $src => $trg");
        exit(1);
    }
}

/**
 * ワークデータを所定のディレクトリに移動する。
 */
function move_data($src, $trg) {
    if (is_dir($trg)) { move_file($trg, "${trg}_bak"); }
    move_file($src, $trg);
    rmdirs(array("${trg}_bak"));
    return true;
}
