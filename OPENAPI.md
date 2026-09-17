# OpenAPI Specification

## 📝 Overview

The Bitcoin Node Dashboard API is documented using OpenAPI 3.0.3. The schema is **generated at build time** at the project root. It is also available at runtime at `/q/openapi`; disabling inclusion of Swagger UI in production does not disable this endpoint.

Schema metadata uses `${quarkus.application.version}`, so its version follows the Maven application version rather than a separately maintained literal.

## 📦 Generated Files

The OpenAPI specification files are located at the project root:

- [openapi.json](openapi.json) - JSON format (versioned in git)
- [openapi.yaml](openapi.yaml) - YAML format (versioned in git)

## 🔨 Generating the Specification

The OpenAPI spec is generated during Quarkus packaging. With JDK 25 and Maven:

```bash
# Run backend/frontend unit tests, build the application and update both schemas
mvn -B --no-transfer-progress clean verify

# The file will be at project root
cat openapi.yaml
```

Maven installs Node.js, npm and pnpm and builds the frontend too. For a local generation without either unit-test suite, use `mvn package -DskipTests -DskipFrontendTests=true`; `-DskipTests` alone does not skip frontend tests. Do not use these skip flags in release workflows.

## Release Documentation

[release.yml](.github/workflows/release.yml) takes a version `X.Y.Z` manually, updates Maven's version, verifies the native build, and uploads both root-level schemas as the `release-openapi` artifact. The schemas and synchronized frontend version are committed with the release before its tag is created.

After the image and GitHub Release are published, the `openapi` job downloads that exact artifact and deploys a Redoc site to GitHub Pages. It does not regenerate the schema from the subsequent development `-SNAPSHOT` commit.

Repository setup:

1. Set **Settings > Pages > Build and deployment > Source** to **GitHub Actions**.
2. Ensure the `github-pages` environment permits deployment from the branch used to dispatch the workflow.
3. Open the deployment URL reported by the workflow's `openapi` job.

The deployment uses `pages: write` and `id-token: write` permissions. Release creation also needs permission to push commits and tags; see [DOCKER.md](DOCKER.md). A failed Pages deployment does not roll back an already published image or release.

[publish-image.yml](.github/workflows/publish-image.yml) rebuilds an existing tag and uploads the schema artifact, but does not deploy GitHub Pages. A tag push alone does not trigger either publication workflow.

## 👁️ Viewing the Documentation

You can view and interact with the generated OpenAPI specification using various tools:

### Option 1: Swagger Editor (Online)

