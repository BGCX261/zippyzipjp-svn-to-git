/*
 * 郵便番号JSON細切れデータを prototype.js で利用するプログラムのサンプル
 * 
 * Copyright 2009 Michinobu Maeda.
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

// JSONデータのURL: 郵便番号別
var urlJsonZip = "/data/json/zip/";
//JSONデータのURL: 都道府県・市区町村別
var urlJsonPref = "/data/json/pref/";
// option の表示最大文字数
var maxOptLen = 48;
// option の表示最大文字数を超えた場合の省略記号
var sufixOpt = "..";
// ひらがな: 0x3041-0x309B
// 全角カナ: 0x30A1-0x30FE
// 半角カナ: 0xFF65-0xFF9F
var regexpNgFrigana = new RegExp(/[^\u3041-\u309B\u30A1-\u30FE\uFF65-\uFF9F]/);

/**
 * HTMLページロード時の処理
 */
document.observe("dom:loaded", function() {

    initContents();

    /*
     * イベントハンドラの設定
     */
    setOnFocusBackgroundColor("zip1");
    setOnFocusBackgroundColor("zip2");
    setOnFocusBackgroundColor("pref");
    setOnFocusBackgroundColor("city");
    setOnFocusBackgroundColor("add1");
    setOnFocusBackgroundColor("add2");
    setOnFocusBackgroundColor("corp");
    setOnFocusBackgroundColor("part");
    setOnFocusBackgroundColor("skana");
    setOnFocusBackgroundColor("gkana");
    setOnFocusBackgroundColor("sname");
    setOnFocusBackgroundColor("gname");
    $("zip1").observe("keydown", onZip1BeforeChange);
    $("zip1").observe("keyup",   onZip1AgfterChange);
    $("zip2").observe("keydown", onZip2BeforeChange);
    $("zip2").observe("keyup",   onZip2AgfterChange);
    $("pref").observe("change",  onPrefAgfterChange);
    $("zip1").observe("blur",    onZip1FocusOff);
    $("zip2").observe("blur",    onZip2FocusOff);
    $("pref").observe("blur",    onPrefFocusOff);
    $("city").observe("blur",    onCityFocusOff);
    $("add1").observe("blur",    onAdd1FocusOff);
    $("add2").observe("blur",    onAdd2FocusOff);
    $("corp").observe("blur",    onCorpFocusOff);
    $("part").observe("blur",    onPartFocusOff);
    $("skana").observe("blur",   onSKanaFocusOff);
    $("gkana").observe("blur",   onGKanaFocusOff);
    $("sname").observe("blur",   onSNameFocusOff);
    $("gname").observe("blur",   onGNameFocusOff);
    $("reset").observe("click",  onResetClick);
});

var valZip1 = "";

/**
 * zip1 変更前の処理
 */
function onZip1BeforeChange(e) {
    valZip1 = $("zip1").value;
}

/**
 * zip1 変更後の処理
 */
function onZip1AgfterChange(e) {

    if (!checkNumberOnly("zip1", "zipmsg")) {
        $("zip1").value = valZip1;
        return false;
    }
    if ((valZip1 != $("zip1").value) && ($("zip1").value.length == 3)) {

        valZip1 = $("zip1").value;

        if ($("zip2").value.length == 4) {
            onCompleteInputZipAll();
        } else {
            onCompleteInputZip1();
        }
    } else {
        valZip1 = $("zip1").value;
    }
}

var valZip2 = "";

/**
 * zip2 変更前の処理
 */
function onZip2BeforeChange(e) {
    valZip2 = $("zip2").value;
}

/**
 * zip2 変更後の処理
 */
function onZip2AgfterChange(e) {

    if (!checkNumberOnly("zip2", "zipmsg")) {
        $("zip2").value = valZip2;
        return false;
    }
    if ((valZip2 != $("zip2").value) && ($("zip2").value.length == 4)) {
        valZip2 = $("zip2").value;
        onCompletIenputZip2();
    } else {
        valZip2 = $("zip2").value;
    }
}

/**
 * 上３桁入力完了時の処理。
 */
function onCompleteInputZip1() {

    clearInstractionArea();
    $("instarea").update("郵便番号 " + $("zip1").value + "- の下４桁の候補を検索しています。");
    $("zip2").focus();
    new Ajax.Request(urlJsonZip + $("zip1").value + ".json",
            { method: "get", onSuccess: onCompleteGetZip2Json});
}

/**
 * 下４桁データ受信完了時の処理。
 */
