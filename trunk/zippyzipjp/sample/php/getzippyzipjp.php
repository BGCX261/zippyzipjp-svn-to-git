<?php
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

require_once 'getzippyzipjp_conf.php';

date_default_timezone_set('Asia/Tokyo');

echo date(DATE_RFC822) . " start.\n";

// JSON細切れデータの圧縮ファイルを保存するディレクトリを確認する。
if (!is_dir(ARCDIR)) {
    mkdir(ARCDIR, 0777, true);
}

if (!is_dir(ARCDIR)) {
    echo "Error: Faild to mkdir " . ARCDIR . "\n";
    return;
}

// JSON細切れデータを保存するディレクトリを確認する。
if (!is_dir(TARGET)) {
    mkdir(TARGET, 0777, true);
}

if (!is_dir(TARGET)) {
    echo "Error: Faild to mkdir " . TARGET . "\n";
    return;
}

// 更新情報を取得する。
$prev_path = ARCDIR . "/" . substr($url, strrpos($url, '/') + 1);
$check = file_get_contents(CHECK);
$prev = is_file($prev_path) ? file_get_contents($prev_path) : "";

// 更新されていなければ終了。
if ($check == $prev) {
    echo "exit.\n";
    return;
}

// 更新情報を保存する。
copy(CHECK, $prev_path);

$doc = new DOMDocument();
$doc->load(PREV);
$list = $doc->getElementsByTagNameNS('http://www.w3.org/1999/xhtml', 'a');

// 圧縮ファイルを取得して解凍する。
foreach ($list as $a) {
    
    $url = $a->getAttribute('href');
    
    if (false === strstr($url, '/json')) { continue; }
    
    $name = substr($url, strrpos($url, '/json') + 1);
    
    if (copy($url, ARCDIR . "/" . $name)) {
        
        echo date(DATE_RFC822) . " " . $name . " OK. " . filesize(ARCDIR . "/" . $name) .  " byte\n";
        
        $zip = new ZipArchive;
        
        if ($zip->open(ARCDIR . "/" . $name)) {
            
            $zip->extractTo(TARGET);
            $zip->close();
        
        } else {
            echo "Error: Failed to open archive: " . $name . "\n";
        }

    } else {
        echo "Error: Failed to fetch archive: " . $name . "\n";
    }
}

echo date(DATE_RFC822) . " complete.\n";

?>