1. Go to [https://editor.swagger.io/](https://editor.swagger.io/)
2. Click **File** > **Import file**
3. Upload `openapi.json` or `openapi.yaml` from project root

### Option 2: VS Code Extension

Install the "OpenAPI (Swagger) Editor" extension:

```bash
code --install-extension 42Crunch.vscode-openapi
```

Then open `openapi.yaml` in VS Code.

### Option 3: Redoc (Docker)

```bash
# Serve the OpenAPI spec with Redoc
docker run --rm -p 8081:80 \
  -v "$PWD:/usr/share/nginx/html/spec:ro" \
  -e SPEC_URL=spec/openapi.yaml \
  redocly/redoc
```

Access at: http://localhost:8081

### Option 4: Swagger UI (Docker)

```bash
# Serve with Swagger UI
docker run --rm -p 8081:8080 \
  -e SWAGGER_JSON=/spec/openapi.yaml \
  -v "$PWD:/spec:ro" \
  swaggerapi/swagger-ui
```

Access at: http://localhost:8081

Run one viewer at a time on port 8081; the dashboard can remain on port 8080. When the application is running, its schema is also available at http://localhost:8080/q/openapi.

## 📋 API Overview

### Endpoints by Category

#### Bitcoin Node Monitoring
- `GET /api/dashboard` - Complete dashboard data aggregation
- `GET /api/getnetworkinfo` - Network information
- `GET /api/getBlockchainInfo` - Blockchain status
- `GET /api/getblock/{hash}` - Block details by hash
- `GET /api/getbestblockhash` - Latest block hash
- `GET /api/getmempoolinfo` - Mempool statistics

#### Configuration
- `GET /api/config` - Dashboard configuration settings

### WebSocket Endpoint
- `ws://localhost:8080/ws/dashboard` - Real-time updates

## 🚀 Example Usage

### Using cURL

```bash
# Get dashboard data
curl http://localhost:8080/api/dashboard

# Get network info
curl http://localhost:8080/api/getnetworkinfo

# Get block by hash
curl http://localhost:8080/api/getblock/00000000000000000001a0a0d0e0f0a0b0c0d0e0f0a0b0c0d0e0f0a0b0c0d0e0

# Get best block hash
curl http://localhost:8080/api/getbestblockhash
```

### Using JavaScript/Fetch

```javascript
// Get dashboard data
const response = await fetch('http://localhost:8080/api/dashboard');
const data = await response.json();
console.log(data);

// Get configuration
const config = await fetch('http://localhost:8080/api/config')
  .then(res => res.json());
console.log('Min outbound peers:', config.minOutboundPeers);
```

### Using Python

```python
import requests

# Get dashboard data
response = requests.get('http://localhost:8080/api/dashboard')
data = response.json()
print(data)

# Get network info
network_info = requests.get('http://localhost:8080/api/getnetworkinfo').json()
print(f"Network: {network_info}")
```

## 🔧 Development

### Generating Updated Documentation

The OpenAPI specification is automatically generated from code annotations during package:

```bash
mvn -B --no-transfer-progress clean verify
```

Both root-level schemas are updated. Review and commit [openapi.yaml](openapi.yaml) and [openapi.json](openapi.json) together when the API changes.

### Customizing the Documentation

Edit the annotations in:
- `src/main/java/comasky/api/BitcoinApiController.java` - Bitcoin Node endpoints documentation
- `src/main/java/comasky/api/ConfigController.java` - Configuration endpoints
- `src/main/resources/application.properties` - OpenAPI metadata (title, version, description)

### Configuration Options

In `application.properties`:

```properties
# OpenAPI Configuration
quarkus.smallrye-openapi.store-schema-directory=.
quarkus.smallrye-openapi.open-api-version=3.0.3
quarkus.smallrye-openapi.path=/q/openapi
quarkus.swagger-ui.always-include=false
mp.openapi.extensions.smallrye.info.title=Bitcoin Node Dashboard API
mp.openapi.extensions.smallrye.info.version=${quarkus.application.version}
```

### Enabling Swagger UI for Development

Swagger UI is available in Quarkus dev mode by default at `/q/swagger-ui`. To use a custom path only during development, add this to [application.properties](src/main/resources/application.properties):

```properties
%dev.quarkus.swagger-ui.path=/swagger-ui
```

Run `mvn quarkus:dev` with RPC configuration, then access http://localhost:8080/swagger-ui. Setting `quarkus.swagger-ui.always-include=true` is a build-time choice that includes Swagger UI in production too; it is not required for dev mode.

## 📚 Response Models

### GlobalResponse
Complete dashboard data including:
- Peer statistics (inbound/outbound)
- Blockchain information
- Network details
- Block information
- Mempool statistics
- Error details (if any)

### NetworkInfoResponse
- Version information
- Subversion
- Protocol version
- Network type
- Connections count

### BlockInfoResponse
- Block hash
- Height
- Timestamp
- Transaction count
- Size
- Weight
- Difficulty

### BlockchainInfoResponse
- Chain name
- Blocks count
- Headers count
- Best block hash
- Difficulty
- Verification progress
- Chain work

### MempoolInfoResponse
- Transaction count
- Total size (bytes)
- Memory usage
- Fee statistics

## 🔒 Security Notes

- The API is designed for local/private network use
- No authentication is required by default
- Ensure proper network security when exposing the API
- Consider using a reverse proxy with authentication for production
- `/q/openapi` exposes API metadata at runtime; protect it with the same network controls as the API
- Published GitHub Pages documentation is separate from the runtime and must not contain RPC credentials or other secrets

## 📖 Additional Resources

- [MicroProfile OpenAPI Specification](https://github.com/eclipse/microprofile-open-api)
- [Quarkus OpenAPI Guide](https://quarkus.io/guides/openapi-swaggerui)
- [Swagger UI Documentation](https://swagger.io/tools/swagger-ui/)
