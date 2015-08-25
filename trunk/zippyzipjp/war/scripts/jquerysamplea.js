/* 
 * zippyzipjp: jQury で利用するプログラムのサンプル
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
 */

/**
 * 都道府県の候補の選択リストを作成する。
 */
function createPrefList() {
    
    $.ajax ({
        url: '/list/prefs.json',
        dataType: 'json',
        success: function(data) {
        
            $('#pref').replaceWith(
                    '<select id="pref" name="pref">' +
                    '<option value="-" selected>-- 選択してください --</option>' +
                    '</select>');
            
            for (i = 0; i < data.prefs.length; ++i) {
                $('#pref').append(
                        '<option id="x0401-' + data.prefs[i].code + '" value="' +
                        data.prefs[i].code + '">' + data.prefs[i].name + '</option>');
            }
            
            // 選択イベントを設定する。
            $('#pref').change(function(e) {
                
                if ($('#pref').val() == '-') {
                    
                    // 市区町村候補表示ボタンを無効にする。
                    $('#showCity').attr('disabled', 'disabled');
                    // 住所１候補表示ボタンを無効にする。
                    $('#showAdd1').attr('disabled', 'disabled');
                    // 事業所候補表示ボタンを無効にする。
                    $('#showCorp').attr('disabled', 'disabled');
                    // 市区町村候補リストダイアログが表示されていたら閉じる。
                    $('#cityListDialog').dialog('close');
                    
               } else {
                    
                    // 市区町村候補表示ボタンを有効にする。
                    $('#showCity').removeAttr('disabled');
                    // 次の要素にフォーカスを移動する準備。
                    $(this).blur();
                    // 市区町村候補リストダイアログを表示する。
                    showCityList();
                }
            });
        }
    });
}

/**
 * 市区町村の候補の選択リストを作成する。
 */
function createCityList() {
    
    $('body').append(
            '<div id="cityListDialog" title="市区町村を選択してください">' +
            '<div><span id="cityListMessage">選択候補がありません。</span></div>' +
            '<div><span id="cityList"></span></div>' +
            '</div>');
    $('#cityListDialog').dialog({ autoOpen: false, width: '480px', 
        buttons: {
        "OK": function() {
        
            // 市区町村候補選択リストの値を取得する。
            if ($('#cityList').val()) {
                
                data = $('#cityList').val().split("\t");
                
                // 選択された値を設定する。
                $('#pref').val(data[0].substring(0, 2));
                $('#city').val(data[1]);
                $('#x0402').attr('value', data[0]);
                
                // 住所１候補表示ボタンを有効にする。
                $('#showAdd1').removeAttr('disabled');
                // 事業所候補表示ボタンを有効にする。
                $('#showCorp').removeAttr('disabled');
            }
            
            // 市区町村候補選択リストを閉じる。
            $(this).dialog('close');
            // 住所１の候補のリストを表示する。
            showAdd1List();
        },
        "キャンセル": function() {
            
            // 市区町村候補選択リストを閉じる。
            $(this).dialog('close');
            // フォーカスを市区町村に移動する。
            $('#city').focus();
        }
    }});
}

/**
 * 住所１の候補の選択リストを作成する。
 */
function createAdd1List() {
    
    $('body').append(
            '<div id="add1ListDialog" title="住所を選択してください">' +
            '<div><span id="add1ListMessage">選択候補がありません。</span></div>' +
            '<div><span id="add1List"></span></div>' +
            '</div>');
    $('#add1ListDialog').dialog({ autoOpen: false, width: '780px', 
        buttons: {
        "OK": function() {
        
            // 住所１候補選択リストの値を取得する。
            if ($('#add1List').val()) {
                
                data = $('#add1List').val().split("\t");
                
                // 選択された値を設定する。
                $('#zip1').val(data[0]);
                $('#zip2').val(data[1]);
                $('#pref').val(data[2].substring(0, 2));
                $('#city').val(data[3]);
                $('#x0402').attr('value', data[2]);
                $('#add1').val(data[4]);
                $('#add2').val(data[5]);
            }
            
            // 住所１候補選択リストを閉じる。
            $(this).dialog('close');
            
            if (data[0]) {
                
                // フォーカスを移動する。
                if ($('#add2').val() != '') {
                    $('#add2').focus();
                } else if ($('#add1').val() != '') {
                    $('#add1').focus();
                } else if ($('#city').val() == '') {
                    $('#city').focus();
                } else {
                    $('#add1').focus();
                }
                
            } else {
            
                // 住所２候補表示ボタンを有効にする。
                $('#showAdd2').removeAttr('disabled');
                // 住所２の候補のリストを表示する。
                showAdd2List();
            }
        },
        "キャンセル": function() {
            
            // 住所１候補選択リストを閉じる。
            $(this).dialog('close');
            // フォーカスを住所１に移動する。
            $('#add1').focus();
        },
        "事業所の候補": function() {
            
            // 住所１候補選択リストを閉じる。
            $(this).dialog('close');
            // 事業所候補リストダイアログを表示する。
            showCorpList();
        },
    }});
}

