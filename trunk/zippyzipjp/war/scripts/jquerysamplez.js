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
        },
        "キャンセル": function() {
            
            // 住所選択リストを閉じる。
            $(this).dialog('close');
            // フォーカスを都道府県の選択リストに移動する。
            $('#pref').focus();
        }
    }});
}

/**
 * 住所の候補の選択リストを表示する。
 */
function showAddList() {
    
    // 住所候補リストダイアログを表示する。
    $.ajax ({
        url: '/list/' + $('#zip1').val() + $('#zip2').val() + '.json',
        dataType: 'json',
        success: function(data) {
        
            $('#addList').replaceWith(
                    '<select id="addList" name="addList"></select>');
            
            for (i = 0; i < data.zips.length; ++i) {
                $('#addList').append(
                        '<option value="' +
                        (data.zips[i].x0402 ? data.zips[i].x0402 : '') + "\t" +
                        (data.zips[i].city ? data.zips[i].city : '') + "\t" +
                        (data.zips[i].add1 ? data.zips[i].add1 : '') + "\t" +
                        (data.zips[i].add2 ? data.zips[i].add2 : '') + "\t" +
                        (data.zips[i].corp ? data.zips[i].corp : '') + '"' +
                        ((i == 0) ? ' selected' : '') + '>' +
                        (data.zips[i].pref ? data.zips[i].pref : '')+ ' ' +
                        (data.zips[i].city ? data.zips[i].city : '') + ' ' +
                        (data.zips[i].add1 ? data.zips[i].add1 : '') + ' ' +
                        (data.zips[i].add2 ? data.zips[i].add2 : '') + ' ' +
                        (data.zips[i].corp ? data.zips[i].corp : '') + ' ' +
                        (data.zips[i].note ? data.zips[i].note : '') +
                        '</option>');
            }
            $('#addListMessage').replaceWith('<span id="addListMessage">住所の候補 ' +
                    data.zips.length + '件</span>');
            $('#addListDialog').dialog('open');
            
            // 住所候補表示ボタンを有効にする。
            $('#showAdd').removeAttr('disabled');
        }
    });
}

/**
 * ページロード時の処理。
 */
$(function () {
    
    // 候補表示ボタンを無効にする。
    $('#showAdd').attr('disabled', 'disabled');
    
    // 住所の候補の選択リストを作成する。
    createAddList();
    // 都道府県の候補の選択リストを作成する。
    createPrefList();    

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
   
    // ページのロード時と、リセットボタンクリック時に先頭のテキストボックスにフォーカスをあてる。
    $("input[type='text']:enabled:first").focus();
    $('#reset').click(function() {
        // 候補表示ボタンを無効にする。
        $('#showAdd').attr('disabled', 'disabled');
        $("input[type='text']:enabled:first").focus();
        $('#addListDialog').dialog('close');
    });
});
