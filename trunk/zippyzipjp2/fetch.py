#!/usr/bin/python
# -*- coding: UTF-8 -*-
'''
zippyzipjp2

Copyright 2011 Michinobu Maeda.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

@author: Michinobu Maeda
'''
import datetime
import logging
import hashlib
import os
import re
import StringIO
import yaml
import zipfile
from google.appengine.api import urlfetch
from google.appengine.api import taskqueue
from google.appengine.ext import db
import model

def init_src(src, url):
    if src.url is None:
        src.url = url
        src.checked = None
        src.updated = None
        src.handled = None

def check_src(src):
    logging.info('check_src("' + src.url + '")')
    ret = False
    hash = check_url(src.url)
    src.checked = datetime.datetime.now()
    if hash is None:
        pass
    elif hash != src.hash:
        src.hash = hash
        src.updated = src.checked
        src.handled = None
        ret = True
    src.put()
    return ret

def check_url(url):
    logging.info('check_url("' + url + '")')
    hash = None
    try:
        result = urlfetch.fetch(url=url, deadline=60)
        if result.status_code == 200:
            m = hashlib.md5()
            m.update(result.content)
            hash = m.hexdigest()
        logging.info(hash + ":" + url)
    except urlfetch.DownloadError:
        logging.info("<None>:" + url)
    return hash

def clear_csv():
    logging.info('clear_csv()')
    for csv in model.Csv.all():
        q1 = db.Query(model.CsvPref, keys_only=True)
        for csv_pref in q1.ancestor(csv):
            q2 = db.Query(model.CsvData, keys_only=True)
            db.delete(q2.ancestor(csv_pref))
            db.delete(csv_pref)
        db.delete(csv)

def get_csv(src, group):
    logging.info('get_csv(src, "' + group + '")')
    body = None
    result = urlfetch.fetch(url=src.url, deadline=60)
    if result.status_code == 200:
        sio = StringIO.StringIO()
        sio.write(result.content)
        zip = zipfile.ZipFile(file=sio, mode="r")
        for name in zip.namelist():
            body = zip.read(name).decode("cp932")
            logging.info("extract:" + name)
            break
        src.put()
    if body is None:
        return False
    csv = model.Csv.get_or_insert(group)
    csv.updated = datetime.datetime.now()
    csv.handled = None
    csv.put()
    pref_org = ""
    city_org = ""
    csv_data = None
    list = body.splitlines(False)
    while 0 < len(list):
        line = list.pop(0)
        pref = line[:2]
        city = line[:5]
        if city_org != city:
            city_org = city
            if pref_org != pref:
                pref_org = pref
                csv_pref = model.CsvPref.get_or_insert(key_name=pref, parent=csv)
                csv_pref.updated = csv.updated
                csv_pref.handled = None
                csv_pref.put()
            if not (csv_data is None):
                csv_data.put()
            csv_data = model.CsvData.get_or_insert(key_name=city, parent=csv_pref)
            csv_data.updated = csv.updated
            csv_data.handled = None
            csv_data.data = ""
        if group == "ken_all":
            line = line[7:-13].replace('","', "\t")
        else:
            line = line[7:-7].replace('","', "\t")
        csv_data.data += line
        csv_data.data += u"\n"
    if not (csv_data is None):
        csv_data.put()
    src.handled = datetime.datetime.now()
    return True

def generate_pref(csv_data):
    pkey = csv_data.parent_key()
    logging.info('generate_pref(' + pkey.name() + ')')
    pref = model.Pref.get_or_insert(key_name=pkey.name())
    if pref.updated == csv_data.updated:
        pass
    elif not (csv_data.data is None):
        for line in csv_data.data.splitlines(False):
            cols = line.split("\t")
            if pkey.parent().name() == "ken_all":
                pref.name = cols[5]
                pref.yomi = cols[2]
            else:
                pref.name = cols[2]
            pref.updated = csv_data.updated
            pref.put()
            break
    return pref

def generate_city(csv_data, pref):
    pkey = csv_data.parent_key()
    city = model.City.get_or_insert(key_name=csv_data.key().name(), parent=pref)
    if city.updated == csv_data.updated:
        pass
    else:
        start = True
        for line in csv_data.data.splitlines(False):
            if start:
                cols = line.split("\t")
                if pkey.parent().name() == "ken_all":
                    city.name = cols[6]
                    city.yomi = cols[3]
                    group = model.Group.get_or_insert(key_name="area", parent=city)
                else:
                    city.name = cols[3]
                    group = model.Group.get_or_insert(key_name="corp", parent=city)
                city.updated = csv_data.updated
                city.put()
                group.updated = csv_data.updated
                group.put()
                start = False
            if pkey.parent().name() == "ken_all":
                generate_area(cols, group)
            else:
                generate_corp(cols, group)
    return city

