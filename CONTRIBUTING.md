# 🚦 CI & Quality

- Every Pull Request must pass all tests to be merged.
- CI workflows ensure that no untested code is deployed.
# Contributing to btc-node-dashboard

Thank you for considering contributing to this project! Please read the following guidelines to help us maintain a robust, reactive, and high-quality codebase.

## ⚡ Reactive Programming & Non-Blocking Guarantees

- All API endpoints (REST and WebSocket) must be implemented using Mutiny (`Uni`/`Multi`), ensuring non-blocking I/O.
- All RPC calls to Bitcoin Core must be performed asynchronously and composed reactively.
- No blocking calls (e.g. `Thread.sleep`, synchronous I/O, blocking database access) are allowed in the request/response path.
- WebSocket push and scheduled broadcast must use non-blocking data fetch and serialization.

### Reactive Entry Points

- `BtcController` (REST): All methods return `Uni<T>`
- `ConfigController` (REST): All methods return `Uni<T>`
- `DashboardWebSocket`: All data sent to clients is fetched and pushed reactively
- `RpcServices`: All service methods return `Uni<T>` and compose multiple async calls

### Contribution Guidelines

- Never introduce blocking calls in the request/response path.
- Always use Mutiny (`Uni`/`Multi`) for async operations and data composition.
- If you add new endpoints or services, ensure they are fully non-blocking and return `Uni` or `Multi`.
- Use `.onFailure()` and `.ifNoItem().after(Duration)` for robust error and timeout handling.
- Add tests for new reactive code (see existing tests for patterns).

For more details, see the code comments in `RpcServices`, `BtcController`, and `DashboardWebSocket`.

---

Feel free to open issues or pull requests if you have questions or suggestions!