/**
 * 住所２の候補の選択リストを作成する。
 */
function createAdd2List() {
    
    $('body').append(
            '<div id="add2ListDialog" title="住所を選択してください">' +
            '<div><span id="add2ListMessage">選択候補がありません。</span></div>' +
            '<div><span id="add2List"></span></div>' +
            '</div>');
    $('#add2ListDialog').dialog({ autoOpen: false, width: '780px', 
        buttons: {
        "OK": function() {
        
            // 住所２候補選択リストの値を取得する。
            if ($('#add2List').val()) {
                data = $('#add2List').val().split("\t");
                // 選択された値を設定する。
                $('#zip1').val(data[0]);
                $('#zip2').val(data[1]);
                $('#pref').val(data[2].substring(0, 2));
                $('#city').val(data[3]);
                $('#x0402').attr('value', data[2]);
                $('#add2').val(data[4]);
            }
            // 住所２候補選択リストを閉じる。
            $(this).dialog('close');
            
            // フォーカスを移動する。
            if ($('#add2').val() != '') {
                $('#add2').focus();
            } else if ($('#add1').val() != '') {
                $('#add1').focus();
            } else if ($('#city').val() == '') {
                $('#city').focus();
            } else {
                $('#add1').focus();
            }
        },
        "キャンセル": function() {
            
            // 住所２候補選択リストを閉じる。
            $(this).dialog('close');
            // フォーカスを住所２に移動する。
            $('#add2').focus();
        },
    }});
}

/**
 * 事業所の候補の選択リストを作成する。
 */
function createCorpList() {
    
    $('body').append(
            '<div id="corpListDialog" title="事業所を選択してください">' +
            '<div><span id="corpListMessage">選択候補がありません。</span></div>' +
            '<div><span id="corpList"></span></div>' +
            '</div>');
    $('#corpListDialog').dialog({ autoOpen: false, width: '780px', 
        buttons: {
        "OK": function() {
        
            // 事業所候補選択リストの値を取得する。
            if ($('#corpList').val()) {
                data = $('#corpList').val().split("\t");
                // 選択された値を設定する。
                $('#zip1').val(data[0]);
                $('#zip2').val(data[1]);
                $('#pref').val(data[2].substring(0, 2));
                $('#city').val(data[3]);
                $('#x0402').attr('value', data[2]);
                $('#add1').val(data[4]);
                $('#add2').val(data[5]);
                if (data[6]) { $('#corp').val(data[6]); }
            }
            // 事業所候補選択リストを閉じる。
            $(this).dialog('close');
            
            // フォーカスを移動する。
            if ($('#corp').val() != '') {
                $('#corp').focus();
            } else if ($('#add2').val() != '') {
                $('#add2').focus();
            } else if ($('#add1').val() != '') {
                $('#add1').focus();
            } else if ($('#city').val() == '') {
                $('#city').focus();
            } else {
                $('#add1').focus();
            }
        },
        "キャンセル": function() {
            
            // 事業所候補選択リストを閉じる。
            $(this).dialog('close');
            // フォーカスを住所１に移動する。
            $('#add1').focus();
        },
        "住所の候補": function() {
            
            // 事業所候補選択リストを閉じる。
            $(this).dialog('close');
            // 住所１候補リストダイアログを表示する。
            showAdd1List();
        },
    }});
}

/**
 * 市区町村の候補の選択リストを表示する。
 */
function showCityList() {

    $.ajax ({
        url: '/list/' + $('#pref').val() +  '.json',
        dataType: 'json',
        success: function(data) {
        
            $('#cityList').replaceWith(
                    '<select id="cityList" name="cityList"></select>');
            
            for (i = 0; i < data.cities.length; ++i) {
                $('#cityList').append(
                        '<option value="' +
                        data.cities[i].code + "\t" +
                        data.cities[i].name + '"' +
                        ((i == 0) ? ' selected' : '') + '>' +
                        $('#x0401-' + data.pref.code).text() + ' ' +
                        data.cities[i].name +
                        '</option>');
            }
            $('#cityListMessage').replaceWith(
                    '<span id="cityListMessage">市区町村の候補 ' +
                    data.cities.length + '件</span>');
            $('#cityListDialog').dialog('open');
        }
    });
}

