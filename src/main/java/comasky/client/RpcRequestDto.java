package comasky.client;

import java.util.List;

/**
 * DTO for Bitcoin Core RPC requests.
 *
 * @param jsonrpc The JSON-RPC version (usually "1.0" or "2.0").
 * @param id      A unique identifier for the request.
 * @param method  The RPC method name to call.
 * @param params  A list of parameters for the RPC method.
 */
public record RpcRequestDto(
    String jsonrpc,
    String id,
    String method,
    List<Object> params
) {}
