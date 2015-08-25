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
package jp.zippyzip.jdo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.Extent;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import jp.gr.java_conf.dangan.util.lha.LhaHeader;
import jp.gr.java_conf.dangan.util.lha.LhaInputStream;
import jp.zippyzip.ContentStatus;
import jp.zippyzip.Lzh;
import jp.zippyzip.LzhDao;
import jp.zippyzip.ZipInfo;
import jp.zippyzip.web.ApplicationContext;

import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

/**
 * LZH のデータアクセスオブジェクトの JDO 実装。
 * 
 * @author Michinobu Maeda
 */
public class JdoLzhDao implements LzhDao {
    
    /** ダイジェストハッシュのメソッド　*/
    private final String DIGEST_METHOD = "MD5";

    /** ダイジェストハッシュを文字列に変換するための 16進数の値 */
    static final byte[] HEX_CHARS = {
        (byte)'0', (byte)'1', (byte)'2', (byte)'3',
        (byte)'4', (byte)'5', (byte)'6', (byte)'7',
        (byte)'8', (byte)'9', (byte)'a', (byte)'b',
        (byte)'c', (byte)'d', (byte)'e', (byte)'f'
    };


    /** ログ　*/
    protected Logger log = Logger.getLogger(this.getClass().getName());

    /* (non-Javadoc)
     * @see jp.zippyzip.LzhDao#initAll()
     */
    public void initAll() {
        
        PersistenceManager pm = ApplicationContext.getContext().getPm();
        Query query;
        
        query = pm.newQuery("SELECT from jp.zippyzip.Base64Encoded");
        query.deletePersistentAll();
        query = pm.newQuery("SELECT from jp.zippyzip.Lzh");
        query.deletePersistentAll();
        query = pm.newQuery("SELECT from jp.zippyzip.ContentStatus");
        query.deletePersistentAll();
        query = pm.newQuery("SELECT from jp.zippyzip.ZipInfo");
        query.deletePersistentAll();
        query = pm.newQuery("SELECT from jp.zippyzip.ParentChild");
        query.deletePersistentAll();
    }

    /* (non-Javadoc)
     * @see jp.zippyzip.LzhDao#initContentStatus()
     */
    public void initContentStatus() {
        
        PersistenceManager pm = ApplicationContext.getContext().getPm();
        Query query;
        
        query = pm.newQuery("SELECT from jp.zippyzip.ContentStatus");
        query.deletePersistentAll();
    }
    
    /* (non-Javadoc)
     * @see jp.zippyzip.ContentStatusDao#store(jp.zippyzip.ContentStatus)
     */
    public void store(ContentStatus stt) {

        PersistenceManager pm = ApplicationContext.getContext().getPm();

        pm.makePersistent(stt);
            
        if (stt.getUrl() == null) {
            pm.deletePersistent(stt);
        }
    }

    /* (non-Javadoc)
     * @see jp.zippyzip.ContentStatusDao#getAll()
     */
    public List<ContentStatus> getAll() {
        
        Map<String, ContentStatus> stts = new TreeMap<String, ContentStatus>();
        List<ContentStatus> ret = new LinkedList<ContentStatus>();

        PersistenceManager pm = ApplicationContext.getContext().getPm();
        Extent<ContentStatus> extent = pm.getExtent(ContentStatus.class);
        
        for (Object obj : extent) {
            ContentStatus stt = (ContentStatus)obj;
            stts.put(stt.getId(), stt);
        }
                
        for (String key : stts.keySet()) {
            ret.add(stts.get(key));
        }

        return ret;
    }

    /* (non-Javadoc)
     * @see jp.zippyzip.ContentStatusDao#checkDepends()
     */
    public boolean checkDepends() {
     
        List<ContentStatus> stts = getAll();
        boolean ret = false;
        
        for (ContentStatus stt : stts) {
            
            if ((stt.getDepends() != null)
                    && (stt.getDepends().length() > 0)) { continue; }
            
            if (refreshContentStatus(stt)) { ret = true; }
        }
        
        return ret;
    }
    
