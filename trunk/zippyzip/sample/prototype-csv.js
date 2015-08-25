/*
 * 郵便番号 CSV細切れデータを prototype.js で利用するプログラムのサンプル
 * 
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
 * CSVデータのURLの定義
 */
var httpMethod       = "get";
var urlCsvMinor      = "/data/csv/zip/%major.csv";
var urlCsvZip        = "/data/csv/zip/%major/%minor.csv";
var urlCsvCity       = "/data/csv/pref/%pref.csv";
var urlCsvCityDetail = "/data/csv/pref/%pref/%city.csv";

/*
 * HTML要素のIDの定義
 */
var idMain = "main";
var idInstArea = "instarea";
var idZip1 = "zip1";
var idZip2 = "zip2";
var idPref = "pref";
var idCity = "city";
var idAdd1 = "add1";
var idAdd2 = "add2";
var idCorp = "corp";
var idPart = "part";
var idSKana = "skana";
var idGKana = "gkana";
var idSName = "sname";
var idGName = "gname";
var idOk    = "ok";
var idReset = "reset";
var idZipMsg = "zipmsg";
var idPrefMsg = "prefmsg";
var idCityMsg = "citymsg";
var idAdd1Msg = "add1msg";
var idAdd2Msg = "add2msg";
var idCorpMsg = "corpmsg";
var idPartMsg = "partmsg";
var idKanaMsg = "kanamsg";
var idNameMsg = "namemsg";
var idMinorArea = "minorArea";
var idMinor     = "minor";
var idStrtArea  = "strtArea";
var idStrt      = "street";
var idBldgArea  = "bldgArea";
var idBldg      = "bldg";
var idCityListArea  = "cityListArea";
var idCityList  = "cityList";

/*
 * メッセージの定義
 */
var msgInitialInstruction    = "郵便番号を入力するか、都道府県を選択してください。";
var msgRecommendNumberFormat = "半角数字で入力してください。";
var msgProspectiveMinorList  = "郵便番号 %major- の下４桁の候補 %count件"
var msgNoMajarNo             = "郵便番号上３桁が未入力です。"
var msgNoProspectiveAddr     = "候補が見つかりません。郵便番号に間違いがないかご確認ください。";
var msgHitZipStrt            = "郵便番号 %major-%minor に対応する住所が見つかりました。<br />";
var msgHitZipBldg            = "郵便番号 %major-%minor に対応する事業所が見つかりました。<br />";
var msgProspectiveZipStrt    = "郵便番号 %major-%minor の住所の候補 %count件";
var msgProspectiveZipBldg    = "郵便番号 %major-%minor の事業所の候補 %count件";
var msgProspectiveCity       = "%pref の市区町村の候補  %count件";
var msgProspectiveCityStrt   = "%pref %city の住所の候補 %count件";
var msgProspectiveCityBldg   = "%pref %city の事業所候補 %count件";
var msgNoProspectiveDetail   = "候補が見つかりません。";
var msgRecommendNoKanji      = "ふりがなに漢字は使えません。";
var msgRecommended           = "必須";
var msgWaitMajor             = "郵便番号 %major- の下４桁の候補を検索しています。";
var msgWaitMinor             = "郵便番号 %major-%minor の候補を検索しています。";
var msgWaitPref              = "%pref の候補を検索しています。";
var msgWaitCity              = "%pref %city の候補を検索しています。";

/*
 * スタイルの定義
 */
var styleInstractionArea = "height: 4em; padding: 4px 8px 4px 8px;";
var styleFocusOn         = "background-color: #FFFACD;";
var styleFocusOff        = "background-color: #FFFFFF;";

/*
 * その他
 */

// option の表示最大文字数
var maxOptionLength = 48;
// option の表示最大文字数を超えた場合の省略記号
var sufixOptionOverMax = "..";
//ひらがな: 0x3041-0x309B
//全角カナ: 0x30A1-0x30FE
//半角カナ: 0xFF65-0xFF9F
var regexpNgFrigana = new RegExp(/[^\u3041-\u309B\u30A1-\u30FE\uFF65-\uFF9F]/);

