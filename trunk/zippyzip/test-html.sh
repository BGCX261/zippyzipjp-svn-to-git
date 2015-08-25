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
#
PHP_COMMAND=/usr/local/zend/bin/php
SCRIPT_HTML=/usr/local/zippyzip/zippyzip-html.php
LOG_FILE=/var/log/zippyzip.log
#
$PHP_COMMAND $SCRIPT_HTML
RETVAL=$?
if [ $RETVAL != 0 ]; then
	echo `date` FATAL Exit code: $RETVAL >> $LOG_FILE
	exit $?
fi
exit 0
