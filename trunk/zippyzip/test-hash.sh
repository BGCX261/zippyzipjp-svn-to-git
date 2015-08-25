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
# <PATH_DATA>/archive に出力する gzip ファイルとそのハッシュのチェックです。
# Mac OS X でテストしましたが、少し変更すれば Linux 等でも使えると思います。
#
for GZ in `ls data/archive/*.gz` ; do
	HASH=`md5 $GZ |sed s/.*\ //g`
	TEST=`cat $GZ.md5.txt`
	if [ $HASH != $TEST ] ; then
		echo NG: $GZ.md5.txt
		exit 1
	fi
	HASH=`shasum $GZ |sed s/\ .*//g`
	TEST=`cat $GZ.sha1.txt`
	if [ $HASH != $TEST ] ; then
		echo NG: $GZ.sha1.txt
		exit 1
	fi
	gzip -t $GZ
	if [ "0" != "$?" ] ; then
		echo NG: $GZ
		exit 1
	fi
done
echo OK.
exit 0
