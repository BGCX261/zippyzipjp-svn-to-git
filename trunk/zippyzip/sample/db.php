<!-- 
 * CSVデータでDBテーブルを更新するサンプル
 * 
 * Copyright 2010 Michinobu Maeda.
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
 -->
<?php
$dsn = 'mysql:dbname=zippyzip;host=localhost';
$user = 'zippyzip';
$password = 'password';
$message = "CSV データの URL を入力して OK ボタンを押してください。";

if (isset($_GET['x0401_submit'])) {
    
    $csv = fopen($_GET['url'] , 'r');
    
    if ($csv === FALSE) {
        $message = "CSV ファイルを開くことができません。";
    } else {        
    
        try {
            $dbh = new PDO($dsn, $user, $password);
            
            $dbh->exec('set names utf8');
            $dbh->beginTransaction();
            $dbh->exec('delete from pref');
            $count = 0;
            $stmt = $dbh->prepare(
                'insert into pref (pref_code, pref_name, pref_kana)' .
                ' values (?, ?, ?)');
            
            while (($rec = fgetcsv($csv)) !== FALSE) {
                $count += $stmt->execute(array($rec[0], $rec[1], $rec[2]));
            }
            
            $dbh->commit();
            $message = "都道府県を更新しました。${count}行";
        
        } catch (PDOException $e) {
            $message = "データベースの接続に失敗しました。 " . $e->getMessage();
        }
        
        fclose($csv);
    }
} else if (isset($_GET['x0402_submit'])) {
    
    $csv = fopen($_GET['url'] , 'r');
    
    if ($csv === FALSE) {
        $message = "CSV ファイルを開くことができません。";
    } else {        
    
        try {
            $dbh = new PDO($dsn, $user, $password);
            
            $dbh->exec('set names utf8');
            $dbh->beginTransaction();
            $dbh->exec('delete from city');
            $count = 0;
            $stmt = $dbh->prepare(
                'insert into city (pref_code, city_code, city_name, city_kana)' .
                ' values (?, ?, ?, ?)');
            
            while (($rec = fgetcsv($csv)) !== FALSE) {
                $count += $stmt->execute(
                    array(substr($rec[0], 0, 2), substr($rec[0], 2, 3), $rec[1], $rec[2]));
            }
            
            $dbh->commit();
            $message = "市区町村を更新しました。${count}行";
        
        } catch (PDOException $e) {
            $message = "データベースの接続に失敗しました。 " . $e->getMessage();
        }
        
        fclose($csv);
    }
} else if (isset($_GET['area_submit'])) {
    
    $csv = fopen($_GET['url'] , 'r');
    
    if ($csv === FALSE) {
        $message = "CSV ファイルを開くことができません。";
    } else {        
    
        try {
            $dbh = new PDO($dsn, $user, $password);
            
            $dbh->exec('set names utf8');
            $dbh->beginTransaction();
            $dbh->exec('delete from zip where is_firm = false');
            $count = 0;
            $stmt = $dbh->prepare(
                'insert into zip (zip_code, pref_code, city_code, add1, add2, firm' .
                ', add1_kana, add2_kana, firm_kana, note, is_firm)' .
                ' values (?, ?, ?, ?, ?, null, ?, ?, null, ?, false)');
            
            while (($rec = fgetcsv($csv)) !== FALSE) {
                $count += $stmt->execute(
                    array($rec[0], substr($rec[1], 0, 2), substr($rec[1], 2, 3),
                        $rec[2], $rec[3], $rec[5], $rec[6], $rec[8]));
            }
            
            $dbh->commit();
            $message = "住所毎の郵便番号を更新しました。${count}行";
        
        } catch (PDOException $e) {
            $message = "データベースの接続に失敗しました。 " . $e->getMessage();
        }
        
        fclose($csv);
    }
} else if (isset($_GET['firm_submit'])) {
    
    $csv = fopen($_GET['url'] , 'r');
    
    if ($csv === FALSE) {
        $message = "CSV ファイルを開くことができません。";
    } else {        
    
        try {
            $dbh = new PDO($dsn, $user, $password);
            
            $dbh->exec('set names utf8');
            $dbh->beginTransaction();
            $dbh->exec('delete from zip where is_firm = true');
            $count = 0;
            $stmt = $dbh->prepare(
                'insert into zip (zip_code, pref_code, city_code, add1, add2, firm' .
                ', add1_kana, add2_kana, firm_kana, note, is_firm)' .
                ' values (?, ?, ?, ?, ?, ?, null, null, ?, ?, true)');
            
            while (($rec = fgetcsv($csv)) !== FALSE) {
                $count += $stmt->execute(
                    array($rec[0], substr($rec[1], 0, 2), substr($rec[1], 2, 3),
                        $rec[2], $rec[3], $rec[4], $rec[7], $rec[8]));
            }
            
            $dbh->commit();
            $message = "事業所毎の郵便番号を更新しました。${count}行";
        
        } catch (PDOException $e) {
            $message = "データベースの接続に失敗しました。 " . $e->getMessage();
        }
        
        fclose($csv);
    }
}

