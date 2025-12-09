# 🚦 CI & Quality

- Every Pull Request must pass all tests to be merged.
- CI workflows ensure that no untested code is deployed.
# Contributing to btc-node-dashboard

Thank you for considering contributing to this project! Please read the following guidelines to help us maintain a robust, reactive, and high-quality codebase.



## ⚡ Reactive Programming & Non-Blocking Guarantees

- All API endpoints (REST and WebSocket) must be implemented using Mutiny (`Uni`/`Multi`), ensuring non-blocking I/O.
- All RPC calls to Bitcoin Core are performed asynchronously, composed reactively, and executed in parallel with DEBUG logs and latency measurement for each call.
- No blocking calls (e.g. `Thread.sleep`, synchronous I/O, blocking DB access) are allowed in the critical path.
- WebSocket push and scheduled broadcast use non-blocking data fetch and asynchronous serialization.

### Reactive Entry Points

- `BtcController` (REST): All methods return `Uni<T>`
- `ConfigController` (REST): All methods return `Uni<T>`
- `DashboardWebSocket`: All data sent to clients is fetched and pushed reactively
- `RpcServices`: All services are parallel, non-blocking, logged and measured

### Contribution Guidelines

- Never introduce blocking calls in the critical path.
- Always use Mutiny (`Uni`/`Multi`) for async operations and data composition.
- If you add a new endpoint or service, ensure it is non-blocking and returns `Uni` or `Multi`.
- Use `.onFailure()` and `.ifNoItem().after(Duration)` for robust error and timeout handling.
- Add tests for any new reactive code (see existing tests for patterns).

For more details, see the comments in `RpcServices`, `BtcController`, and `DashboardWebSocket`.

---

Feel free to open issues or pull requests if you have questions or suggestions!