var valZip1 = "";
var valZip2 = "";
var valPref = "-";
var valCityCode = "";
var valCityName = "";

/**
 * HTMLページロード時の処理
 */
document.observe("dom:loaded", function() {

    /*
     * 表示の設定
     */
    setOnFocusBackgroundColor(idZip1);
    setOnFocusBackgroundColor(idZip2);
    setOnFocusBackgroundColor(idPref);
    setOnFocusBackgroundColor(idCity);
    setOnFocusBackgroundColor(idAdd1);
    setOnFocusBackgroundColor(idAdd2);
    setOnFocusBackgroundColor(idCorp);
    setOnFocusBackgroundColor(idPart);
    setOnFocusBackgroundColor(idSKana);
    setOnFocusBackgroundColor(idGKana);
    setOnFocusBackgroundColor(idSName);
    setOnFocusBackgroundColor(idGName);
    
    initContents();

    /*
     * イベントハンドラの設定
     */
    $(idZip1).observe("keydown", onZip1BeforeChange);
    $(idZip1).observe("keyup",   onZip1AgfterChange);
    $(idZip2).observe("keydown", onZip2BeforeChange);
    $(idZip2).observe("keyup",   onZip2AgfterChange);
    $(idPref).observe("change",  onPrefAgfterChange);
    $(idZip1).observe("blur",    onZip1FocusOff);
    $(idZip2).observe("blur",    onZip2FocusOff);
    $(idPref).observe("blur",    onPrefFocusOff);
    $(idCity).observe("blur",    onCityFocusOff);
    $(idAdd1).observe("blur",    onAdd1FocusOff);
    $(idAdd2).observe("blur",    onAdd2FocusOff);
    $(idCorp).observe("blur",    onCorpFocusOff);
    $(idPart).observe("blur",    onPartFocusOff);
    $(idSKana).observe("blur",   onSKanaFocusOff);
    $(idGKana).observe("blur",   onGKanaFocusOff);
    $(idSName).observe("blur",   onSNameFocusOff);
    $(idGName).observe("blur",   onGNameFocusOff);
    $(idReset).observe("click",  onResetClick);
    $(idOk).observe("click",     onOkClick);
});

/**
 * zip1 変更前の処理
 */
function onZip1BeforeChange(e) {
    valZip1 = $(idZip1).value;
}

/**
 * zip1 変更後の処理
 */
function onZip1AgfterChange(e) {

    if (!checkNumberOnly(idZip1, idZipMsg)) {
        $(idZip1).value = valZip1;
        return false;
    }
    if ((valZip1 != $(idZip1).value) && ($(idZip1).value.length == 3)) {

        valZip1 = $(idZip1).value;

        if (valZip2.length == 4) {
            onCompleteInputZipAll();
        } else {
            onCompleteInputZip1();
        }
    } else {
        valZip1 = $(idZip1).value;
    }
}

/**
 * zip2 変更前の処理
 */
function onZip2BeforeChange(e) {
    valZip2 = $(idZip2).value;
}

/**
 * zip2 変更後の処理
 */
function onZip2AgfterChange(e) {

    if (!checkNumberOnly(idZip2, idZipMsg)) {
        $(idZip2).value = valZip2;
        return false;
    }
    if ((valZip2 != $(idZip2).value) && ($(idZip2).value.length == 4)) {
        valZip2 = $(idZip2).value;
        onCompletIenputZip2();
    } else {
        valZip2 = $(idZip2).value;
    }
}

/**
 * 上３桁入力完了時の処理。
 */
function onCompleteInputZip1() {

    clearInstractionArea();
    $(idInstArea).update(msgWaitMajor.replace(/%major/, valZip1));
    $(idZip2).focus();
    new Ajax.Request(
        urlCsvMinor
            .replace(/%major/, valZip1),
        { method: httpMethod, onSuccess: onCompleteGetMinorCsv});
}