    /* (non-Javadoc)
     * @see jp.zippyzip.LzhDao#checkContent()
     */
    public boolean checkContents() {
        
        List<ContentStatus> stts = getAll();
        HashMap<String, ContentStatus> depends = new HashMap<String, ContentStatus>();
        boolean ret = false;
        
        for (ContentStatus stt : stts) {
            
            if ((stt.getDepends() != null)
                    && (stt.getDepends().length() > 0)) { continue; }
            
            depends.put(stt.getId(), stt);
        }
        
        for (ContentStatus stt : stts) {
            
            if ((stt.getDepends() == null)
                    || (stt.getDepends().length() == 0)) { continue; }
            
            ContentStatus cmp = depends.get(stt.getDepends());
            
            if (cmp == null) { continue; }
            if (stt.getLastCheck().getTime() >= cmp.getLastCheck().getTime()) { continue; } 
            
            refreshContentStatus(stt);
            ret = true;
            break;
        }
        
        return ret;
    }

    /**
     * 更新情報を最新にする。
     * 
     * @return 更新の有無
     */
    boolean refreshContentStatus(ContentStatus stt) {
        
        String hash = null;
        
        try {
            hash = getHash(new URL(stt.getUrl()));
        } catch (MalformedURLException e) {
            log.log(Level.WARNING, "", e);
        }
        
        if (hash == null) { return false; }
        
        stt.setLastCheck(new Date());
        
        if (hash.equals(stt.getHash())) { return false; }
            
        stt.setHash(hash);
        stt.setLastUpdate(new Date());
        return true;
    }
    
    /**
     * Webコンテンツのダイジェストハッシュを取得する。
     * 
     * @param url Webコンテンツ
     * @return ダイジェストハッシュ
     */
    String getHash(URL url) {
        
        String ret = null;
        
        try {
            
            InputStream is = url.openStream();
            byte[] buff = new byte[1024];
            int len = is.read(buff);
            MessageDigest digest = MessageDigest.getInstance(DIGEST_METHOD);
            
            while (0 < len) {
                digest.update(buff, 0, len);
                len = is.read(buff);
            }

            byte [] ba = digest.digest();
            byte [] hex = new byte[2 * ba.length];
            int i = 0;

            for (byte b : ba) {
              int v = b & 0xFF;
              hex[i++] = HEX_CHARS[v >>> 4];
              hex[i++] = HEX_CHARS[v & 0xF];
            }
            
            ret = new String(hex, "ASCII");
            
        } catch (NoSuchAlgorithmException e) {
            log.log(Level.WARNING, "", e);
        } catch (IOException e) {
            log.log(Level.WARNING, "", e);
        }
        
        return ret;
    }
    
    /* (non-Javadoc)
     * @see jp.zippyzip.LzhDao#store(byte[], int, int, boolean)
     */
    public Lzh store(byte[] buff, int offset, int size, boolean uploaded) {

        PersistenceManager pm = ApplicationContext.getContext().getPm();
        Lzh lzh = null;
        
        try {
            
            LhaInputStream lha = new LhaInputStream(
                    new ByteArrayInputStream(buff, offset, size));
            LhaHeader header = lha.getNextEntryWithoutExtract();
            lzh = new Lzh(header.getLastModified(), header.getPath(
                    ).toLowerCase().replaceFirst("(\\.csv|\\.CSV)", ".lzh"), uploaded);
            lha.close();
            pm.makePersistent(lzh);
        
        } catch (IOException e) {
            log.log(Level.WARNING, "", e);
        }

        return lzh;
    }

    /* (non-Javadoc)
     * @see jp.zippyzip.LzhDao#download(java.net.URL)
     */
    public byte[] download(URL url) {
        
        byte[] ret = null;
        
        try {
            ret = URLFetchServiceFactory.getURLFetchService(
                    ).fetch(url).getContent();
        } catch (IOException e) {
            log.log(Level.WARNING, "", e);
        }
        
        return ret;
    }
    
    /* (non-Javadoc)
     * @see jp.zippyzip.LzhDao#getInfoAll()
     */
    public Collection<Lzh> getInfoAll() {
        
        PersistenceManager pm = ApplicationContext.getContext().getPm();
        Collection<Lzh> ret = new LinkedList<Lzh>();
        
        for (Lzh e : pm.getExtent(Lzh.class)) { ret.add(e); }
        
        return ret;
    }
    
    /* (non-Javadoc)
     * @see jp.zippyzip.LzhDao#remove(java.lang.String)
     */
    public void remove(String key) {
        
        PersistenceManager pm = ApplicationContext.getContext().getPm();

        try {
            
            Lzh lzh = pm.getObjectById(Lzh.class, key);
            pm.deletePersistent(lzh);

        } catch (JDOObjectNotFoundException e) {
        }
        
    }
    
