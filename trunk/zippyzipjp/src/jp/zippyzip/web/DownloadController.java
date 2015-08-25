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

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.zippyzip.Controller;
import jp.zippyzip.DistributorService;

/**
 * Jobを HTTP 経由で受け付けるコントローラ。
 * 
 * @author Michinobu Maeda
 */
public class DownloadController implements Controller {

    /** 配布サービス */
    private DistributorService distributorService;

    /**
     * 配布サービスを取得する。
     * 
     * @return ジョブサービス
     */
    public DistributorService getDistributorService() {
        return distributorService;
    }

    /**
     * 配布サービスを設定する。
     * 
     * @param distributorService ジョブサービス
     */
    public void setDistributorService(DistributorService distributorService) {
        this.distributorService = distributorService;
    }
    /* (non-Javadoc)
     * @see jp.zippyzip.Controller#defaultHandler(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.String, boolean)
     */
    public String defaultHandler(HttpServletRequest request,
            HttpServletResponse response, String path, boolean isAdmin)
    throws IOException {
        
        String ret = null;
        
        if (path.toLowerCase().endsWith(".lzh")) {
                
            response.setContentType("application/octet-stream");
            response.getOutputStream().write(distributorService.getRawData(
                    path.replaceFirst(".*\\/", "")));
        
        } else if (path.endsWith(".zip")) {
            
            response.setContentType("application/zip");
            response.getOutputStream().write(distributorService.getRawData(
                    path.replaceFirst(".*\\/[0-9]*_*", "")));

        } else if (path.equals("/feed.atom")) {
            
            request.setAttribute("info", distributorService.getZipInfo());
            ret = "feed";
            
        } else {
            
            ret = "../?message=priv.admin";
        }
            
        return ret;
    }
    
}
