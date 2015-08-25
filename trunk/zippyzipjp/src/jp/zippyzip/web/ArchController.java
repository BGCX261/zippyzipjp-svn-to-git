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
import java.io.InputStream;
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
 * アーカイブデータを扱うページのコントローラ。
 * 
 * @author Michinobu Maeda
 */
public class ArchController implements Controller {

    private final static int BUFSZ = 512;
    private final static String FILE_UPLOAD_KEY = "FileUploadKey";
    private final static int LZH_MAX_SIZE = 4096000;

    /** ログ　*/
    protected Logger log = Logger.getLogger(this.getClass().getName());

    /** LZH サービス */
    private LzhService lzhService;

    /** 生成物サービス */
    private GeneratorService generatorService;

    /**
     * LZH サービスを取得する。
     * 
     * @return LZH サービス
     */
    public LzhService getLzhService() {
        return lzhService;
    }

    /**
     * LZH サービスを設定する。
     * 
     * @param lzhService LZH サービス
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

    /*(non-Javadoc)
     * @see jp.zippyzip.Controller#defaultHandler(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.String, boolean)
     */
    public String defaultHandler(HttpServletRequest request,
            HttpServletResponse response, String path, boolean isAdmin)
    throws IOException {
        
        String ret = null;
        
        if (path.equals("/upload")) {
            ret = upload(request.getInputStream());
        } else if (path.equals("/delete")) {
            ret = delete(RequestHelper.getStringParam(request, "key"));
        } else if (path.equals("/fetchLzh")) {
            ret = fetchLzh();
        } else if (path.equals("/refreshZipDataStart")) {
            ret = refreshZipDataStart();
        } else if (path.equals("/resetZipDataStart")) {
            ret = resetZipDataStart();
        } else if (path.equals("/storeJsonPrefs")) {
            ret = storeJsonPrefs();
        } else {
            ret = "redirect:../";
        }
            
        return ret;
    }

    /**
     * アーカイブファイルをアップロードする。
     * 
     * @param is アップロードデータ
     * @return 遷移先のページ。
     */
    public String upload(InputStream is) {
        
        String ret = "upload.success";
        
        byte[] buff = new byte [LZH_MAX_SIZE];
        
        try {
            
            int len = is.read(buff);
            int size = len;
            
            while (0 < len) {
                
                len = is.read(buff, size, BUFSZ);
                size += len;
            }
            
            is.close();
            
            byte[] boun = RequestHelper.getFirstBoundery(buff, size);
            
            if ((boun == null) || (boun.length < 2)) {
                return "redirect:../?message=upload.failed";
            }
            
            int offset = RequestHelper.getOffset(buff, size, boun, FILE_UPLOAD_KEY);
            int next = RequestHelper.getNextBoundery(buff, offset, size, boun);
            
            if ((buff[next - 2] == (byte)13) && (buff[next - 1] == (byte)10)) {
                --next;
            }
            
            lzhService.store(buff, offset, next - offset);
        
        } catch (IOException e) {
            log.log(Level.WARNING, "", e);
            ret = "upload.failed";
        }
        
        return "redirect:../?message=" + ret;
    }

    /**
     * アーカイブデータを削除する。
     * 
     * @param key キー
     * @return 遷移先のページ。
     */
    public String delete(String key) {
        
        lzhService.delete(key);
        
        return "redirect:../";
    }
    
    /**
     * 最新のデータの有無を確認し、ダウンロードする。
     * 
     * @return 遷移先のページ。
     */
    public String fetchLzh() {
        
        boolean ret = lzhService.fetch();
        
        return "redirect:../?message=" + (ret ? "lzh.update" : "lzh.cancel");
    }
    
    /**
     * 郵便番号データの更新を開始する。
     * 
     * @return 遷移先のページ。
     */
    public String refreshZipDataStart() {
        
        Queue queue = QueueFactory.getDefaultQueue();
        queue.add(withUrl("/zippyzipjp/job/checkUpdate"));                
        
        return "redirect:../";
    }
    
    /**
     * 郵便番号データのリセットを開始する。
     * 
     * @return 遷移先のページ。
     */
    public String resetZipDataStart() {
        
        Queue queue = QueueFactory.getDefaultQueue();
        queue.add(withUrl("/zippyzipjp/job/reset"));                
        
        return "redirect:../";
    }
    
    /**
     * JSON 一覧データの作成を開始する。
     * 
     * @return 遷移先のページ。
     */
    public String storeJsonPrefs() {
        
        Queue queue = QueueFactory.getDefaultQueue();
        queue.add(withUrl("/zippyzipjp/job/storeJsonPrefs"));                
        
        return "redirect:../";
    }
    
}
