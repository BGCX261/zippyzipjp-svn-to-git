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
package jp.zippyzip.web;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.PersistenceManager;

import jp.zippyzip.Controller;
import jp.zippyzip.impl.DistributorServiceImpl;
import jp.zippyzip.impl.GeneratorServiceImpl;
import jp.zippyzip.impl.LzhServiceImpl;
import jp.zippyzip.jdo.JdoLzhDao;
import jp.zippyzip.jdo.JdoParentChildDao;
import jp.zippyzip.jdo.JdoRawDao;

public class ApplicationContext {
    
    /**  */
    public static ApplicationContext getContext() {
        return instance;
    }
    
    /**  */
    private static ApplicationContext instance = new ApplicationContext();
    
    protected Map<Long, PersistenceManager> pmPool =
        Collections.synchronizedMap(new HashMap<Long, PersistenceManager>());

    /**  */
    private boolean debug = false;
    
    /**  */
    private JdoLzhDao lzhDao = new JdoLzhDao();
    
    /**  */
    private JdoRawDao rawDao = new JdoRawDao();
    
    /**  */
    private JdoParentChildDao parentChildDao = new JdoParentChildDao();
    
    /**  */
    private LzhServiceImpl lzhService = new LzhServiceImpl();
    {
        lzhService.setLzhDao(lzhDao);
        lzhService.setRawDao(rawDao);
    }
    
    /**  */
    private GeneratorServiceImpl generatorService = new GeneratorServiceImpl();
    {
        generatorService.setLzhDao(lzhDao);
        generatorService.setRawDao(rawDao);
        generatorService.setParentChildDao(parentChildDao);
        generatorService.setStreetNumSplitMax(30);
    }
    
    /**  */
    private DistributorServiceImpl distributorService = new DistributorServiceImpl();
    {
        distributorService.setLzhDao(lzhDao);
        distributorService.setRawDao(rawDao);
    }
    
    /**  */
    private DefaultController defaultController = new DefaultController();
    {
        defaultController.setLzhService(lzhService);
    }
    
    /**  */
    private ContentController contentController = new ContentController();
    {
        contentController.setLzhService(lzhService);
    }
    
    /**  */
    private ArchController archController = new ArchController();
    {
        archController.setLzhService(lzhService);
        archController.setGeneratorService(generatorService);
    }
    
    /**  */
    private JobController jobController = new JobController();
    {
        jobController.setLzhService(lzhService);
        jobController.setGeneratorService(generatorService);
    }
    
    /**  */
    private DownloadController downloadController = new DownloadController();
    {
        downloadController.setDistributorService(distributorService);
    }

    /**
     * @return the defaultController
     */
    public Controller getDefaultController() {
        return defaultController;
    }

    /**
     * @return the contentController
     */
    public ContentController getContentController() {
        return contentController;
    }

    /**
     * @return the archController
     */
    public Controller getArchController() {
        return archController;
    }

    /**
     * @return the jobController
     */
    public Controller getJobController() {
        return jobController;
    }

    /**
     * @return the downloadController
     */
    public DownloadController getDownloadController() {
        return downloadController;
    }

    /**
     * @return the distributorService
     */
    public DistributorServiceImpl getDistributorService() {
        return distributorService;
    }
    
    /**
     * PersistenceManager を取得する。
     * 
     * @return PersistenceManager
     */
    public PersistenceManager getPm() {
        
        Long id = Thread.currentThread().getId();
        
        if (!pmPool.containsKey(id)) {
            pmPool.put(id, PMF.get().getPersistenceManager());
        }
        
        return pmPool.get(id);
    }
    
    /**
     * PersistenceManager を閉じる。
     */
    public void closePm() {
        
        Long id = Thread.currentThread().getId();
        
        if (pmPool.containsKey(id)) {
            pmPool.get(id).close();
            pmPool.remove(id);
        }
    }

    /**
     * @return the debug
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * @param debug the debug to set
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
