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

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.zippyzip.Controller;
import jp.zippyzip.GeneratorService;
import jp.zippyzip.LzhService;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;

/**
 * Jobを HTTP 経由で受け付けるコントローラ。
 * 
 * @author Michinobu Maeda
 */
public class JobController implements Controller {

    /** ログ　*/
    protected Logger log = Logger.getLogger(this.getClass().getName());
    
    /** LZH 管理サービス */
    private LzhService lzhService;

    /** 生成物サービス */
    private GeneratorService generatorService;

    /**
     * LZH 管理サービスを取得する。
     * 
     * @return LZH 管理サービス
     */
    public LzhService getLzhService() {
        return lzhService;
    }

    /**
     * LZH 管理サービスを設定する。
     * 
     * @param lzhService LZH 管理サービス
     */
    public void setLzhService(LzhService lzhService) {
        this.lzhService = lzhService;
    }

    /**
     * 生成物サービスを取得する。
     * 
     * @return 生成物サービス
     */
    public GeneratorService getGeneratorService() {
        return generatorService;
    }

    /**
     * 生成物サービスを設定する。
     * @param generatorService 生成物サービス
     */
    public void setGeneratorService(GeneratorService generatorService) {
        this.generatorService = generatorService;
    }
    
    /* (non-Javadoc)
     * @see jp.zippyzip.Controller#defaultHandler(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.String, boolean)
     */
    public String defaultHandler(HttpServletRequest request,
            HttpServletResponse response, String path, boolean isAdmin)
    throws IOException {
        
        String ret = null;
        
        log.setLevel(Level.INFO);
        log.info(path);
        
        if (path.equals("/startUpdate")) {
            
            getLzhService().getAll();            
            getQueue().add(withUrl("/zippyzipjp/job/checkDepends"));
            
        } else if (path.equals("/checkDepends")) {
            
            if (getLzhService().checkDepends()) {
                getQueue().add(withUrl("/zippyzipjp/job/checkDepends"));
            } else {
                getQueue().add(withUrl("/zippyzipjp/job/checkContent"));
            }
            
        } else if (path.equals("/checkContent")) {
            
            if (getLzhService().checkContents()) {
                getQueue().add(withUrl("/zippyzipjp/job/checkContent"));                
            } else if (getLzhService().isStatusUpdated()) {
                getQueue().add(withUrl("/zippyzipjp/job/fetch"));
            }
            
        } else if (path.equals("/fetch")) {
            
            if (getLzhService().fetch()) {
                getQueue().add(withUrl("/zippyzipjp/job/fetch"));                
            } else {
                getQueue().add(withUrl("/zippyzipjp/job/checkUpdate"));
            }
            
        } else if (path.equals("/checkUpdate")) {
            
            if (generatorService.check()) {
                getQueue().add(withUrl("/zippyzipjp/job/updateArea"));                
            }
            
        } else if (path.equals("/reset")) {
            
            getQueue().add(withUrl("/zippyzipjp/job/updateArea"));
            
        } else if (path.equals("/updateArea")) {
            
            generatorService.updateArea();
            getQueue().add(withUrl("/zippyzipjp/job/updateCorp"));
            
        } else if (path.equals("/updateCorp")) {
            
            generatorService.updateCorp();
            getQueue().add(withUrl("/zippyzipjp/job/preZips"));
            
        } else if (path.equals("/preZips")) {
            
            generatorService.preZips();
            getQueue().add(withUrl("/zippyzipjp/job/updateZips"));
            
        } else if (path.equals("/updateZips")) {
            
            generatorService.updateZips();
            getQueue().add(withUrl("/zippyzipjp/job/storeX0401Zip"));
            
        } else if (path.equals("/storeX0401Zip")) {
            
            generatorService.storeX0401Zip();
            getQueue().add(withUrl("/zippyzipjp/job/storeX0402Zip"));                
            
        } else if (path.equals("/storeX0402Zip")) {
            
            generatorService.storeX0402Zip();
            getQueue().add(withUrl("/zippyzipjp/job/storeAreaText"));                
            
        } else if (path.equals("/storeAreaText")) {
            
            generatorService.storeAreaText();
            getQueue().add(withUrl("/zippyzipjp/job/storeAreaCsv"));
            
        } else if (path.equals("/storeAreaCsv")) {
            
            generatorService.storeAreaCsv();
            getQueue().add(withUrl("/zippyzipjp/job/storeAreaJson"));                
            
        } else if (path.equals("/storeAreaJson")) {
            
            generatorService.storeAreaJson();
            getQueue().add(withUrl("/zippyzipjp/job/storeAreaXml"));                
            
        } else if (path.equals("/storeAreaXml")) {
            
            generatorService.storeAreaXml();
            getQueue().add(withUrl("/zippyzipjp/job/storeAreaIme"));                
            
        } else if (path.equals("/storeAreaIme")) {
            
            generatorService.storeAreaIme();
            getQueue().add(withUrl("/zippyzipjp/job/storeCorpText"));                
            
        } else if (path.equals("/storeCorpText")) {
            
            generatorService.storeCorpText();
            getQueue().add(withUrl("/zippyzipjp/job/storeCorpCsv"));                
            
        } else if (path.equals("/storeCorpCsv")) {
            
            generatorService.storeCorpCsv();
            getQueue().add(withUrl("/zippyzipjp/job/storeCorpJson"));                
            
        } else if (path.equals("/storeCorpJson")) {
            
            generatorService.storeCorpJson();
            getQueue().add(withUrl("/zippyzipjp/job/storeCorpXml"));                
            
        } else if (path.equals("/storeCorpXml")) {
            
            generatorService.storeCorpXml();
            getQueue().add(withUrl("/zippyzipjp/job/storeCorpIme"));                
            
        } else if (path.equals("/storeCorpIme")) {
            
            generatorService.storeCorpIme();
            getQueue().add(withUrl("/zippyzipjp/job/storeJsonPrefCity"));                
       
        } else if (path.equals("/storeJsonPrefCity")) {
            
            generatorService.storeJsonPrefCity();
            getQueue().add(withUrl("/zippyzipjp/job/storeJsonArea"));                
       
        } else if (path.equals("/storeJsonArea")) {
            
            generatorService.storeJsonArea();
            getQueue().add(withUrl("/zippyzipjp/job/storeJsonCorp"));
            
        } else if (path.equals("/storeJsonCorp")) {
            
            generatorService.storeJsonCorp();
            getQueue().add(withUrl("/zippyzipjp/job/storeJsonZips"));
            
        } else if (path.equals("/storeJsonZips")) {
            
            String digit = RequestHelper.getStringParam(request, "digit");
            digit = generatorService.storeJsonZips(digit);
            
            if (digit != null) {
                getQueue().add(withUrl("/zippyzipjp/job/storeJsonZips")
                        .param("digit", digit));
            } else {
                getQueue().add(withUrl("/zippyzipjp/job/updateBuilding"));
            }
            
        } else if (path.equals("/updateBuilding")) {
            
            generatorService.updateBuilding();
            getQueue().add(withUrl("/zippyzipjp/job/complete"));
            
        } else if (path.equals("/complete")) {
            generatorService.complete();
        }
        
        response.getWriter().println("End.");
            
        return ret;
    }
    
    Queue getQueue() { return QueueFactory.getDefaultQueue(); }
}
