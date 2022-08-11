package com.bitfye.common.crypto.encrypt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;

public class RSAFilter implements Filter {
    final Logger logger = LoggerFactory.getLogger(this.getClass());
    private Boolean allowSwaggerUi = false;
    RSAKeyPair myKeyPair;
    RSAKeyPair peerKeyPair;

    public RSAFilter() {
    }

    public void setAllowSwaggerUi(Boolean allow) {
        this.allowSwaggerUi = allow;
    }

    public void setMyKeyPair(RSAKeyPair myKeyPair) {
        this.myKeyPair = myKeyPair;
    }

    public void setPeerKeyPair(RSAKeyPair peerKeyPair) {
        this.peerKeyPair = peerKeyPair;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest)request;
        HttpServletResponse resp = (HttpServletResponse)response;
        this.logger.warn("audit rsa begin process...");
        if (this.allowSwaggerUi) {
            this.logger.warn("allow swagger ui, skip decrypt...");
            String referer = req.getHeader("Referer");
            if (referer != null && referer.contains("swagger-ui.html")) {
                chain.doFilter(request, response);
                return;
            }
        }

        this.logger.warn("Decrypt request: " + req.getMethod() + ": " + req.getRequestURI());
        WrappedHttpServletResponse wrappedResponse = new WrappedHttpServletResponse(resp);
        byte[] decrypted;
        if (req.getMethod().equals("POST")) {
            Object var7 = null;

            try {
                StringBuilder sb = new StringBuilder(8192);
                char[] buffer = new char[1024];
                BufferedReader reader = req.getReader();
                String encKey = reader.readLine();
                String encIv = reader.readLine();
                boolean var13 = false;

                int n;
                while((n = reader.read(buffer)) != -1) {
                    sb.append(buffer, 0, n);
                }

                String encMessage = sb.toString().trim();
                this.logger.warn("now decrypted msg: " + encMessage);
                byte[] aesKey = this.myKeyPair.decryptByPrivateKey(Base64.getDecoder().decode(encKey.getBytes(StandardCharsets.UTF_8)));
                this.logger.warn("using private key to decrypt aes key ok!");
                byte[] aesIv = this.myKeyPair.decryptByPrivateKey(Base64.getDecoder().decode(encIv.getBytes(StandardCharsets.UTF_8)));
                this.logger.warn("using private key to decrypt iv key ok!");
                if (aesKey.length != 32) {
                    throw new IllegalArgumentException("invalid key size: " + aesKey.length);
                }

                if (aesIv.length != 16) {
                    throw new IllegalArgumentException("invalid iv size: " + aesIv.length);
                }

                decrypted = AesUtil.decrypt(aesKey, aesIv, Base64.getDecoder().decode(encMessage.getBytes(StandardCharsets.UTF_8)));
                this.logger.warn("decrypt ok: " + new String(decrypted, "UTF-8"));
            } catch (Exception var18) {
                this.logger.warn("Decrypt failed!", var18);
                throw new ServletException(var18);
            }

            ByteArrayInputStream input = new ByteArrayInputStream(decrypted);
            HttpServletRequestWrapper wrappedRequest = new WrappedHttpServletRequest(req, input);
            chain.doFilter(wrappedRequest, wrappedResponse);
        } else {
            chain.doFilter(req, wrappedResponse);
        }

        this.logger.warn("will send original msg: " + wrappedResponse.output.toByteArray());
        resp.setHeader("Content-Type", "text/plain");

        try {
            decrypted = AesUtil.randomKey();
            byte[] rndIv = AesUtil.randomIV();
            byte[] encrypted = AesUtil.encrypt(decrypted, rndIv, wrappedResponse.output.toByteArray());
            PrintWriter pw = resp.getWriter();
            this.logger.warn("now using public key to encrypt...");
            pw.write(Base64.getEncoder().encodeToString(this.peerKeyPair.encryptByPublicKey(decrypted)));
            pw.write(10);
            pw.write(Base64.getEncoder().encodeToString(this.peerKeyPair.encryptByPublicKey(rndIv)));
            pw.write(10);
            pw.write(Base64.getEncoder().encodeToString(encrypted));
            pw.flush();
        } catch (GeneralSecurityException var17) {
            this.logger.warn("Encrypt failed!", var17);
            throw new ServletException(var17);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.logger.info("Init RSADecryptFilter...");
    }

    @Override
    public void destroy() {
        this.logger.info("Destroy RSADecryptFilter...");
    }
}

