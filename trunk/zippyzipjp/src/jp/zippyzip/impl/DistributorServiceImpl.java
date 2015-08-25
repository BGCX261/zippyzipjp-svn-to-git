/*
 * zippyzipjp
 * 
 * Copyright 2008-2010 Michinobu Maeda.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.zippyzip.impl;

import java.util.logging.Logger;

import jp.zippyzip.DistributorService;
import jp.zippyzip.LzhDao;
import jp.zippyzip.RawDao;
import jp.zippyzip.ZipInfo;

/**
 * 配布サービスの実装。
 * 
 * @author Michinobu Maeda
 */
public class DistributorServiceImpl implements DistributorService {

    /** ログ　*/
    protected Logger log = Logger.getLogger(this.getClass().getName());
    
    /** 郵便番号データのデータアクセスオブジェクト */
    private LzhDao lzhDao;
    
    /** バイナリデータのデータアクセスオブジェクト */
    private RawDao rawDao;

    /**
     * 郵便番号データのデータアクセスオブジェクトを取得する。
     * 
     * @return 郵便番号データのデータアクセスオブジェクト
     */
    public LzhDao getLzhDao() {
        return lzhDao;
    }

    /**
     * 郵便番号データのデータアクセスオブジェクトを設定する。
     * 
     * @param zipInfoDao 郵便番号データのデータアクセスオブジェクト
     */
    public void setLzhDao(LzhDao lzhDao) {
        this.lzhDao = lzhDao;
    }

    /**
     * バイナリデータのデータアクセスオブジェクトを取得する。
     * 
     * @return バイナリデータのデータアクセスオブジェクト
     */
    public RawDao getRawDao() {
        return rawDao;
    }

    /**
     * バイナリデータのデータアクセスオブジェクトを設定する。
     * 
     * @param rawDao バイナリデータのデータアクセスオブジェクト
     */
    public void setRawDao(RawDao rawDao) {
        this.rawDao = rawDao;
    }
    
    /* (non-Javadoc)
     * @see jp.zippyzip.impl.DistributorService#getZipInfo()
     */
    public ZipInfo getZipInfo() {
        
        if (getLzhDao().getZipInfo() == null) {
            return null;
        }
        
        return getLzhDao().getZipInfo();
    }
    
    /* (non-Javadoc)
     * @see jp.zippyzip.DistributorService#getRawData(java.lang.String)
     */
    public byte[] getRawData(String filename) {
        return rawDao.get(filename);
    }
}