/**
 * 下４桁データ受信完了時の処理。
 */
function onCompleteGetMinorCsv(csvDocument) {

    clearInstractionArea();
    
    major = valZip1;
    minorList = csvDocument.responseText.split("\n");
    
    cnt = 0;
    for (i = 0; i < minorList.length; ++i) {
        if (minorList[i].length > 0) { ++cnt; }
    }

    $(idInstArea).appendChild(new Element("div", { id: idMinorArea}));
    $(idMinorArea).appendChild(
        new Element("select", { id: idMinor, name: idMinor }));
    $(idMinor).appendChild(
        new Element("option", { value: "-", selected: "true" })
            .update(msgProspectiveMinorList
                .replace(/%major/, major)
                .replace(/%count/, cnt)));

    for (i = 0; i < minorList.length; ++i) {

        if (minorList[i].length == 0) { continue; }
        $(idMinor).appendChild(
            new Element("option", { value: minorList[i] }).update(minorList[i]));
    }
    
    $(idMinor).observe("change", onMinorAgfterChange);
}

/**
 * 下４桁候補選択時の処理。
 */
function onMinorAgfterChange (e) {
    $(idZip2).value = $(idMinor).value;
    valZip2         = $(idMinor).value;
    $(idZip2).focus();
    onCompletIenputZip2();
}

/**
 * zip2 入力完了時の処理
 */
function onCompletIenputZip2() {

    if (valZip1.length == 3) {
        onCompleteInputZipAll();
    } else {
        clearInstractionArea();
        $(idInstArea).update(msgNoMajarNo);
        $(idZip1).focus();
    }
}

/**
 * 郵便番号入力完了時の処理。
 */
function onCompleteInputZipAll() {

    clearInstractionArea();
    $(idInstArea).update(msgWaitMinor
        .replace(/%major/, valZip1)
        .replace(/%minor/, valZip2));
    $(idPref).focus();
    new Ajax.Request(urlCsvZip
            .replace(/%major/, valZip1)
            .replace(/%minor/, valZip2),
        { method: httpMethod, onSuccess: onCompleteGetZipCsv});
}

/**
 * 郵便番号データ受信完了時の処理。
 */