/**
 * 住所１の候補の選択リストを表示する。
 */
function showAdd1List() {

    $.ajax ({
        url: '/list/' + $('#x0402').val() + '.json',
        dataType: 'json',
        success: function(data) {
            
            $('#add1List').replaceWith(
                    '<select id="add1List" name="add1List"></select>');
            
            for (i = 0; i < data.zips.length; ++i) {
                    
                $('#add1List').append(
                        '<option value="' +
                        (data.zips[i].zip1 ? data.zips[i].zip1 : '') + "\t" +
                        (data.zips[i].zip2 ? data.zips[i].zip2 : '') + "\t" +
                        (data.city.code    ? data.city.code : '') + "\t" +
                        (data.city.name    ? data.city.name : '') + "\t" +
                        (data.zips[i].add1 ? data.zips[i].add1 : '') + "\t" +
                        (data.zips[i].add2 ? data.zips[i].add2 : '') + "\t" +
                        (data.zips[i].corp ? data.zips[i].corp : '') + '"' +
                        ((i == 0) ? ' selected' : '') + '>' +
                        (data.zips[i].zip1 ? (data.zips[i].zip1 + '-') : '') +
                        (data.zips[i].zip2 ? data.zips[i].zip2 : '') + ' ' +
                        (data.zips[i].add1 ? data.zips[i].add1 : '') + ' ' +
                        (data.zips[i].add2 ? (data.zips[i].add2.length < 20 ? data.zips[i].add2 : data.zips[i].add2.substring(0, 17) + '...') : '') + ' ' +
                        (data.zips[i].corp ? (data.zips[i].corp.length < 20 ? data.zips[i].corp : data.zips[i].corp.substring(0, 17) + '...') : '') + ' ' +
                        (data.zips[i].note ? (' ' + data.zips[i].note) : '') +
                        '</option>');
            }
            $('#add1ListMessage').replaceWith(
                    '<span id="add1ListMessage">住所の候補 ' +
                    data.zips.length + '件</span>');
            $('#add1ListDialog').dialog('open');
        }
    });
}

function toHex(str) {
    
    ret = "";
    
    for (i = 0; i < str.length; ++i) {
        add = "0000" + Number(str.charCodeAt(i)).toString(16);
        ret = ret + add.substr(add.length - 4, 4);
    }
    
    return ret;
}

/**
 * 住所２の候補の選択リストを表示する。
 */
function showAdd2List() {

    $.ajax ({
        url: '/list/' + $('#x0402').val() + '-' + toHex($('#add1').val()) + '.json',
        dataType: 'json',
        success: function(data) {
            
            $('#add2List').replaceWith(
                    '<select id="add2List" name="add2List"></select>');
            
            for (i = 0; i < data.zips.length; ++i) {
                    
                $('#add2List').append(
                        '<option value="' +
                        (data.zips[i].zip1 ? data.zips[i].zip1 : '') + "\t" +
                        (data.zips[i].zip2 ? data.zips[i].zip2 : '') + "\t" +
                        (data.city.code    ? data.city.code : '') + "\t" +
                        (data.city.name    ? data.city.name : '') + "\t" +
                        (data.zips[i].add2 ? data.zips[i].add2 : '') + "\t" +
                        (data.zips[i].corp ? data.zips[i].corp : '') + '"' +
                        ((i == 0) ? ' selected' : '') + '>' +
                        (data.zips[i].zip1 ? (data.zips[i].zip1 + '-') : '') +
                        (data.zips[i].zip2 ? data.zips[i].zip2 : '') + ' ' +
                        (data.zips[i].add2 ? (data.zips[i].add2.length < 20 ? data.zips[i].add2 : data.zips[i].add2.substring(0, 17) + '...') : '') + ' ' +
                        (data.zips[i].corp ? (data.zips[i].corp.length < 20 ? data.zips[i].corp : data.zips[i].corp.substring(0, 17) + '...') : '') + ' ' +
                        (data.zips[i].note ? (' ' + data.zips[i].note) : '') +
                        '</option>');
            }
            $('#add2ListMessage').replaceWith(
                    '<span id="add2ListMessage">住所の候補 ' +
                    data.zips.length + '件</span>');
            $('#add2ListDialog').dialog('open');
        }
    });
}

