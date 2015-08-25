#!/usr/bin/perl
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

use File::Path qw(make_path remove_tree);
use File::Fetch;
use Archive::Extract;
use strict;

my $base = $0;
$base =~ s/\/[^\/]*$//;

require "$base/getzippyzipjp.conf";

print localtime() . " start.\n";

# JSON細切れデータの圧縮ファイルを保存するディレクトリを確認する。
make_path(arcdir()) unless (-d arcdir());

unless (-d arcdir()) {
    print 'Error: Faild to mkdir ' . arcdir() . "\n";
    exit 1;
}

# JSON細切れデータを保存するディレクトリを確認する。
make_path(taregt()) unless (-d taregt());

unless (-d taregt()) {
    print 'Error: Faild to mkdir ' . taregt() . "\n";
    exit 1;
}

# 更新情報を取得する。
chdir arcdir();
my $ff = File::Fetch->new(uri => check());
my $where = $ff->fetch() or die $ff->error;
my $where = $ff->fetch;

my $feed = check();
$feed =~ s/.*\///;
$feed = arcdir() . "/$feed";

unless (-f $feed) {
    print 'Error: Faild to fetch ' . check() . "\n";
    exit 1;
}

# 更新されていなければ終了。
my $prev = arcdir() . '/feed.prev';

open(my $hf, '<', $feed) or die $!;
my @feed_text = <$hf>;
close($hf);

if (-f $prev) {
    
    open(my $hp, '<', $prev) or die $!;
    my @prev_text = <$hp>;
    close($hp);
    
    if (join(@feed_text) eq join(@prev_text)) {
        print "exit.\n";
        exit 0;
    }
}

# 更新情報を保存する。
open(my $hp, '>', $prev) or die $!;
print $hp @feed_text;
close($hp);

# 圧縮ファイルを取得して解凍する。
my @urls = grep(/"http:\/\/.*\/json.*\.zip"/, @feed_text);

foreach (@urls) {
    
    s/.*"http:/http:/;
    s/\.zip".*/\.zip/;
    chomp;

    chdir arcdir();
    my $ff = File::Fetch->new(uri => $_);
    my $where = $ff->fetch() or die $ff->error;
    my $where = $ff->fetch( to => arcdir() );
    
    s/.*\///;
    
    my $path =  arcdir() . '/' . $_;
    
    if (-f $path) {
    
        print localtime() . " " . $_ . " OK. " . (stat($path))[7] .  " byte\n";

        chdir taregt();
        my $ae = Archive::Extract->new( archive => $path );
        my $ok = $ae->extract;
        
    } else {
        print "Error: Failed to fetch archive: " . $_ . "\n";
    }
}

print localtime() . " complete.\n";

exit 0;
