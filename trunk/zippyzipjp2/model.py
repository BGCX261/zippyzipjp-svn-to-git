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
from google.appengine.ext import db

# Key Name: ken_all, jigyosyo
class SourceContent(db.Model):
    url = db.StringProperty()
    checked = db.DateTimeProperty()
    updated = db.DateTimeProperty()
    handled = db.DateTimeProperty()
    hash = db.StringProperty()

# Key Name: ken_01, ken_02, ... ken_47, jigyosyo
class Archive(db.Model):
    url = db.StringProperty()
    handled = db.DateTimeProperty()

# Key Name: ken_all, jigyosyo
class Csv(db.Model):
    updated = db.DateTimeProperty()
    handled = db.DateTimeProperty()

# Key Name: 01, 02, ... 47
class CsvPref(db.Model):
    updated = db.DateTimeProperty()
    handled = db.DateTimeProperty()

# Key Name: 01101, 01102, ...
class CsvData(db.Model):
    updated = db.DateTimeProperty()
    data = db.TextProperty()
    handled = db.DateTimeProperty()

# Key Name: 01, 02, ... 47
class Pref(db.Model):
    name = db.StringProperty()
    yomi = db.StringProperty()
    updated = db.DateTimeProperty()

# Key Name: 01101, 01102, ...
class City(db.Model):
    name = db.StringProperty()
    yomi = db.StringProperty()
    updated = db.DateTimeProperty()

# Key Name: area, corp
class Group(db.Model):
    updated = db.DateTimeProperty()

# Key Name: 999
class Major(db.Model):
    updated = db.DateTimeProperty()

# Key Name: 9999
class Minor(db.Model):
    updated = db.DateTimeProperty()

# Key Name: hash
class Address(db.Model):
    add1 = db.StringProperty()
    add2 = db.StringProperty()
    bldg = db.StringProperty()
    yom1 = db.StringProperty()
    yom2 = db.StringProperty()
    byom = db.StringProperty()
    note = db.StringProperty()
    updated = db.DateTimeProperty()