/**
 * 事業所候補の選択リストを表示する。
 */
function showCorpList() {

    $.ajax ({
        url: '/list/' + $('#x0402').val() + 'c.json',
        dataType: 'json',
        success: function(data) {
            
            $('#corpList').replaceWith(
                    '<select id="corpList" name="add1List"></select>');
            
            for (i = 0; i < data.zips.length; ++i) {
                    
                $('#corpList').append(
                        '<option value="' +
                        (data.zips[i].zip1 ? data.zips[i].zip1 : '') + "\t" +
                        (data.zips[i].zip2 ? data.zips[i].zip2 : '') + "\t" +
                        (data.city.code    ? data.city.code : '') + "\t" +
                        (data.city.name    ? data.city.name : '') + "\t" +
                        (data.zips[i].add1 ? data.zips[i].add1 : '') + "\t" +
                        (data.zips[i].add2 ? data.zips[i].add2 : '') + "\t" +
                        (data.zips[i].corp ? data.zips[i].corp : '') + '"' +
                        ((i == 0) ? ' selected' : '') + '>' +
                        (data.zips[i].zip1 ? (data.zips[i].zip1 + '-') : '') +
                        (data.zips[i].zip2 ? data.zips[i].zip2 : '') + ' ' +
                        (data.zips[i].add1 ? data.zips[i].add1 : '') + ' ' +
                        (data.zips[i].add2 ? (data.zips[i].add2.length < 20 ? data.zips[i].add2 : data.zips[i].add2.substring(0, 17) + '...') : '') + ' ' +
                        (data.zips[i].corp ? (data.zips[i].corp.length < 20 ? data.zips[i].corp : data.zips[i].corp.substring(0, 17) + '...') : '') + ' ' +
                        (data.zips[i].note ? (' ' + data.zips[i].note) : '') +
                        '</option>');
            }
            $('#corpListMessage').replaceWith(
                    '<span id="corpListMessage">事業所の候補 ' +
                    data.zips.length + '件</span>');
            $('#corpListDialog').dialog('open');
        }
    });
}

/**
 * ページロード時の処理。
 */
$(function () {
    
    // 候補表示ボタンを無効にする。
    $('#showCity').attr('disabled', 'disabled');
    $('#showAdd1').attr('disabled', 'disabled');
    $('#showAdd2').attr('disabled', 'disabled');
    $('#showCorp').attr('disabled', 'disabled');

    // 都道府県の候補の選択リストを作成する。
    createPrefList();    
    // 市区町村の候補の選択リストを作成する。
    createCityList();
    // 住所１の候補の選択リストを作成する。
    createAdd1List();
    // 住所２の候補の選択リストを作成する。
    createAdd2List();
    // 事業所の候補の選択リストを作成する。
    createCorpList();

    // フォーカスイベントを設定する。
    $('#zip1, #zip2, #pref, #city, #add1, #add2, #corp').focus(function(e) {
        $(this).css('background', '#ffa');
    });
    $('#zip1, #zip2, #pref, #city, #add1, #add2, #corp').blur(function(e) {
        $(this).css('background', '#fff');
    });
    $('#showCity').click(function(e) {
        // 市区町村候補リストダイアログを表示する。
        showCityList();
        return false;
    });
    $('#showAdd1').click(function(e) {
        // 住所１候補リストダイアログを表示する。
        showAdd1List();
        return false;
    });
    $('#showAdd2').click(function(e) {
        // 住所２候補リストダイアログを表示する。
        showAdd2List();
        return false;
    });
    $('#showCorp').click(function(e) {
        // 事業所候補リストダイアログを表示する。
        showCorpList();
        return false;
    });
   
    // ページのロード時と、リセットボタンクリック時に先頭のテキストボックスにフォーカスをあてる。
    $("input[type='text']:enabled:first").focus();
    $('#reset').click(function() {
        // 候補表示ボタンを無効にする。
        $('#showCity').attr('disabled', 'disabled');
        $('#showAdd1').attr('disabled', 'disabled');
        $('#showAdd2').attr('disabled', 'disabled');
        $('#showCorp').attr('disabled', 'disabled');
        $("input[type='text']:enabled:first").focus();
        $('#addListDialog').dialog('close');
    });
});
