#!/usr/bin/python
# -*- coding: UTF-8 -*-
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
import os, time, filecmp, shutil, urllib, re, zipfile
from xml.dom.minidom import parse
import const, getzippyzipjp_conf

filenamepttn = re.compile("[^\/]*$")

# JSON細切れデータの圧縮ファイルを保存するディレクトリを確認する。
print time.ctime() + " start."

if not os.path.isdir(const.arcdir):
    os.mkdir(const.arcdir)

if not os.path.isdir(const.arcdir):
    print "Error: Faild to mkdir " + const.arcdir
    os._exit(1)

# JSON細切れデータを保存するディレクトリを確認する。
if not os.path.isdir(const.target):
    os.mkdir(const.target)

if not os.path.isdir(const.target):
    print "Error: Faild to mkdir " + const.arcdir
    os._exit(1)

# 更新情報を取得する。
const.check = const.arcdir + "/" + filenamepttn.search(const.checkUrl, 0).group(0)
const.prev = re.compile(".*\/").search(const.check, 0).group(0) + "prev.txt"
urllib.urlretrieve(const.checkUrl, const.check)

# 更新されていなければ終了。
if os.path.exists(const.prev) and filecmp.cmp(const.check, const.prev):
    print "exit."
    os._exit(0)

# 更新情報を保存する。
shutil.copy(const.check, const.prev)

# 圧縮ファイルを取得して解凍する。
arcpttn = re.compile("\/json[^\/]*\.zip$")
doc = parse(const.prev)

for a in doc.getElementsByTagName("a"):
    
    url = a.getAttribute("href")
    
    if not arcpttn.search(url, 0):
        continue

    name = filenamepttn.search(url, 0).group(0)
    trg = const.arcdir + "/" + name
    urllib.urlretrieve(url, trg)
    
    if not os.path.isfile(trg):
        print "Error: Failed to open archive: " + name
        os._exit(1)
    
    print time.ctime() + " " + name + " OK. " + str(os.path.getsize(trg)) + " byte"
    zip = zipfile.ZipFile(trg, "r")
    
    for ent in zip.namelist():
        f = open(const.target + "/" + ent, 'w')
        f.write(zip.read(ent))
        f.close()
        
    zip.close()

doc.unlink()
print time.ctime() + " complete."
