/* 
 * 郵便番号JSON細切れデータを jQury で利用するプログラムのサンプル
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
        url: '/sample/json/pref/prefs.json',
        dataType: 'json',
        success: function(data) {
        
            $('#pref').replaceWith(
                    '<select id="pref" name="pref">' +
                    '<option value="-" selected>-- 選択してください --</option>' +
                    '</select>');
            
            for (i = 0; i < data.length; ++i) {
                $('#pref').append(
                        '<option id="x0401-' + data[i].x0401 + '" value="' +
                        data[i].x0401 + '">' + data[i].name + '</option>');
            }
            
            // 選択イベントを設定する。
            $('#pref').change(function(e) {
                
                if ($('#pref').val() == '-') {
                    
                    // 市区町村候補表示ボタンを無効にする。
                    $('#showCity').attr('disabled', 'disabled');
                    // 郵便番号候補表示ボタンを無効にする。
                    $('#showZip').attr('disabled', 'disabled');
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
 * 住所の候補の選択リストを作成する。
 */
function createAddList() {
    
    $('body').append(
            '<div id="addListDialog" title="住所を選択してください">' +
            '<div><span id="addListMessage">選択候補がありません。</span></div>' +
            '<div><span id="addList"></span></div>' +
            '</div>');
    $('#addListDialog').dialog({ autoOpen: false, width: '780px', 
        buttons: {
        "キャンセル": function() {
        
            // 住所選択リストを閉じる。
            $(this).dialog('close');
            // フォーカスを都道府県の選択リストに移動する。
            $('#pref').focus();
        },
        "OK": function() {
        
            // 住所選択リストの値を取得する。
            data = $('#addList').val().split("\t");
            // 住所選択リストを閉じる。
            $(this).dialog('close');
            
            // 選択された値を設定する。
            $('#pref').val(data[0].substring(0, 2));
            $('#city').val(data[1]);
            $('#add1').val(data[2]);
            $('#add2').val(data[3]);
            if (data[4] != '') {
                $('#corp').val(data[4]);
            }
            
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
        }}
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
        "キャンセル": function() {
        
            // 市区町村候補選択リストを閉じる。
            $(this).dialog('close');
            // フォーカスを市区町村に移動する。
            $('#city').focus();
        },
        "OK": function() {
        
            // 市区町村候補選択リストの値を取得する。
            if ($('#cityList').val()) {
                
                data = $('#cityList').val().split("\t");
                
                // 選択された値を設定する。
                $('#pref').val(data[0].substring(0, 2));
                $('#city').val(data[1]);
                $('#x0402').attr('value', data[0]);
                
                // 郵便番号候補表示ボタンを有効にする。
                $('#showZip').removeAttr('disabled');
            }
            
            // 市区町村候補選択リストを閉じる。
            $(this).dialog('close');
            // 郵便番号の候補のリストを表示する。
            showZipList();
        }}
    });
}

/**
 * 郵便番号の候補の選択リストを作成する。
 */
function createZipList() {
    
    $('body').append(
            '<div id="zipListDialog" title="郵便番号を選択してください">' +
            '<div><span id="zipListMessage">選択候補がありません。</span></div>' +
            '<div><span id="zipList"></span></div>' +
            '</div>');
    $('#zipListDialog').dialog({ autoOpen: false, width: '780px', 
        buttons: {
        "キャンセル": function() {
        
            // 郵便番号候補選択リストを閉じる。
            $(this).dialog('close');
            // フォーカスを住所１に移動する。
            $('#add1').focus();
        },
        "OK": function() {
        
            // 郵便番号候補選択リストの値を取得する。
            if ($('#zipList').val()) {
                data = $('#zipList').val().split("\t");
                // 選択された値を設定する。
                $('#zip1').val(data[0].substring(0, 3));
                $('#zip2').val(data[0].substring(3, 7));
                $('#pref').val(data[1].substring(0, 2));
                $('#city').val(data[2]);
                $('#x0402').attr('value', data[1]);
                $('#add1').val(data[3]);
                $('#add2').val(data[4]);
                if (data[5]) {
                    $('#corp').val(data[5]);
                }
            }
            // 郵便番号候補選択リストを閉じる。
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
        }}
    });
}

