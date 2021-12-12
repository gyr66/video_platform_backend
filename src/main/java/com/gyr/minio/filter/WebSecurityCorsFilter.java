package com.gyr.minio.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class WebSecurityCorsFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException, IOException {
        HttpServletResponse res = (HttpServletResponse) response;
        HttpServletRequest req = (HttpServletRequest) request;
        res.setHeader("Access-Control-Allow-Origin", req.getHeader("origin"));
        res.setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, UPDATE, PATCH, HEAD, OPTIONS");
        res.setHeader("Access-Control-Allow-Headers", "Accept,Authorization,DNT,Content-Type,Referer,User-Agent, tus-resumable, upload-length, upload-metadata, Location, upload-offset");
        res.setHeader("Access-Control-Expose-Headers", "Location, upload-offset, Upload-Length");
        res.setHeader("Access-Control-Allow-Credentials", "true");
        chain.doFilter(request, res);
    }

    @Override
    public void destroy() {
    }
}
