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

import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.zippyzip.Controller;
import jp.zippyzip.LzhService;

/**
 * Welcome Page のコントローラ。
 * 
 * @author Michinobu Maeda
 */
public class DefaultController implements Controller {

    /** メッセージのリソース */
    private ResourceBundle bandle = ResourceBundle.getBundle(
            jp.zippyzip.impl.MessageResourceBundle.class.getName());

    private final static String FILE_UPLOAD_KEY = "FileUploadKey";

    /**
     *  LZH 管理サービス
     */
    private LzhService lzhService;

    /**
     * LZH 管理サービスを設定する。
     * 
     * @return LZH 管理サービス
     */
    public LzhService getLzhService() {
        return lzhService;
    }

    /**
     * LZH 管理サービスを取得する。
     * 
     * @param lzhService LZH 管理サービス
     */
    public void setLzhService(LzhService lzhService) {
        this.lzhService = lzhService;
    }

    /* (non-Javadoc)
     * @see jp.zippyzip.Controller#defaultHandler(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.String, boolean)
     */
    public String defaultHandler(HttpServletRequest request,
            HttpServletResponse response, String path, boolean isAdmin) {
        
        request.setAttribute("message", bandle.getString(
                "" + RequestHelper.getStringParam(request, "message")));
        request.setAttribute("stt", bandle.getString(
                lzhService.isStatusUpdated() ?
                        "con.updated" : "con.notupdated"));
        request.setAttribute("isAdmin", isAdmin);
        request.setAttribute("sttList", lzhService.getAll());
        request.setAttribute("sttEdit",
                RequestHelper.getStringParam(request, "sttEdit"));
        request.setAttribute("archList", lzhService.getInfoAll());
        request.setAttribute("fileUploadKey", FILE_UPLOAD_KEY);
        
        return "default";
    }

}
