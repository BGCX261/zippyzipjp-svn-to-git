#!/bin/sh
#
# Copyright 2008,2009 Michinobu Maeda.
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#      http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# 元データを UTF-8 に変換したものと、出力結果 ( area.csv, firm.csv ) を
# 突き合わせてチェックします。zippyzip 2.0 の場合 firm.csv については差異が
# ないはずです。 area.csv については、「、」を展開した箇所などが出力されます。
#
# Mac OS X でテストしましたが、少し変更すれば Linux 等でも使えると思います。
#
cat data/csv/ken_all_utf8.csv \
 |sed -E "s/^([^,]+),([^,]+),([^,]+),([^,]+),([^,]+),([^,]+),([^,]+),([^,]+),([^,]+),.*/\3,\1,\9,,,\6,,,/g" \
 |sed -E s/\"//g \
 |sed -E "s/,以下に掲載がない場合,,,イカニケイサイガナイバアイ,,,/,,,,,,,以下に掲載がない場合/g" \
 |sed -E "s/\(([^\(,]+)\),/,\1/g" \
 |sed -E "s/,その他,,([^,]+),ソノタ,,/,,,\1,,,その他/g" \
 > ken_all_utf8_test.csv
cat data/csv/area.csv \
 |sed -E s/\"//g \
 > area_test.csv
diff ken_all_test.csv area_test.csv > area_diff.txt
cat data/csv/jigyosyo_utf8.csv \
 |sed -E "s/^([^,]+),([^,]+),([^,]+),([^,]+),([^,]+),([^,]+),([^,]+),([^,]+),([^,]+),.*/\8,\1,\6,\7,\3,,,\2,/g" \
 |sed -E s/\"//g \
 > jigyosyo_utf8_test.csv
cat data/csv/firm.csv \
 |sed -E s/\"//g \
 > firm_test.csv
diff jigyosyo_utf8_test.csv firm_test.csv > firm_diff.txt