def generate_area(cols, group):
    zip = cols[1]
    major = model.Major.get_or_insert(key_name=zip[:3], parent=group)
    major.updated = group.updated
    major.put()
    minor = model.Minor.get_or_insert(key_name=zip[3:], parent=major)
    minor.updated = group.updated
    minor.put()

def generate_corp(cols, group):
    zip = cols[6]
    major = model.Major.get_or_insert(key_name=zip[:3], parent=group)
    major.updated = group.updated
    major.put()
    minor = model.Minor.get_or_insert(key_name=zip[3:], parent=major)
    minor.updated = group.updated
    minor.put()

def generate(csv):
    logging.info('generate(' + csv.key().name() + ')')
    q1 = db.Query(model.CsvPref)
    q1.ancestor(csv)
    for csv_pref in q1:
        if time_limit < datetime.datetime.now():
            return False
        if not (csv_pref.handled is None):
            continue
        pkey = csv_pref.key().name()
        q2 = db.Query(model.CsvData)
        q2.ancestor(csv_pref)
        for csv_data in q2:
            if time_limit < datetime.datetime.now():
                return False
            if not (csv_data.handled is None):
                continue
            if not (pkey is None):
                pref = generate_pref(csv_data)
                pkey = None
            city = generate_city(csv_data, pref)
            csv_data.handled = datetime.datetime.now()
            csv_data.put()
        csv_pref.handled = datetime.datetime.now()
        csv_pref.put()
    csv.handled = datetime.datetime.now()
    csv.put()
    return True

time_limit = datetime.datetime.now() + datetime.timedelta(seconds=300)
conf = yaml.load(file(os.path.join(os.path.dirname(__file__), 'conf.yaml'), 'r'))
#conf = yaml.load(file(os.path.join(os.path.dirname(__file__), 'test/conf.yaml'), 'r'))
ken_page = model.SourceContent.get_or_insert("ken_all_page")
ken_arch = model.SourceContent.get_or_insert("ken_all_arch")
ken_csv = None
jig_page = model.SourceContent.get_or_insert("jigyosyo_page")
jig_arch = model.SourceContent.get_or_insert("jigyosyo_arch")
jig_csv = None
init_src(ken_page, conf['ken_all']['page_url'])
init_src(ken_arch, conf['ken_all']['archive_url'])
init_src(jig_page, conf['jigyosyo']['page_url'])
init_src(jig_arch, conf['jigyosyo']['archive_url'])
status = "remain"
if ken_page.checked is None:
    check_src(ken_page)
elif jig_page.checked is None:
    check_src(jig_page)
elif ken_page.handled is None:
    check_src(ken_arch)
    ken_page.handled = datetime.datetime.now()
    ken_page.put()
elif jig_page.handled is None:
    check_src(jig_arch)
    jig_page.handled = datetime.datetime.now()
    jig_page.put()
elif (ken_arch.handled is None) or (jig_arch.handled is None):
    db.delete(model.Archive.all())
    for item in conf['archive']:
        arch = model.Archive.get_or_insert(key_name=item['name'])
        arch.url = item['url']
        arch.put()
    ken_arch.handled = datetime.datetime.now()
    jig_arch.handled = datetime.datetime.now()
    ken_arch.put()
    jig_arch.put()
    clear_csv()
else:
    arch = None
    for item in model.Archive.all():
        if item.handled is None:
            arch = item
            break
    if not (arch is None):
        logging.info(arch.url)
        if arch.key().name()[:1] == 'k':
            group = 'ken_all'
        else:
            group = 'jigyosyo'
        if False == get_csv(arch, group):
            status = "error"
            logging.error(arch.url)
        arch.handled = datetime.datetime.now()
        arch.put()
    else:
        ken_csv = model.Csv.get(db.Key.from_path('Csv', 'ken_all'))
        jig_csv = model.Csv.get(db.Key.from_path('Csv', 'jigyosyo'))

if ken_csv is None:
    pass
elif ken_csv.handled is None:
    generate(ken_csv)
elif jig_csv is None:
    pass
elif jig_csv.handled is None:
    generate(jig_csv)
else:
    status = "exit"
    logging.info("exit")
print 'Content-Type: text/plain'
print ''
print status
if status == "remain":
    taskqueue.add(url='/fetch')