?>
<html>
  <head>
    <title>CSVデータでDBテーブルを更新するサンプル</title>
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8" />
    <style type="text/css">
    body, h1, h2, p, form, input {
      font-weight: normal;
      font-family: sans-serif;
      font-size: 100%;
      margin: 0;
    }
    body {
      padding: 0 1em 0 1em;
    }
    h1 {
      font-size: 200%;
      line-height: 2em;
      color: #990;
    }
    h2 {
      font-size: 140%;
      line-height: 2em;
      color: #990;
    }
    p {
      line-height: 1.5em;
    }
    #bc {
      padding: .1em 1em .1em 1em;
      background: #990;
      color: #fff;
      margin: 0 0 1em 0;
    }
    #bc a {
      padding: 0 .25em 0 .25em;
      color: #fff;
      text-decoration: none;
    }
    #bc a:hover {
      background: #ff9;
      color: #990;
    }
    .url {
      font-family: monospace;
      width: 40em;
    }
    #status {
      color: #099;
      margin: .5em 2em .5em 2em;
    }
    #cr {
      color: #f60;
      margin: 2em 2em 2em 2em;
    }
    </style>
  </head>
  <body>
    <h1>CSVデータでDBテーブルを更新するサンプル</h1>
    <div id="bc"><a href="/">Top</a> ≫ <a href="./">サンプル</a></div>
    <p id="status"><?php echo $message; ?></p>
    <p><a href="dbddl.txt">テーブルの定義</a></p>
    <h2>都道府県</h2>
    <form id="pref" name="x0401" method="get" action="db.php">
      <input class="url" type="text" name="url" value="http://localhost:10088/sample/csv/x0401.csv"/>
      <input type="submit" name="x0401_submit" value="OK"/>
    </form>
    <h2>市区町村</h2>
    <form id="pref" name="x0402" method="get" action="db.php">
      <input class="url" type="text" name="url" value="http://localhost:10088/sample/csv/x0402.csv"/>
      <input type="submit" name="x0402_submit" value="OK"/>
    </form>
    <h2>住所毎の郵便番号</h2>
    <form id="pref" name="area" method="get" action="db.php">
      <input class="url" type="text" name="url" value="http://localhost:10088/sample/csv/area.csv"/>
      <input type="submit" name="area_submit" value="OK"/>
    </form>
    <h2>事業所毎の郵便番号</h2>
    <form id="pref" name="firm" method="get" action="db.php">
      <input class="url" type="text" name="url" value="http://localhost:10088/sample/csv/firm.csv"/>
      <input type="submit" name="firm_submit" value="OK"/>
    </form>
    <p id="cr">Copyright 2010 Michinobu Maeda.</p>
  </body>
</html>
