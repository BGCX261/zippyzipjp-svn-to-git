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
from google.appengine.api import taskqueue
import model

ken_page = model.SourceContent.get_or_insert("ken_all_page")
if not (ken_page is None):
    ken_page.checked = None
    ken_page.put()
jig_page = model.SourceContent.get_or_insert("jigyosyo_page")
if not (jig_page is None):
    jig_page.checked = None
    jig_page.put()

print 'Content-Type: text/plain'
print ''
print 'exit'
taskqueue.add(url='/fetch')
