package cn.imaq.autumn.rpc.server;

import cn.imaq.autumn.core.context.AutumnContext;
import cn.imaq.autumn.rpc.server.handler.AutumnRpcRequestHandler;
import cn.imaq.autumn.rpc.server.net.AbstractRpcHttpServer;
import cn.imaq.autumn.rpc.server.net.RPCHttpServerFactory;
import cn.imaq.autumn.rpc.util.AutumnRPCBanner;
import cn.imaq.autumn.rpc.util.PropertiesUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Properties;

@Slf4j
public class AutumnRPCServer {
    private static final String DEFAULT_CONFIG = "autumn-rpc-server-default.properties";

    private static AbstractRpcHttpServer httpServer;

    private static final Object mutex = new Object();

    public static void start() {
        start(null);
    }

    public static void start(String configFile) {
        synchronized (mutex) {
            // Stop existing server
            stop();
            AutumnRPCBanner.printBanner();
            // Load config
            Properties config = new Properties();
            try {
                PropertiesUtil.load(config, DEFAULT_CONFIG, configFile);
            } catch (IOException e) {
                log.error("Error loading config: {}", String.valueOf(e));
            }
            // Scan services with scanners
            AutumnContext rpcContext = new AutumnContext("AutumnRPCContext");
            AutumnRpcRequestHandler handler = new AutumnRpcRequestHandler(config, rpcContext);
            log.warn("Scanning services to expose ...");
            rpcContext.scanComponents();
            // Start HTTP server
            String host = config.getProperty("http.host", "0.0.0.0");
            int port = Integer.valueOf(config.getProperty("http.port", "8801"));
            httpServer = RPCHttpServerFactory.create(config.getProperty("http.server"), host, port, handler);
            log.info("Using HTTP server: {}", httpServer.getClass().getSimpleName());
            log.warn("Starting HTTP server ...");
            try {
                httpServer.start();
                log.warn("Bootstrap finished");
            } catch (IOException e) {
                log.error("Error starting server: {}", String.valueOf(e));
            }
        }
    }

    public static void stop() {
        synchronized (mutex) {
            if (httpServer != null) {
                try {
                    httpServer.stop();
                } catch (IOException e) {
                    log.error("Error stopping server: {}", String.valueOf(e));
                }
            }
        }
    }
}
