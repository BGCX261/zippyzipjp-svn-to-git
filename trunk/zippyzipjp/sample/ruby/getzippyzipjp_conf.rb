#!/usr/bin/ruby
#
# zippyzipjp
# 
# Copyright 2008-2010 Michinobu Maeda.
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

# JSON細切れデータの圧縮ファイルを保存するディレクトリ
ARCDIR = "/var/www/zippyzipjp/zips"

# JSON細切れデータを保存するディレクトリ
TARGET = "/var/www/zippyzipjp/list"

# データの更新情報を取得するURL
CHECK = "http://zippyzipjp.appspot.com/zippyzipjp/feed.atom"

# zip 回答のためのコマンド
ZIPCOMMAND = "/usr/bin/unzip -d " + TARGET + " -o -qq "
