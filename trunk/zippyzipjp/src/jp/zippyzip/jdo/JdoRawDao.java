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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import jp.zippyzip.Base64Encoded;
import jp.zippyzip.RawDao;
import jp.zippyzip.web.ApplicationContext;
import net.iharder.base64.Base64;

/**
 * バイナリデータのデータアクセスオブジェクトの定義。
 * 
 * @author Michinobu Maeda
 */
public class JdoRawDao implements RawDao {

    /** ログ　*/
    protected Logger log = Logger.getLogger(this.getClass().getName());
    
    public static final int SEQ_MAX =
        Integer.MAX_VALUE / Base64Encoded.BLOCK_MAX_LINE / Base64Encoded.LINE_MAX_LEN;

    /* (non-Javadoc)
     * @see jp.zippyzip.RawDao#store(byte[], java.lang.String)
     */
    public void store(byte[] raw, String filename) {
        store(raw, 0, raw.length, filename);
    }
    
    /* (non-Javadoc)
     * @see jp.zippyzip.RawDao#store(byte[], int, int, java.lang.String)
     */
    public void store(byte[] raw, int offset, int size, String filename) {
        
        PersistenceManager pm = ApplicationContext.getContext().getPm();
        int seq = 0;
        Base64Encoded b64 = new Base64Encoded(filename, seq);
        int cnt = 0;
        int end = size + offset;
            
        pm.makePersistent(b64);
        
        while (0 < (end - offset)) {
            
            int len = (Base64Encoded.LINE_MAX_LEN < (end - offset)) ?
                    Base64Encoded.LINE_MAX_LEN : (end - offset);
            
            b64.addBase64Encoded(Base64.encodeBytes(raw, offset, len));
            ++cnt;
            offset += len;
            
            if (cnt >= Base64Encoded.BLOCK_MAX_LINE) {
                cnt = 0;
                ++seq;
                b64 = new Base64Encoded(filename, seq);
                pm.makePersistent(b64);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see jp.zippyzip.RawDao#get(java.lang.String)
     */
    public byte[] get(String filename) {
        
        PersistenceManager pm = ApplicationContext.getContext().getPm();
        int seq = 0;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try {
            
            while (seq < SEQ_MAX) {
                
                for (String line : pm.getObjectById(Base64Encoded.class,
                        Base64Encoded.getKey(filename, seq)).getBae64Encoded()) {
                    baos.write(Base64.decode(line.getBytes("ASCII")));
                }

                ++seq;
            }
            
            baos.flush();
            
        } catch (JDOObjectNotFoundException e) {
        } catch (IOException e) {
            log.log(Level.WARNING, "", e);
        }
        
        return baos.toByteArray();
    }

    /* (non-Javadoc)
     * @see jp.zippyzip.RawDao#remove(java.lang.String)
     */
    public void remove(String filename) {
        
        PersistenceManager pm = ApplicationContext.getContext().getPm();
        int seq = 0;
        
        try {
            
            while (seq < SEQ_MAX) {
                
                Base64Encoded e = pm.getObjectById(Base64Encoded.class,
                        Base64Encoded.getKey(filename, seq));
                pm.deletePersistent(e);

                ++seq;
            }
            
        } catch (JDOObjectNotFoundException e) {
        }
    }
}
