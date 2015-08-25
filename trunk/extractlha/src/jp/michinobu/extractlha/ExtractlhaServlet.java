package jp.michinobu.extractlha;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.urlfetch.FetchOptions;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

import jp.gr.java_conf.dangan.util.lha.LhaHeader;
import jp.gr.java_conf.dangan.util.lha.LhaInputStream;

@SuppressWarnings("serial")
public class ExtractlhaServlet extends HttpServlet {

    private final Logger log = Logger.getLogger(ExtractlhaServlet.class
            .getName());

    public void service(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        log.setLevel(Level.INFO);

        @SuppressWarnings("rawtypes")
        Enumeration names = req.getParameterNames();
        HashMap<String, String> param = new HashMap<String, String>();

        while (names.hasMoreElements()) {

            String name = (String) names.nextElement();
            String[] values = req.getParameterValues(name);
            if ((values != null) && (values.length > 0)) {
                param.put(name, values[0]);
            }
        }

        if (!param.containsKey("req")) {

            resp.setContentType("text/plain; charset=UTF-8");
            resp.getWriter().println("Helo");
            
        } else if ("fetch".equals(param.get("req"))) {

            try {

                fetch(param.get("url"), resp);

            } catch (IOException e) {

                resp.setContentType("text/plain; charset=UTF-8");
                resp.getWriter().println("Error:" + param.get("url"));
            }
        }
    }

    private void fetch(String param, HttpServletResponse resp)
            throws IOException {

        byte[] lha = URLFetchServiceFactory
                .getURLFetchService()
                .fetch(new HTTPRequest(new URL(param), HTTPMethod.GET,
                        FetchOptions.Builder.withDeadline(10))).getContent();

        log.info(param);
        log.info("" + lha.length + " byte");

        LhaInputStream in = new LhaInputStream(new ByteArrayInputStream(lha));
        LhaHeader header = null;
        byte[] buff = new byte[1024];
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(bos);

        while (null != (header = in.getNextEntry())) {

            log.info("Path:" + header.getPath());
            log.info("Last Modified:" + header.getLastModified());
            log.info("Original Size:" + header.getOriginalSize());

            ZipEntry entry = new ZipEntry(header.getPath().replace("\\", "/"));
            entry.setSize(header.getOriginalSize());
            entry.setTime(header.getLastModified().getTime());
            zip.putNextEntry(entry);

            int ret = 0;
            long len = 0;
            while (0 < (ret = in.read(buff))) {

                len += (long) ret;
                zip.write(buff, 0, ret);
            }

            log.info("Extracted Size:" + len);

            zip.closeEntry();

        }

        zip.finish();
        zip.flush();
        byte[] raw = bos.toByteArray();

        resp.setContentType("application/zip");
        resp.setHeader("Content-length", "" + raw.length);

        if (param.endsWith(".lzh")) {

            resp.setHeader(
                    "Content-disposition",
                    "inline; filename="
                            + param.replaceFirst(".*\\/", "").replaceFirst(
                                    ".lzh$", ".zip"));

        } else if (param.endsWith(".LZH")) {

            resp.setHeader(
                    "Content-disposition",
                    "inline; filename="
                            + param.replaceFirst(".*\\/", "").replaceFirst(
                                    ".LZH$", ".zip"));
        }

        ServletOutputStream out = resp.getOutputStream();
        out.write(raw);

    }
}