/**
 * 住所の候補の選択リストを表示する。
 */
function showAddList() {
    
    // 住所候補リストダイアログを表示する。
    $.ajax ({
        url: '/sample/json/zip/' + $('#zip1').val() + '/' + $('#zip2').val() + '.json',
        dataType: 'json',
        success: function(data) {
        
            $('#addList').replaceWith(
                    '<select id="addList" name="addList"></select>');
            
            for (i = 0; i < data.addresslist.length; ++i) {
                $('#addList').append(
                        '<option value="' +
                        data.addresslist[i].x0402 + "\t" +
                        data.addresslist[i].city + "\t" +
                        data.addresslist[i].add1 + "\t" +
                        data.addresslist[i].add2 + "\t" +
                        data.addresslist[i].bldg + '"' +
                        ((i == 0) ? ' selected' : '') + '>' +
                        data.addresslist[i].pref + ' ' +
                        data.addresslist[i].city + ' ' +
                        data.addresslist[i].add1 + ' ' +
                        data.addresslist[i].add2 + ' ' +
                        data.addresslist[i].bldg + ' ' +
                        data.addresslist[i].note +
                        '</option>');
            }
            $('#addListMessage').replaceWith('<span id="addListMessage">住所の候補 ' +
                    data.addresslist.length + '件</span>');
            $('#addListDialog').dialog('open');
            
            // 住所候補表示ボタンを有効にする。
            $('#showAdd').removeAttr('disabled');
        }
    });
}

/**
 * 市区町村の候補の選択リストを表示する。
 */
function showCityList() {

    $.ajax ({
        url: '/sample/json/pref/' + $('#pref').val() +  '.json',
        dataType: 'json',
        success: function(data) {
        
            $('#cityList').replaceWith(
                    '<select id="cityList" name="cityList"></select>');
            
            for (i = 0; i < data.citylist.length; ++i) {
                $('#cityList').append(
                        '<option value="' +
                        data.citylist[i].x0402 + "\t" +
                        data.citylist[i].city + '"' +
                        ((i == 0) ? ' selected' : '') + '>' +
                        $('#x0401-' + data.x0401).text() + ' ' +
                        data.citylist[i].city +
                        '</option>');
            }
            $('#cityListMessage').replaceWith(
                    '<span id="cityListMessage">市区町村の候補 ' +
                    data.citylist.length + '件</span>');
            $('#cityListDialog').dialog('open');
        }
    });
}

/**
 * 郵便番号の候補の選択リストを表示する。
 */
function showZipList() {

    $.ajax ({
        url: '/sample/json/pref/' + $('#x0402').val().substring(0, 2) +
                              '/' + $('#x0402').val().substring(2, 5) + '.json',
        dataType: 'json',
        success: function(data) {
            
            $('#zipList').replaceWith(
                    '<select id="zipList" name="zipList"></select>');
            
            for (i = 0; i < data.addresslist.length; ++i) {
                    
                $('#zipList').append(
                        '<option value="' +
                        data.addresslist[i].zip + "\t" +
                        data.x0402 + "\t" +
                        data.city + "\t" +
                        data.addresslist[i].add1 + "\t" +
                        data.addresslist[i].add2 + "\t" +
                        data.addresslist[i].bldg + '"' +
                        ((i == 0) ? ' selected' : '') + '>' +
                        data.addresslist[i].zip.substring(0, 3) + '-' +
                        data.addresslist[i].zip.substring(3, 7) + ' ' +
                        data.addresslist[i].add1 + ' ' +
                        (data.addresslist[i].add2.length < 20 ? data.addresslist[i].add2 : data.addresslist[i].add2.substring(0, 17) + '...') + ' ' +
                        (data.addresslist[i].bldg.length < 20 ? data.addresslist[i].bldg : data.addresslist[i].bldg.substring(0, 17) + '...') + ' ' +
                        data.addresslist[i].note +
                        '</option>');
            }
            $('#zipListMessage').replaceWith(
                    '<span id="zipListMessage">郵便番号の候補 ' +
                    data.addresslist.length + '件</span>');
            $('#zipListDialog').dialog('open');
        }
    });
}

