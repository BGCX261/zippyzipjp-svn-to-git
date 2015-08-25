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

import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import jp.zippyzip.ParentChild;
import jp.zippyzip.ParentChildDao;
import jp.zippyzip.web.ApplicationContext;

/**
 * 親子関係を保持するエンティティのデータアクセスオブジェクトの JDO 実装。
 * 
 * @author Michinobu Maeda
 */
public class JdoParentChildDao implements ParentChildDao {

    /** ログ　*/
    protected Logger log = Logger.getLogger(this.getClass().getName());
    
    /* (non-Javadoc)
     * @see jp.zippyzip.ParentChildDao#store(jp.zippyzip.ParentChild)
     */
    public void store(ParentChild pc) {
        
        PersistenceManager pm = ApplicationContext.getContext().getPm();
        
        pm.makePersistent(pc);
    }
    
    /* (non-Javadoc)
     * @see jp.zippyzip.ParentChildDao#getAll(java.lang.String)
     */
    public ParentChild get(String key) {
        
        PersistenceManager pm = ApplicationContext.getContext().getPm();
        ParentChild ret = null;
        
        try {
            ret = pm.getObjectById(ParentChild.class, key);
        } catch (JDOObjectNotFoundException e) { }
        
        return ret;
    }
    
    /* (non-Javadoc)
     * @see jp.zippyzip.ParentChildDao#addChild(java.lang.String, java.lang.String)
     */
    public void addChild(String key, String json) {
        
        PersistenceManager pm = ApplicationContext.getContext().getPm();
        
        try {
            
            ParentChild pc = pm.getObjectById(ParentChild.class, key);
            
            pc.getChildren().add(json);
            pm.makePersistent(pc);
            
        } catch (JDOObjectNotFoundException e) { }
    }
    
    /* (non-Javadoc)
     * @see jp.zippyzip.ParentChildDao#addChild(java.lang.String, java.util.LinkedList)
     */
    public void addChild(String key, LinkedList<String> jsons) {
        
        PersistenceManager pm = ApplicationContext.getContext().getPm();
        
        try {
            
            ParentChild pc = pm.getObjectById(ParentChild.class, key);
            
            pc.getChildren().addAll(jsons);
            pm.makePersistent(pc);
            
        } catch (JDOObjectNotFoundException e) { }
    }
    
    /* (non-Javadoc)
     * @see jp.zippyzip.ParentChildDao#getKeys()
     */
    public Collection<String> getKeys() {
        
        PersistenceManager pm = ApplicationContext.getContext().getPm();
        Query q = pm.newQuery("select key from " + ParentChild.class.getName());
        @SuppressWarnings("unchecked")
        Collection<String> ret = (Collection<String>) q.execute();
        
        return ret;
    }
}