function onCompleteGetZip2Json(jsonDocument) {

    clearInstractionArea();
    data = jsonDocument.responseText.evalJSON();
    $("instarea").appendChild(new Element("div", { id: "zip2Area"}));
    $("zip2Area").appendChild(
        new Element("select", { id: "zip2List", name: "zip2List" }));
    $("zip2List").appendChild(
        new Element("option", { value: "-", selected: "true" }).update(
                "郵便番号 " + data.zip1 + "- の下４桁の候補 " + data.zip2list.length + "件"));

    for (i = 0; i < data.zip2list.length; ++i) {
        $("zip2List").appendChild(
            new Element("option", { value: data.zip2list[i] }).update(data.zip2list[i]));
    }
    
    $("zip2List").observe("change", onZip2ListAgfterChange);
}

/**
 * 下４桁候補選択時の処理。
 */
function onZip2ListAgfterChange (e) {
    $("zip2").value = $("zip2List").value;
    $("zip2").focus();
    onCompletIenputZip2();
}

/**
 * zip2 入力完了時の処理
 */
function onCompletIenputZip2() {

    if ($("zip1").value.length == 3) {
        onCompleteInputZipAll();
    } else {
        clearInstractionArea();
        $("instarea").update("郵便番号上３桁が未入力です。");
        $("zip1").focus();
    }
}

/**
 * 郵便番号入力完了時の処理。
 */
function onCompleteInputZipAll() {

    clearInstractionArea();
    $("instarea").update("郵便番号 " + $("zip1").value + "-" + $("zip2").value
            + " の候補を検索しています。");
    $("pref").focus();
    new Ajax.Request(urlJsonZip + $("zip1").value + "/" + $("zip2").value + ".json",
            { method: "get", onSuccess: onCompleteGetZipJson});
}

/**
 * 郵便番号データ受信完了時の処理。
 */
function onCompleteGetZipJson(jsonDocument) {

    clearInstractionArea();

    cntStrt = 0;
    lastStrt = -1;
    cntBldg = 0;
    lastBldg = -1;
    data = jsonDocument.responseText.evalJSON();

    for (i = 0; i < data.addresslist.length; ++i) {
        if (data.addresslist[i].bldg == "") {
            ++ cntStrt;
            lastStrt = i;
        } else {
            ++ cntBldg;
            lastBldg = i;
        }
    }
    
    if ((cntBldg + cntStrt) == 0) {
        $("instarea").update("候補が見つかりません。郵便番号に間違いがないかご確認ください。");
        return;
    }
        
    if (cntStrt == 1) {
        rec = data.addresslist[lastStrt];
        $("instarea").update("郵便番号 " + data.zip1 + "-" + data.zip2
                + " に対応する住所が見つかりました。<br />"
                + rec.pref + rec.city + rec.add1 + rec.add2
                + ((rec.note == "") ? "" : " &nbsp; ※ " + rec.note));
        $("pref").value = rec.x0402.substr(0, 2);
        $("city").value = rec.city;
        $("add1").value = rec.add1;
        $("add2").value = rec.add2;
        if ($("add2").value == "" ) {
            $("add1").focus();
        } else {
            $("add2").focus();
        }
        validate(false);
        return;
    }
    
    if (cntBldg == 1) {
        rec = data.addresslist[lastBldg];
        $("instarea").update("郵便番号 " + data.zip1 + "-" + data.zip2
                + " に対応する事業所が見つかりました。<br />"
                + rec.bldg + ((rec.note == "") ? "" : " &nbsp; ※ " + rec.note));
        $("pref").value = rec.x0402.substr(0, 2);
        $("city").value = rec.city;
        $("add1").value = rec.add1;
        $("add2").value = rec.add2;
        $("corp").value = rec.bldg;
        $("part").focus();
        validate(false);
        return;
    }
    
    if (cntStrt > 1) {
        $("instarea").appendChild(new Element("div", { id: "strtArea"}));
        $("strtArea").appendChild(
            new Element("select", { id: "street", name: "street" }));
        $("street").appendChild(
                new Element("option", { value: "-", selected: "true" }).update(
                        "郵便番号 " + data.zip1 + "-" + data.zip2
                        + " の住所の候補 " + cntStrt + "件"));
        $("street").observe("change", onStreetAfterChange);
    }
    if (cntBldg > 1) {
        $("instarea").appendChild(new Element("div", { id: "bldgArea"}));
        $("bldgArea").appendChild(
            new Element("select", { id: "bldg", name: "bldg" }));
        $("bldg").appendChild(
                new Element("option", { value: "-", selected: "true" }).update(
                        "郵便番号 " + data.zip1 + "-" + data.zip2
                        + " の事業所の候補 " + cntBldg + "件"));
        $("bldg").observe("change", onBldgAfterChange);
    }    

    for (i = 0; i < data.addresslist.length; ++i) {

        rec = data.addresslist[i];
        val = $H({zip1: data.zip1, zip2: data.zip2, pref: rec.x0402.substr(0, 2),
            city: rec.city, add1: rec.add1, add2: rec.add2, bldg: rec.bldg}).toJSON();

        if (rec.bldg == "") {
            cap = rec.pref + rec.city + rec.add1 + rec.add2
                + ((rec.note == "") ? "" : " ※" + rec.note);
            trg = "street";
        } else {
            cap = rec.bldg;
            trg = "bldg";
        }

        if (cap.length > maxOptLen) {
            cap = cap.substr(0, maxOptLen) + sufixOpt;
        }
        $(trg).appendChild(new Element("option", { value: val }).update(cap));
    }
}

