package com.bitfye.common.crypto.signature;

import com.bitfye.common.crypto.http.ErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SignatureFilter implements Filter {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    private Boolean allowSwaggerUi = false;
    ApiSignature apiSignature;
    SignatureConfigs signatureConfigs;
    ErrorHandler errorHandler = (req, resp, e) -> {
        HttpServletResponse response = (HttpServletResponse)resp;
        response.sendError(400);
    };

    public SignatureFilter() {
    }

    public void setSignatureConfigs(SignatureConfigs signatureConfigs) {
        this.signatureConfigs = signatureConfigs;
    }

    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public void setAllowSwaggerUi(Boolean allow) {
        this.allowSwaggerUi = allow;
    }

    public void init() {
        Map<String, String> apps = new HashMap();
        if (this.signatureConfigs == null) {
            this.log.warn("WARNING: YOU ARE USING SignatureFilter BUT NO CONFIGURATIONS!!!");
        } else {
            Iterator var2 = this.signatureConfigs.getSignatures().entrySet().iterator();

            while(var2.hasNext()) {
                Map.Entry<String, SignatureConfigs.SignatureConfig> entry = (Map.Entry)var2.next();
                String appName = (String)entry.getKey();
                String appId = ((SignatureConfigs.SignatureConfig)entry.getValue()).getAppid();
                String appKey = ((SignatureConfigs.SignatureConfig)entry.getValue()).getAppkey();
                this.log.info("Found app: {}, appid={}", appName, appId);
                apps.put(appId, appKey);
            }
        }

        this.apiSignature = new ApiSignature();
        this.apiSignature.setApps(apps);
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)req;
        if (this.allowSwaggerUi) {
            String referer = request.getHeader("Referer");
            if (referer != null && referer.contains("swagger-ui.html")) {
                chain.doFilter(req, resp);
                return;
            }
        }

        try {
            this.apiSignature.checkSignature(request);
        } catch (BadSignatureException var6) {
            this.log.warn(request.getRequestURI() + ": Bad signature: " + var6.getMessage(), var6);
            this.errorHandler.handle(req, resp, var6);
            return;
        }

        chain.doFilter(req, resp);
    }

    @Override
    public void init(FilterConfig config) throws ServletException {
        this.log.info("Init SignatureFilter...");
    }

    @Override
    public void destroy() {
        this.log.info("Destroy SignatureFilter...");
    }
}

