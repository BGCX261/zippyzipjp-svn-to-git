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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jp.zippyzip.ContentStatus;
import jp.zippyzip.Lzh;
import jp.zippyzip.LzhDao;
import jp.zippyzip.LzhService;
import jp.zippyzip.RawDao;
import jp.zippyzip.web.ApplicationContext;

/**
 * LZHサービスの実装。
 * 
 * @author Michinobu Maeda
 */
public class LzhServiceImpl implements LzhService {

    /** ログ　*/
    protected Logger log = Logger.getLogger(this.getClass().getName());
    
    /** LZH のデータアクセスオブジェクト */
    private LzhDao lzhDao;
    
    /** バイナリデータのデータアクセスオブジェクト */
    private RawDao rawDao;

    /**
     * LZH のデータアクセスオブジェクトを取得する。
     * 
     * @return LZH のデータアクセスオブジェクト
     */
    public LzhDao getLzhDao() {
        return lzhDao;
    }

    /**
     * LZH のデータアクセスオブジェクトを設定する。
     * 
     * @param lzhDao LZH のデータアクセスオブジェクト
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
     * @see jp.zippyzip.LzhService#initAll()
     */
    public void initAll() {
        
        getLzhDao().initAll();
        initStatus();
    }
    
    /* (non-Javadoc)
     * @see jp.zippyzip.LzhService#initStatus()
     */
    public void initStatus() {
        
        getLzhDao().initContentStatus();
        
        getLzhDao().store(new ContentStatus("j", "",
                "https://www.post.japanpost.jp/zipcode/dl/jigyosyo/index.html",
                new Date(0), new Date(0), false, true));
        getLzhDao().store(new ContentStatus("j01", "j",
                "https://www.post.japanpost.jp/zipcode/dl/jigyosyo/lzh/jigyosyo.lzh",
                new Date(0), new Date(0), true, true));
        getLzhDao().store(new ContentStatus("k", "",
                "https://www.post.japanpost.jp/zipcode/dl/kogaki.html",
                new Date(0), new Date(0), false, false));
        
        if (ApplicationContext.getContext().isDebug()) {
            
            getLzhDao().store(new ContentStatus("k01", "k",
                    "https://www.post.japanpost.jp/zipcode/dl/kogaki/lzh/01hokkai.lzh",
                    new Date(0), new Date(0), true, false));
            getLzhDao().store(new ContentStatus("k13", "k",
                    "https://www.post.japanpost.jp/zipcode/dl/kogaki/lzh/13tokyo.lzh",
                    new Date(0), new Date(0), true, false));
            getLzhDao().store(new ContentStatus("k47", "k",
                    "https://www.post.japanpost.jp/zipcode/dl/kogaki/lzh/47okinaw.lzh",
                    new Date(0), new Date(0), true, false));
            
        } else {
        
            getLzhDao().store(new ContentStatus("k01", "k",
                    "https://www.post.japanpost.jp/zipcode/dl/kogaki/lzh/ken_all.lzh",
                    new Date(0), new Date(0), true, false));
        }

    }
    
    /* (non-Javadoc)
     * @see jp.zippyzip.LzhService#getAll()
     */
    public List<ContentStatus> getAll() {
        return getLzhDao().getAll();
    }
    
    /* (non-Javadoc)
     * @see jp.zippyzip.LzhService#update(jp.zippyzip.ContentStatus)
     */
    public void update(ContentStatus stt) {
        
        if ((stt == null) || (stt.getId() == null)
                || (stt.getId().length() == 0)) {
            return;
        }

        getLzhDao().store(stt);
    }
    
    /* (non-Javadoc)
     * @see jp.zippyzip.LzhService#refresh()
     */
    public boolean checkDepends() {
        return getLzhDao().checkDepends();
    }
 
    /* (non-Javadoc)
     * @see jp.zippyzip.LzhService#checkContent()
     */
    public boolean checkContents() {
        return getLzhDao().checkContents();
    }
    
    /* (non-Javadoc)
     * @see jp.zippyzip.LzhService#isStatusUpdated()
     */
    public boolean isStatusUpdated() {
        
        for (ContentStatus stt : getLzhDao().getAll()) {
            
            if ((stt.getDepends() == null)
                    || (stt.getDepends().length() == 0)) { continue; }
            
            if (stt.getLastUpdate().getTime()
                    < stt.getLastCheck().getTime()) { continue; } 
            
            return true;
        }
        
        return false;
    }

    /* (non-Javadoc)
     * @see jp.zippyzip.impl.LzhService#getInfoAll()
     */
    public Collection<Lzh> getInfoAll() {
        return getLzhDao().getInfoAll();
    }

    /* (non-Javadoc)
     * @see jp.zippyzip.impl.LzhService#store(byte[], int, int)
     */
    public boolean store(byte[] buff, int offset, int size) {
        
        Lzh lzh = getLzhDao().store(buff, offset, size, true);
        getRawDao().store(buff, offset, size, lzh.getKey());
        return true;
    }

    /* (non-Javadoc)
     * @see jp.zippyzip.impl.LzhService#delete(java.lang.String)
     */
    public void delete(String key) {
        getLzhDao().remove(key);
        getRawDao().remove(key);
    }
    
    /* (non-Javadoc)
     * @see jp.zippyzip.impl.LzhService#fetch()
     */
    public boolean fetch() {
        
        boolean ret = false;
        Collection<ContentStatus> stts = getLzhDao().getAll();
        Collection<Lzh> lzhs = getLzhDao().getInfoAll();
        
        try {
            
            for (ContentStatus stt : stts) {
                
                if ((stt.getDepends() == null)
                        || (stt.getDepends().length() == 0)) { continue; }

                boolean check = false;

                for (Lzh lzh : lzhs) {
                
                    if (lzh.isUploaded()) { continue; }
                    
                    if (!stt.getUrl().endsWith(lzh.getFilename())) { continue; }
                    if (lzh.getStored().getTime()
                            > stt.getLastUpdate().getTime()) {
                        
                        check = true;
                        break;
                    }
                }
                
                if (check) { continue; }
                    
                byte[] buff = getLzhDao().download(new URL(stt.getUrl()));
                Lzh stored = getLzhDao().store(buff, 0, buff.length, false);
                
                getRawDao().store(buff, 0, buff.length, stored.getKey());
                ret = true;
                break;
            }
         
        } catch (MalformedURLException e) {
            log.log(Level.WARNING, "", e);
        }
        
        return ret;
    }
}