/**
 * 住所候補変更時の処理
 */
function onStreetAfterChange(e) {

    params = $("street").value.evalJSON();
    $("zip1").value = params.zip1;
    $("zip2").value = params.zip2;
    $("pref").value = params.pref;
    $("city").value = params.city;
    $("add1").value = params.add1;
    $("add2").value = params.add2;
    if ($("add2").value == "" ) {
        $("add1").focus();
    } else {
        $("add2").focus();
    }
    validate(false);
}

/**
 * 事業所候補変更時の処理
 */
function onBldgAfterChange(e) {

    params = $("bldg").value.evalJSON();
    $("zip1").value = params.zip1;
    $("zip2").value = params.zip2;
    $("pref").value = params.pref;
    $("city").value = params.city;
    $("add1").value = params.add1;
    $("add2").value = params.add2;
    $("corp").value = params.bldg;
    $("corp").focus();
    validate(false);
}

/**
 * 都道府県変更時の処理
 */
function onPrefAgfterChange(e) {

    clearInstractionArea();
    $("instarea").update($("pref" + $("pref").value).text + " の候補を検索しています。");
    $("city").focus();
    new Ajax.Request(urlJsonPref + $("pref").value + ".json",
            { method: "get", onSuccess: onCompleteGetCityJson});
}

/**
 * 市区町村データ受信完了時の処理。
 */
function onCompleteGetCityJson(jsonDocument) {

    clearInstractionArea();
    data = jsonDocument.responseText.evalJSON();

    $("instarea").appendChild(new Element("div", { id: "cityListArea"}));
    $("cityListArea").appendChild(
            new Element("select", { id: "cityList", name: "cityList" }));
    $("cityList").appendChild(
            new Element("option", { value: "-", selected: "true" }).update(
                    $("pref" + $("pref").value).text + " の市区町村の候補 "
                    + data.citylist.length + "件"));

    for (i = 0; i < data.citylist.length; ++i) {
        rec = data.citylist[i];
        $("cityList").appendChild(new Element("option",
                { value: rec.x0402, id: "city" + rec.x0402 }).update(rec.city));
    }

    $("cityList").observe("change", onChangeSelectCityList);
}

/**
 * 市区町村変更時の処理
 */
function onChangeSelectCityList(e) {

    x0401 = $("cityList").value;
    city = $("city" + x0401).text;
    clearInstractionArea();
    $("instarea").update(
            $("pref" + $("pref").value).text + " " + city + "の候補を検索しています。");
    $("city").value = city;
    validate(false);
    new Ajax.Request(urlJsonPref + $("pref").value + "/" + x0401.substr(2, 3) + ".json",
            { method: "get", onSuccess: onCompleteGetCityDetailJson});
}

/**
 * 市区町村詳細データ受信完了時の処理。
 */