/**
 * ページロード時の処理。
 */
$(function () {
    
    // 候補表示ボタンを無効にする。
    $('#showCity').attr('disabled', 'disabled');
    $('#showAdd').attr('disabled', 'disabled');
    $('#showZip').attr('disabled', 'disabled');
    
    // Copyright の年を計算して設定する。
    var crYear = 2010;
    var crNow = new Date().getYear() + 1900;

    if (crYear == (crNow - 1)) {
        crYear += ',' + crNow;
    } else if (crYear < crNow) {
        crYear += '-' + crNow;
    }
    
    $('#crYear').append(crYear);
    
    // 住所の候補の選択リストを作成する。
    createAddList();
    // 都道府県の候補の選択リストを作成する。
    createPrefList();    
    // 市区町村の候補の選択リストを作成する。
    createCityList();
    // 郵便番号の候補の選択リストを作成する。
    createZipList();

    // フォーカスイベントを設定する。
    $('#zip1, #zip2, #pref, #city, #add1, #add2, #corp').focus(function(e) {
        $(this).css('background', '#ffa');
    });
    $('#zip1, #zip2, #pref, #city, #add1, #add2, #corp').blur(function(e) {
        $(this).css('background', '#fff');
    });
    
    // キーイベントを設定する。
    $('#zip1, #zip2').keyup(function(e) {
        
        // キーコードが数字の場合
        if ((48 <= e.keyCode) && (e.keyCode <=57)) {
            
            // 値の桁数が maxlength と等しければ、次の要素にフォーカスをあてる。
            if ($(this).val().length == $(this).attr('maxlength')) {
                
                // 次の要素にフォーカスを移動する準備。
                $(this).blur();
                
                // 郵便番号が入力済みの場合
                if ($('#zip1').val().length != $('#zip1').attr('maxlength')) {
                    
                    // 未入力の項目に移動する。
                    $('#zip1').focus();
                    
                } else if (($('#zip1').val().length == $('#zip1').attr('maxlength'))
                        && ($('#zip2').val().length == $('#zip2').attr('maxlength'))) {
                    
                    // 住所候補リストダイアログを表示する。
                    showAddList();
                
                } else {
                    
                    // 次の要素にフォーカスを移動する。
                    $(this).next().focus();
                }
            }
            
        } else if ((e.keyCode == 32 /* Space */) || (57 < e.keyCode)) {
            
            // 数字以外の値は削除する。
            if ($(this).val != $(this).val().replace(/\D/, '')) {
                $(this).val($(this).val().replace(/\D/, ''));
            }
        }
        
        if (($('#zip1').val().length != $('#zip1').attr('maxlength'))
                || ($('#zip2').val().length != $('#zip2').attr('maxlength'))) {
 
            // 住所候補表示ボタンを無効にする。
            $('#showAdd').attr('disabled', 'disabled');
        }
    });
    
    // クリックイベントを設定する。
    $('#showAdd').click(function(e) {
        // 住所候補リストダイアログを表示する。
        showAddList();
        return false;
    });
    $('#showCity').click(function(e) {
        // 市区町村候補リストダイアログを表示する。
        showCityList();
        return false;
    });
    $('#showZip').click(function(e) {
        // 郵便番号候補リストダイアログを表示する。
        showZipList();
        return false;
    });
   
    // ページのロード時と、リセットボタンクリック時に先頭のテキストボックスにフォーカスをあてる。
    $("input[type='text']:enabled:first").focus();
    $('#reset').click(function() {
        // 候補表示ボタンを無効にする。
        $('#showAdd').attr('disabled', 'disabled');
        $('#showCity').attr('disabled', 'disabled');
        $('#showZip').attr('disabled', 'disabled');
        $("input[type='text']:enabled:first").focus();
        $('#addListDialog').dialog('close');
    });
});
