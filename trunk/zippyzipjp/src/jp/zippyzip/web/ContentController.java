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

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.zippyzip.ContentStatus;
import jp.zippyzip.Controller;
import jp.zippyzip.LzhService;
/**
 * 更新情報を扱うページのコントローラ。
 * 
 * @author Michinobu Maeda
 */
public class ContentController implements Controller {

    /** サービス */
    private LzhService lzhService;

    /**
     * @return lzhService
     */
    public LzhService getLzhService() {
        return lzhService;
    }

    /**
     * @param lzhService lzhService
     */
    public void setLzhService(LzhService lzhService) {
        this.lzhService = lzhService;
    }

    /* (non-Javadoc)
     * @see jp.zippyzip.Controller#defaultHandler(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.String, boolean)
     */
    public String defaultHandler(HttpServletRequest request,
            HttpServletResponse response, String path, boolean isAdmin) {
        
        String ret = null;
        
        if (path.equals("/status")) {
            
            ret = updateStatus(
                    RequestHelper.getStringParam(request, "id"),
                    RequestHelper.getStringParam(request, "depends"),
                    RequestHelper.getStringParam(request, "url"),
                    RequestHelper.getStringParam(request, "lastUpdate"),
                    RequestHelper.getStringParam(request, "lastCheck"),
                    RequestHelper.getBooleanParam(request, "lzh"),
                    RequestHelper.getBooleanParam(request, "corp"));
            
        } else if (path.equals("/dep")) {
            ret = checkDepends();
        } else if (path.equals("/con")) {
            ret = checkContents();
        } else if (path.equals("/init")) {
            ret = initStatus();
        } else if (path.equals("/initAll")) {
            ret = initAll();
        } else {
            ret = "redirect:../";
        }
        
        return ret;
    }

    public String updateStatus(String id, String depends, String urlStr,
            String lastUpdate, String lastCheck, boolean lzh, boolean corp) {

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date upd = new Date(0);
        Date chk = new Date(0);
        
        // URLの書式を確認する。
        try {
            new URL(urlStr);
        } catch (NullPointerException e) {            
        } catch (MalformedURLException e) {
            urlStr = null;
        }
        
        if ((id == null) || (id.length() == 0)) {
            
            if (urlStr != null) {
                
                String name = urlStr.replaceFirst(".*/", "");
                
                if (name.toLowerCase().endsWith(".lzh")) {
                    
                    lzh = true;
                    corp = (name.toLowerCase().matches(".*jigyosyo.*"));
                    depends = id = (corp ? "j" : "k");

                    if (name.matches("[0-9]+.*")) {
                        id = depends + name.replaceFirst("[^0-9].*", "");
                    } else {
                        id = depends + "01";
                    }
                    
                } else {
                    
                    lzh = false;
                    corp = false;
                }
            
            } else {
                
                return "redirect:../";
                
            }
        }
        
        if (0 < id.indexOf(",")) {
            id = id.substring(0, id.indexOf(","));
        }
                
        try {
            upd = format.parse(lastUpdate);
        } catch (NullPointerException e) {            
        } catch (ParseException e) {
        }
        
        try {
            chk = format.parse(lastCheck);
        } catch (NullPointerException e) {
        } catch (ParseException e) {
        }
        
        ContentStatus stt = new ContentStatus(
                id, depends, urlStr, upd, chk, lzh, corp);
        lzhService.update(stt);
        
        return "redirect:../";
    }

    /**
     * 依存コンテンツの更新を確認する。
     * 
     * @return 遷移先のページ
     */
    public String checkDepends() {
        
        return lzhService.checkDepends() ?
                "redirect:../?message=dep.updated"
                : "redirect:../?message=dep.notupdated";
    }

    /**
     * コンテンツの更新を確認する。
     * 
     * @return 遷移先のページ
     */
    public String checkContents() {
        
        return lzhService.checkContents() ?
                "redirect:../?message=con.checked"
                : "redirect:../?message=con.nonchecked";
    }

    /**
     * 更新情報を初期化する。
     * 
     * @return 遷移先のページ
     */
    public String initStatus() {
        
        lzhService.initStatus();
        return "redirect:../";
    }

    /**
     * 全てを初期化する。
     * 
     * @return 遷移先のページ
     */
    public String initAll() {
        
        lzhService.initAll();
        return "redirect:../";
    }
}