function onCompleteGetCityDetailJson(jsonDocument) {

    clearInstractionArea();
    data = jsonDocument.responseText.evalJSON();

    cntStrt = 0;
    lastStrt = -1;
    cntBldg = 0;
    lastBldg = -1;

    for (i = 0; i < data.addresslist.length; ++i) {
        rec = data.addresslist[i];
        if (rec.bldg == "") {
            ++ cntStrt;
            lastStrt = i;
        } else {
            ++ cntBldg;
            lastBldg = i;
        }
    }
    
    if ((cntBldg + cntStrt) == 0) {
        $("instarea").update("候補が見つかりません。");
        return;
    }
        
    if (cntStrt > 0) {
        $("instarea").appendChild(new Element("div", { id: "strtArea"}));
        $("strtArea").appendChild(
            new Element("select", { id: "street", name: "street" }));
        $("street").appendChild(
            new Element("option", { value: "-", selected: "true" }).update(
                    data.pref + " " + data.city + " の住所の候補 "
                    + cntStrt + "件"));
        $("street").observe("change", onStreetAfterChange);
    }    
    
    if (cntBldg > 0) {
        $("instarea").appendChild(new Element("div", { id: "bldgArea"}));
        $("bldgArea").appendChild(
            new Element("select", { id: "bldg", name: "bldg" }));
        $("bldg").appendChild(
            new Element("option", { value: "-", selected: "true" }).update(
                    data.pref + " " + data.city + " の事業所の候補 "
                    + cntBldg + "件"));
        $("bldg").observe("change", onBldgAfterChange);
    }    

    for (i = 0; i < data.addresslist.length; ++i) {

        rec = data.addresslist[i];
        val = $H({zip1: rec.zip.substr(0, 3), zip2: rec.zip.substr(3, 4),
            pref: $("pref").value, city: data.city,
            add1: rec.add1, add2: rec.add2, bldg: rec.bldg}).toJSON();

        if (rec.bldg == "") {
            cap = rec.zip.substr(0, 3) + "-" + rec.zip.substr(3, 4) + " "
                + rec.add1 + rec.add2 + " " + ((rec.note == "") ? "" : " ※" + rec.note);
            trg = "street";
        } else {
            cap = rec.zip.substr(0, 3) + "-" + rec.zip.substr(3, 4) + " " + rec.bldg;
            trg = "bldg";
        }
        
        if (cap.length > maxOptLen) {
            cap = cap.substr(0, maxOptLen) + sufixOpt;
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
        $(messageid).update("半角数字で入力してください。");
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

    if ($("sname").value == "") {
        ret = false;
        nextForcus = "sname";
        $("namemsg").update("必須");
    } else if ($("gname").value == "") {
        ret = false;
        nextForcus = "gname";
        $("namemsg").update("必須");
    } else {
        $("namemsg").update("");
    }

    if ($("skana").value == "") {
        ret = false;
        nextForcus = "skana";
        $("kanamsg").update("必須");
    } else if ($("gkana").value == "") {
        ret = false;
        nextForcus = "gkana";
        $("kanamsg").update("必須");
    } else if ($("skana").value.match(regexpNgFrigana)) {
        ret = false;
        nextForcus = "skana";
        $("kanamsg").update("ふりがなはひらがなで入力してください。");
    } else if ($("gkana").value.match(regexpNgFrigana)) {
        ret = false;
        nextForcus = "gkana";
        $("kanamsg").update("ふりがなはひらがなで入力してください。");
    } else {
        $("kanamsg").update("");
    }

    if (($("add1").value == "") &&
        ($("corp").value == "")) {
        ret = false;
        nextForcus = "add1";
        $("add1msg").update("必須");
    } else {
        $("add1msg").update("");
    }

    if ($("city").value == "") {
        ret = false;
        nextForcus = "city";
        $("citymsg").update("必須");
    } else {
        $("citymsg").update("");
    }

    if ($("pref").value == "-") {
        ret = false;
        nextForcus = "pref";
        $("prefmsg").update("必須");
    } else {
        $("prefmsg").update("");
    }

    if (($("zip1").value.length < 3)) {
        ret = false;
        nextForcus = "zip1";
        $("zipmsg").update("必須");
    } else if ($("zip2").value.length < 4) {
        ret = false;
        nextForcus = "zip2";
        $("zipmsg").update("必須");
    } else if ($("zip1").value.match(/[^0-9]/)) {
        ret = false;
        nextForcus = "zip1";
        $("zipmsg").update("半角数字で入力してください。");
    } else if ($("zip2").value.match(/[^0-9]/)) {
        ret = false;
        nextForcus = "zip2";
        $("zipmsg").update("半角数字で入力してください。");
    } else {
        $("zipmsg").update("");
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

    $("zip1").value = "";
    $("zip2").value = "";
    $("pref").value = "-";
    $("city").value = "";
    $("add1").value = "";
    $("add2").value = "";
    $("corp").value = "";
    $("part").value = "";
    $("skana").value = "";
    $("gkana").value = "";
    $("sname").value = "";
    $("gname").value = "";

    $("zipmsg").update("");
    $("prefmsg").update("");
    $("citymsg").update("");
    $("add1msg").update("");
    $("add2msg").update("");
    $("corpmsg").update("");
    $("partmsg").update("");
    $("kanamsg").update("");
    $("namemsg").update("");
    
    validate(true);

    clearInstractionArea();
    $("instarea").insert(new Element("p").update(
            "郵便番号を入力するか、都道府県を選択してください。"));
}

/**
 * インストラクション領域の初期化
 */
function clearInstractionArea() {
    $("instarea").update("");
    contents = $("instarea").childNodes;
    for (i = 0; i < contents.length; ++i) {
        $("instarea").removeChild(contents[i]);
    }
    $("instarea").writeAttribute("style", "height: 4em; padding: 4px 8px 4px 8px;");
}

/**
 * テキストボックスのフォーカス時の色を設定する。
 */
function setOnFocusBackgroundColor(targetId) {
    $(targetId).observe("focus",   function () {
        $(targetId).writeAttribute("style", "background-color: #FFFACD;");
    });
    $(targetId).observe("blur",    function () {
        $(targetId).writeAttribute("style", "background-color: #FFFFFF;");
    });
}
