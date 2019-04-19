package cn.imaq.autumn.rpc.client.net;

import cn.imaq.autumn.rpc.client.exception.RpcHttpException;

public interface RpcHttpClient {
    default byte[] get(String url, int timeout) throws RpcHttpException {
        return post(url, null, null, timeout);
    }

    byte[] post(String url, byte[] payload, String mime, int timeout) throws RpcHttpException;
}
