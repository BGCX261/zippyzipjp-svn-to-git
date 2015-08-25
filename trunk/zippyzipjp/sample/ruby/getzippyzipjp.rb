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

require "open-uri"
require "rexml/document"

conf_path = "" + $0
conf_path[/\/[^\/]*$/] = "/getzippyzipjp_conf.rb"
require conf_path

puts Time.now.ctime + " start."

# JSON細切れデータの圧縮ファイルを保存するディレクトリを確認する。
unless (test(?d, ARCDIR))
  unless (0 == Dir.mkdir(ARCDIR))
    puts "Faild to mkdir " + ARCDIR
    exit(false)
  end
end

# JSON細切れデータを保存するディレクトリを確認する。
unless (test(?d, TARGET))
  unless (0 == Dir.mkdir(TARGET))
    puts "Faild to mkdir " + TARGET
    exit(false)
  end
end

# 更新情報を取得する。
text_curr = ""
open(CHECK) {|io|
  text_curr = io.read
}

# 更新されていなければ終了。
PREV = ARCDIR + "/feed.prev"
text_prev = ""
if (test(?e, PREV))
  open(PREV) {|io|
    text_prev = io.read
  }
end

if (text_curr == text_prev)
  puts "exit."
  exit
end

# 更新情報を保存する。
open(PREV, "w") {|io|
  io.write text_curr
}

# 圧縮ファイルを取得して解凍する。
doc = REXML::Document.new text_curr
doc.elements.each("//a") {|element|
  url = element.attributes["href"]
  if (url[/\/json[^\/]+\.zip$/])
    name = "" + url
    name[/.*\//] = ""
    
    zip = ""
    open(url) {|io|
      zip = io.read
    }
    
    path = ARCDIR + "/" + name
    open(path, "w") {|io|
      io.write zip
    }
    
    puts Time.now.ctime + " " + name + " OK " + File.size(path).to_s + " byte"
    system(ZIPCOMMAND + path)
    
  end
}

puts Time.now.ctime + " complete."
