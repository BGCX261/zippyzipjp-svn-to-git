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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.zippyzip.Controller;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

/**
 * デフォルトで呼び出される Servlet。
 * 
 * @author Michinobu Maeda
 */
public class DefaultServlet extends HttpServlet {

    /** シリアライズのIDの値。 */
    private static final long serialVersionUID = 3429014790143055403L;
    
    /** ビューのパスの Prefix */
    private static final String VIEW_PREFIX = "/WEB-INF/views/";
    
    /** ビューのパスの Suffix */
    private static final String VIEW_SUFFIX = ".jsp";

    /** ログ　*/
    protected Logger log = Logger.getLogger(this.getClass().getName());
    {
        log.setLevel(Level.INFO);
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getPathInfo();
       
        // Google ID によるログインの有無を確認する。
        UserService userService = UserServiceFactory.getUserService();
        boolean isAdmin = false;
        
        if ((!path.startsWith("/download"))
                && (!path.startsWith("/job"))
                && (!path.startsWith("/feed"))) {
            
            if (!userService.isUserLoggedIn()) {
            
                response.sendRedirect(userService.createLoginURL(
                        request.getRequestURI()));
                log.info("Access denied: " + request.getPathInfo());
                return;
            } else {
                isAdmin = userService.isUserAdmin();
            }
        }

        ApplicationContext context = ApplicationContext.getContext();
        if ((!context.isDebug()) && request.getServerName().startsWith("localhost")) {
            context.setDebug(true);
            log.info("context.setDebug(true)");
        }
        String ret = null;
               
        if ((path == null) || path.equals("") || path.equals("/")) {
            
            Controller controller = context.getDefaultController();
            ret = controller.defaultHandler(request, response, path, isAdmin);
            
        } else if (path.startsWith("/content")) {
            
            Controller controller = context.getContentController();
            ret = controller.defaultHandler(request, response,
                    path.replaceFirst("\\/content", ""), isAdmin);

            
        } else if (path.startsWith("/arch")) {
            
            Controller controller = context.getArchController();
            ret = controller.defaultHandler(request, response,
                    path.replaceFirst("\\/arch", ""), isAdmin);

        } else if (path.startsWith("/feed")) {
            
            Controller controller = context.getDownloadController();
            ret = controller.defaultHandler(request, response,
                    path, isAdmin);
           
        } else if (path.startsWith("/job")) {
            
            Controller controller = context.getJobController();
            ret = controller.defaultHandler(request, response,
                    path.replaceFirst("\\/job", ""), isAdmin);
           
        } else if (path.startsWith("/download")) {
            
            Controller controller = context.getDownloadController();
            ret = controller.defaultHandler(request, response,
                    path.replaceFirst("\\/download", ""), isAdmin);

        } else {
            
            log.info("Not found: " + path);
            response.sendRedirect("/zippyzipjp/?message=invalidUrl");
        }

        context.closePm();
        
        if (ret != null) {
            
            if (ret.startsWith("redirect:")) {
                response.sendRedirect(ret.replaceFirst("redirect:", ""));
            } else {
                getServletContext().getRequestDispatcher(
                        VIEW_PREFIX + ret + VIEW_SUFFIX).forward(
                                request, response);
            }
        }
    }
    
}
