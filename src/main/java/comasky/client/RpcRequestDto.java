package comasky.client;

import java.util.List;

/**
 * DTO for Bitcoin Core RPC requests.
 */
public record RpcRequestDto(
    String jsonrpc,
    String id,
    String method,
    List<Object> params
) {}