    /* (non-Javadoc)
     * @see jp.zippyzip.LzhDao#getLatest(boolean, java.util.Date)
     */
    public LinkedList<Lzh> getLatest(boolean corp, Date checkPoint) {
        LinkedList<Lzh> ret = new LinkedList<Lzh>();
        TreeMap<String, Lzh> map = new TreeMap<String, Lzh>();
        Collection<Lzh> lzhs = getInfoAll();
        
        for (Lzh lzh : lzhs) {
            
            if (lzh.getTimestamp().getTime() > checkPoint.getTime()) {
                continue;
            }
            
            String filename = lzh.getFilename().toLowerCase();
            
            if (map.containsKey(filename)) {
                
                if (map.get(filename).getTimestamp().getTime()
                        > lzh.getTimestamp().getTime()) {
                    
                    continue;
                } else {                    
                    map.remove(filename);
                }
            }
            
            map.put(filename, lzh);
        }
        
        if (corp) {
            
            for (String filename : map.keySet()) {
                
                if (!filename.startsWith("j")) { continue; }
                ret.add(map.get(filename));
            }
        
        } else {
            
            long tsKen = 0;
            long tsPrefs = 0;
            int prefCount = 0;
            
            for (String filename : map.keySet()) {
                
                if (filename.startsWith("j")) { continue; }
                
                if (filename.startsWith("k")) {
                    
                    if (tsKen < map.get(filename).getTimestamp().getTime()) {
                        tsKen = map.get(filename).getTimestamp().getTime();
                    }
                    
                } else {
                    
                    if (tsPrefs < map.get(filename).getTimestamp().getTime()) {
                        tsPrefs = map.get(filename).getTimestamp().getTime();
                    }
                    
                    ++prefCount;
                }
            }
            
            if (prefCount < 47) { tsPrefs = 0; }
            
            if (tsKen > tsPrefs) {
                
                for (String filename : map.keySet()) {
                    
                    if (filename.startsWith("j")) { continue; }
                    if (!filename.startsWith("k")) { continue; }
                    ret.add(map.get(filename));
                }
                
            } else {
                
                for (String filename : map.keySet()) {
                    
                    if (filename.startsWith("j")) { continue; }
                    if (filename.startsWith("k")) { continue; }
                    ret.add(map.get(filename));
                }
                
            }
        }
        
        return ret;
    }
    
    /* (non-Javadoc)
     * @see jp.zippyzip.LzhDao#updateArea(java.util.LinkedList, java.util.Date)
     */
    public ZipInfo updateArea(LinkedList<Lzh> lzhs, Date generated) {

        PersistenceManager pm = ApplicationContext.getContext().getPm();
        ZipInfo zipInfo = null;
            
        for (ZipInfo e : pm.getExtent(ZipInfo.class)) {
            zipInfo =  e;
            break;
        }
        
        if (zipInfo == null) {
            zipInfo = new ZipInfo();
            zipInfo.setKey("1");
            zipInfo.setGenerated(generated);
            zipInfo.setTimestamp(new Date(0));
            pm.makePersistent(zipInfo);
        }
        
        return zipInfo;
    }
    
    /* (non-Javadoc)
     * @see jp.zippyzip.LzhDao#updateCorp(java.util.LinkedList, java.util.Date)
     */
    public ZipInfo updateCorp(LinkedList<Lzh> lzhs, Date generated) {

        PersistenceManager pm = ApplicationContext.getContext().getPm();
        ZipInfo zipInfo = null;
            
        for (ZipInfo e : pm.getExtent(ZipInfo.class)) {
            zipInfo =  e;
            break;
        }
        
        if (zipInfo == null) {
            zipInfo = new ZipInfo();
            zipInfo.setKey("1");
            zipInfo.setGenerated(generated);
            zipInfo.setTimestamp(new Date(0));
            pm.makePersistent(zipInfo);
        }
    
        return zipInfo;
    }

    /* (non-Javadoc)
     * @see jp.zippyzip.LzhDao#getZipInfo()
     */
    public ZipInfo getZipInfo() {

        PersistenceManager pm = ApplicationContext.getContext().getPm();
        ZipInfo ret = null;
        
        for (ZipInfo zipInfo : pm.getExtent(ZipInfo.class)) {
            ret = zipInfo;
        }
            
        return ret;
    }
    
    /* (non-Javadoc)
     * @see jp.zippyzip.LzhDao#store(jp.zippyzip.ZipInfo)
     */
    public void store(ZipInfo zipInfo) {
        ApplicationContext.getContext().getPm().makePersistent(zipInfo);
    }
}