function onCompleteGetZipCsv(csvDocument) {

    clearInstractionArea();

    cntStrt = 0;
    lastStrt = -1;
    cntBldg = 0;
    lastBldg = -1;
    recs = csvDocument.responseText.split("\n");
    major = valZip1;
    minor = valZip2;

    for (i = 0; i < recs.length; ++i) {
        if (recs[i].length == 0) { continue; }
        rec = recs[i].split(",");
        for (j = 0; j < rec.length; ++j) {
            if (rec[j].charAt(0) == "\"") {
        	    for (k = j + 1; k < rec.length; ++k) {
        	        if (rec[j].charAt(rec[j].length - 1) == "\"") { break; }
        	        rec[j] = rec[j] + "," + rec[k];
        	        rec.splice(k, 1);
        	        k = j;
        	    }
            }
        }
        if (rec[5] == "") {
            ++ cntStrt;
            lastStrt = i;
        } else {
            ++ cntBldg;
            lastBldg = i;
        }
    }
    
    if ((cntBldg + cntStrt) == 0) {
        $(idInstArea).update(msgNoProspectiveAddr);
        return;
    }
        
    if (cntStrt == 1) {
        rec = recs[lastStrt].split(",");
        for (j = 0; j < rec.length; ++j) {
            if (rec[j].charAt(0) == "\"") {
                for (k = j + 1; k < rec.length; ++k) {
                    if (rec[j].charAt(rec[j].length - 1) == "\"") { break; }
                    rec[j] = rec[j] + "," + rec[k];
                    rec.splice(k, 1);
                    k = j;
                }
            }
        }
        $(idInstArea).update(msgHitZipStrt
                .replace(/%major/, major)
                .replace(/%minor/, minor)
            + rec[1]
            + rec[2]
            + rec[3]
            + rec[4]
            + ((rec[6] == "")
                ? ""
                : " &nbsp; ※ " + rec[6]));
        $(idPref).value = rec[0].substr(0, 2);
        valPref         = $(idPref).value;
        $(idCity).value = rec[2].replace(/\"/g, "");
        $(idAdd1).value = rec[3].replace(/\"/g, "");
        $(idAdd2).value = rec[4].replace(/\"/g, "");
        if ($(idAdd2).value == "" ) {
            $(idAdd1).focus();
        } else {
            $(idAdd2).focus();
        }
        validate(false);
        return;
    }
    
    if (cntBldg == 1) {
        rec = recs[lastBldg].split(",");
        for (j = 0; j < rec.length; ++j) {
            if (rec[j].charAt(0) == "\"") {
                for (k = j + 1; k < rec.length; ++k) {
                    if (rec[j].charAt(rec[j].length - 1) == "\"") { break; }
                    rec[j] = rec[j] + "," + rec[k];
                    rec.splice(k, 1);
                    k = j;
                }
            }
        }
        $(idInstArea).update(msgHitZipBldg
                .replace(/%major/, major)
                .replace(/%minor/, minor)
            + rec[5]
            + ((rec[6] == "")
                ? ""
                : " &nbsp; ※ " + rec[6]));
        $(idPref).value = rec[0].substr(0, 2);
        valPref         = $(idPref).value;
        $(idCity).value = rec[2].replace(/\"/g, "");
        $(idAdd1).value = rec[3].replace(/\"/g, "");
        $(idAdd2).value = rec[4].replace(/\"/g, "");
        $(idCorp).value = rec[5].replace(/\"/g, "");
        $(idPart).focus();
        validate(false);
        return;
    }
    
    if (cntStrt > 1) {
        $(idInstArea).appendChild(new Element("div", { id: idStrtArea}));
        $(idStrtArea).appendChild(
            new Element("select", { id: idStrt, name: idStrt }));
        $(idStrt).appendChild(
            new Element("option", { value: "-", selected: "true" })
                .update(msgProspectiveZipStrt
                .replace(/%major/, major)
                .replace(/%minor/, minor)
                .replace(/%count/, cntStrt)));
        $(idStrt).observe("change", onStreetAfterChange);
    }    
    if (cntBldg > 1) {
        $(idInstArea).appendChild(new Element("div", { id: idBldgArea}));
        $(idBldgArea).appendChild(
            new Element("select", { id: idBldg, name: idBldg }));
        $(idBldg).appendChild(
            new Element("option", { value: "-", selected: "true" })
                .update(msgProspectiveZipBldg
                    .replace(/%major/, major)
                    .replace(/%minor/, minor)
                    .replace(/%count/, cntBldg)));
        $(idBldg).observe("change", onBldgAfterChange);
    }    

    for (i = 0; i < recs.length; ++i) {

        if (recs[i].length == 0) { continue; }
        rec = recs[i].split(",");
        for (j = 0; j < rec.length; ++j) {
            if (rec[j].charAt(0) == "\"") {
                for (k = j + 1; k < rec.length; ++k) {
                    if (rec[j].charAt(rec[j].length - 1) == "\"") { break; }
                    rec[j] = rec[j] + "," + rec[k];
                    rec.splice(k, 1);
                    k = j;
                }
            }
        }

        val = major + ","
            + minor + ","
            + rec[0].substr(0, 2) + "," 
            + rec[2] + "," 
            + rec[3] + ","
            + rec[4] + "," 
            + rec[5];

        if (rec[5] == "") {
            cap = rec[1]
                + rec[2]
                + rec[3]
                + rec[4]
                + ((rec[6] == "")
                    ? ""
                    : " ※" + rec[6]);
            trg = idStrt;
        } else {
            cap = rec[5];
            trg = idBldg;
        }

        if (cap.length > maxOptionLength) {
            cap = cap.substr(0, maxOptionLength) + sufixOptionOverMax;
        }
        $(trg).appendChild(new Element("option", { value: val }).update(cap));
    }
}

/**
 * 住所候補変更時の処理
 */
function onStreetAfterChange(e) {

    params = $(idStrt).value.split(",");
    for (j = 0; j < params.length; ++j) {
        if (params[j].charAt(0) == "\"") {
            for (k = j + 1; k < params.length; ++k) {
                if (params[j].charAt(params[j].length - 1) == "\"") { break; }
                params[j] = params[j] + "," + params[k];
                params.splice(k, 1);
                k = j;
            }
        }
    }

    $(idZip1).value = params[0];
    valZip1         = params[0];
    $(idZip2).value = params[1];
    valZip2         = params[1];
    $(idPref).value = params[2];
    valPref         = params[2];
    $(idCity).value = params[3].replace(/\"/g, "");
    $(idAdd1).value = params[4].replace(/\"/g, "");
    $(idAdd2).value = params[5].replace(/\"/g, "");
    if ($(idAdd2).value == "" ) {
        $(idAdd1).focus();
    } else {
        $(idAdd2).focus();
    }
    validate(false);
}

/**
 * 事業所候補変更時の処理
 */
function onBldgAfterChange(e) {

    params = $(idBldg).value.split(",");
    for (j = 0; j < params.length; ++j) {
        if (params[j].charAt(0) == "\"") {
            for (k = j + 1; k < params.length; ++k) {
                if (params[j].charAt(params[j].length - 1) == "\"") { break; }
                params[j] = params[j] + "," + params[k];
                params.splice(k, 1);
                k = j;
            }
        }
    }

    $(idZip1).value = params[0];
    valZip1         = params[0];
    $(idZip2).value = params[1];
    valZip2         = params[1];
    $(idPref).value = params[2];
    valPref         = params[2];
    $(idCity).value = params[3].replace(/\"/g, "");
    $(idAdd1).value = params[4].replace(/\"/g, "");
    $(idAdd2).value = params[5].replace(/\"/g, "");
    $(idCorp).value = params[6].replace(/\"/g, "");
    $(idCorp).focus();
    validate(false);
}

/**
 * 都道府県変更時の処理
 */
function onPrefAgfterChange(e) {

    valPref = $(idPref).value;
    clearInstractionArea();
    $(idInstArea).update(msgWaitPref
        .replace(/%pref/, $("pref" + valPref).text));
    $(idCity).focus();
    new Ajax.Request(urlCsvCity
            .replace(/%pref/, valPref),
        { method: httpMethod, onSuccess: onCompleteGetCityCsv});
}

/**
 * 市区町村データ受信完了時の処理。
 */
function onCompleteGetCityCsv(csvDocument) {

    $pref = valPref;
    $prefName = $("pref" + $pref).text;

    clearInstractionArea();

    cityList = csvDocument.responseText.split("\n");

    $cnt = 0;
    for (i = 0; i < cityList.length; ++i) {
        if (cityList[i].length == 0) { continue; }
        ++$cnt;
    }

    $(idInstArea).appendChild(new Element("div", { id: idCityListArea}));
    $(idCityListArea).appendChild(
        new Element("select", { id: idCityList, name: idCityList }));
    $(idCityList).appendChild(
        new Element("option", { value: "-", selected: "true" })
            .update(msgProspectiveCity
                .replace(/%pref/, $prefName)
                .replace(/%count/, $cnt)));
    valCityCode = "";
    valCityName = "";

    for (i = 0; i < cityList.length; ++i) {
        if (cityList[i].length == 0) { continue; }
        rec = cityList[i].split(",");
        for (j = 0; j < rec.length; ++j) {
            if (rec[j].charAt(0) == "\"") {
                for (k = j + 1; k < rec.length; ++k) {
                    if (rec[j].charAt(rec[j].length - 1) == "\"") { break; }
                    rec[j] = rec[j] + "," + rec[k];
                    rec.splice(k, 1);
                    k = j;
                }
            }
        }
        $(idCityList).appendChild(
            new Element(
                "option",
                {
                    value: rec[0],
                    id: "city" + rec[0]
                }
            ).update(rec[1])
        );
    }

    $(idCityList).observe("change", onChangeSelectCityList);
}

/**
 * 市区町村変更時の処理
 */
function onChangeSelectCityList(e) {

    valCityCode = $(idCityList).value;
    valCityName = $("city" + valCityCode).text;
    clearInstractionArea();
    $(idInstArea).update(msgWaitCity
        .replace(/%pref/, $("pref" + valPref).text)
        .replace(/%city/, valCityName));
    $(idCity).value = valCityName;
    validate(false);
    new Ajax.Request(urlCsvCityDetail
            .replace(/%pref/, valPref)
            .replace(/%city/, valCityCode.substr(2, 3)),
        { method: httpMethod, onSuccess: onCompleteGetCityDetailCsv});
}

/**
 * 市区町村詳細データ受信完了時の処理。
 */
function onCompleteGetCityDetailCsv(csvDocument) {

    prefName = $("pref" + valPref).text;

    clearInstractionArea();

    cntStrt = 0;
    lastStrt = -1;
    cntBldg = 0;
    lastBldg = -1;
    recs = csvDocument.responseText.split("\n");

    for (i = 0; i < recs.length; ++i) {
        if (recs[i].length == 0) { continue; }
        rec = recs[i].split(",");
        for (j = 0; j < rec.length; ++j) {
            if (rec[j].charAt(0) == "\"") {
                for (k = j + 1; k < rec.length; ++k) {
                    if (rec[j].charAt(rec[j].length - 1) == "\"") { break; }
                    rec[j] = rec[j] + "," + rec[k];
                    rec.splice(k, 1);
                    k = j;
                }
            }
        }
        if (rec[3] == "") {
            ++ cntStrt;
            lastStrt = i;
        } else {
            ++ cntBldg;
            lastBldg = i;
        }
    }
    
    if ((cntBldg + cntStrt) == 0) {
        $(idInstArea).update(msgNoProspectiveDetail);
        return;
    }
        
    if (cntStrt > 0) {
        $(idInstArea).appendChild(new Element("div", { id: idStrtArea}));
        $(idStrtArea).appendChild(
            new Element("select", { id: idStrt, name: idStrt }));
        $(idStrt).appendChild(
            new Element("option", { value: "-", selected: "true" })
                .update(msgProspectiveCityStrt
                    .replace(/%pref/, prefName)
                    .replace(/%city/, valCityName)
                    .replace(/%count/, cntStrt)));
        $(idStrt).observe("change", onStreetAfterChange);
    }    
    
    if (cntBldg > 0) {
        $(idInstArea).appendChild(new Element("div", { id: idBldgArea}));
        $(idBldgArea).appendChild(
            new Element("select", { id: idBldg, name: idBldg }));
        $(idBldg).appendChild(
            new Element("option", { value: "-", selected: "true" })
                .update(msgProspectiveCityBldg
                    .replace(/%pref/, prefName)
                    .replace(/%city/, valCityName)
                    .replace(/%count/, cntBldg)));
        $(idBldg).observe("change", onBldgAfterChange);
    }    

    for (i = 0; i < recs.length; ++i) {

        if (recs[i].length == 0) { continue; }
        rec = recs[i].split(",");
        for (j = 0; j < rec.length; ++j) {
            if (rec[j].charAt(0) == "\"") {
                for (k = j + 1; k < rec.length; ++k) {
                    if (rec[j].charAt(rec[j].length - 1) == "\"") { break; }
                    rec[j] = rec[j] + "," + rec[k];
                    rec.splice(k, 1);
                    k = j;
                }
            }
        }
        val = rec[0].substr(0, 3) + ","
            + rec[0].substr(3, 4) + ","
            + valPref + ","
            + valCityName + "," 
            + rec[1] + "," 
            + rec[2] + ","
            + rec[3];

        if (rec[3] == "") {
            cap = rec[0].substr(0, 3) + "-"
                + rec[0].substr(3, 4) + " "
                + rec[1]
                + rec[2] + " "
                + ((rec[4] == "")
                    ? ""
                    : " ※" + rec[4]);
            trg = idStrt;
        } else {
            cap = rec[0].substr(0, 3) + "-"
                + rec[0].substr(3, 4) + " "
                + rec[3];
            trg = idBldg;
        }
        
        if (cap.length > maxOptionLength) {
            cap = cap.substr(0, maxOptionLength) + sufixOptionOverMax;
        }
        $(trg).appendChild(new Element("option", { value: val }).update(cap));
    }
}

/**
 * 郵便番号：上３桁フォーカス Off 時の処理
 */
function onZip1FocusOff(e) {
    validate(false);
}

/**
 * 郵便番号：下４桁フォーカス Off 時の処理
 */
function onZip2FocusOff(e) {
    validate(false);
}

/**
 * 都道府県フォーカス Off 時の処理
 */
function onPrefFocusOff(e) {
    validate(false);
}

/**
 * 市区町村フォーカス Off 時の処理
 */
function onCityFocusOff(e) {
    validate(false);
}

/**
 * 住所１フォーカス Off 時の処理
 */
function onAdd1FocusOff(e) {
    validate(false);
}

/**
 * 住所2フォーカス Off 時の処理
 */
function onAdd2FocusOff(e) {
    validate(false);
}

/**
 * 会社名フォーカス Off 時の処理
 */
function onCorpFocusOff(e) {
    validate(false);
}

/**
 * 部署名フォーカス Off 時の処理
 */
function onPartFocusOff(e) {
    validate(false);
}

/**
 * ふりがな：姓 フォーカス Off 時の処理
 */
function onSKanaFocusOff(e) {
    validate(false);
}

/**
 * ふりがな：名 フォーカス Off 時の処理
 */
function onGKanaFocusOff(e) {
    validate(false);
}

/**
 * 氏名：姓 フォーカス Off 時の処理
 */
function onSNameFocusOff(e) {
    validate(false);
}

/**
 * 氏名：名 フォーカス Off 時の処理
 */
function onGNameFocusOff(e) {
    validate(false);
}

/**
 * OKボタンクリック時の処理
 */
function onOkClick() {
    validate(true);
    return false;
}

/**
 * リセットボタンクリック時の処理
 */
function onResetClick() {
    initContents();
    return false;
}

/**
 * 半角数字のみ可のチェック
 */
function checkNumberOnly(inputid, messageid) {

    txt = $(inputid).value;
    if (/[^０-９]/.test(txt)) {
        txt.replace(/０/, "0");
        txt.replace(/１/, "1");
        txt.replace(/２/, "2");
        txt.replace(/３/, "3");
        txt.replace(/４/, "4");
        txt.replace(/５/, "5");
        txt.replace(/６/, "6");
        txt.replace(/７/, "7");
        txt.replace(/８/, "8");
        txt.replace(/９/, "9");
        $(inputid).value = txt;
    }

    if (/[^0-9]/.test($(inputid).value)) {
        $(messageid).update(msgRecommendNumberFormat);
        return false;
    }

    return true;
}

/**
 * 入力チェック
 */
function validate(isSetFocus) {
    
    ret = true;
    nextForcus = false;

    if ($(idSName).value == "") {
        ret = false;
        nextForcus = idSName;
        $(idNameMsg).update(msgRecommended);
    } else if ($(idGName).value == "") {
        ret = false;
        nextForcus = idGName;
        $(idNameMsg).update(msgRecommended);
    } else {
        $(idNameMsg).update("");
    }

    if ($(idSKana).value == "") {
        ret = false;
        nextForcus = idSKana;
        $(idKanaMsg).update(msgRecommended);
    } else if ($(idGKana).value == "") {
        ret = false;
        nextForcus = idGKana;
        $(idKanaMsg).update(msgRecommended);
    } else if ($(idSKana).value.match(regexpNgFrigana)) {
        ret = false;
        nextForcus = idSKana;
        $(idKanaMsg).update(msgRecommendNoKanji);
    } else if ($(idGKana).value.match(regexpNgFrigana)) {
        ret = false;
        nextForcus = idGKana;
        $(idKanaMsg).update(msgRecommendNoKanji);
    } else {
        $(idKanaMsg).update("");
    }

    if (($(idAdd1).value == "") &&
        ($(idCorp).value == "")) {
        ret = false;
        nextForcus = idAdd1;
        $(idAdd1Msg).update(msgRecommended);
    } else {
        $(idAdd1Msg).update("");
    }

    if ($(idCity).value == "") {
        ret = false;
        nextForcus = idCity;
        $(idCityMsg).update(msgRecommended);
    } else {
        $(idCityMsg).update("");
    }

    if ($(idPref).value == "-") {
        ret = false;
        nextForcus = idPref;
        $(idPrefMsg).update(msgRecommended);
    } else {
        $(idPrefMsg).update("");
    }

    if (($(idZip1).value.length < 3)) {
        ret = false;
        nextForcus = idZip1;
        $(idZipMsg).update(msgRecommended);
    } else if ($(idZip2).value.length < 4) {
        ret = false;
        nextForcus = idZip2;
        $(idZipMsg).update(msgRecommended);
    } else if ($(idZip1).value.match(/[^0-9]/)) {
        ret = false;
        nextForcus = idZip1;
        $(idZipMsg).update(msgRecommendNumberFormat);
    } else if ($(idZip2).value.match(/[^0-9]/)) {
        ret = false;
        nextForcus = idZip2;
        $(idZipMsg).update(msgRecommendNumberFormat);
    } else {
        $(idZipMsg).update("");
    }
    
    if (isSetFocus && nextForcus) {
        setTimeout(function () { $(nextForcus).focus(); }, 100);
    }

    return ret;
}

/**
 * 内容をすべて初期化する。
 */
function initContents() {

    $(idZip1).value = "";
    valZip1         = "";
    $(idZip2).value = "";
    valZip2         = "";
    $(idPref).value = "-";
    valPref         = "-";
    $(idCity).value = "";
    valCityCode = "";
    valCityName = "";
    $(idAdd1).value = "";
    $(idAdd2).value = "";
    $(idCorp).value = "";
    $(idPart).value = "";
    $(idSKana).value = "";
    $(idGKana).value = "";
    $(idSName).value = "";
    $(idGName).value = "";

    $(idZipMsg).update("");
    $(idPrefMsg).update("");
    $(idCityMsg).update("");
    $(idAdd1Msg).update("");
    $(idAdd2Msg).update("");
    $(idCorpMsg).update("");
    $(idPartMsg).update("");
    $(idKanaMsg).update("");
    $(idNameMsg).update("");
    
    validate(true);

    clearInstractionArea();
    $(idInstArea).insert(new Element("p").update(msgInitialInstruction));
}

/**
 * インストラクション領域の初期化
 */
function clearInstractionArea() {
    $(idInstArea).update("");
    contents = $(idInstArea).childNodes;
    for (i = 0; i < contents.length; ++i) {
        $(idInstArea).removeChild(contents[i]);
    }
    $(idInstArea).writeAttribute("style", styleInstractionArea);
}

/**
 * テキストボックスのフォーカス時の色を設定する。
 */
function setOnFocusBackgroundColor(targetId) {
    $(targetId).observe("focus",   function () {
        $(targetId).writeAttribute("style", styleFocusOn);
    });
    $(targetId).observe("blur",    function () {
        $(targetId).writeAttribute("style", styleFocusOff);
    });
}
