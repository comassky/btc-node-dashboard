# OpenAPI Specification

## 📝 Overview

The Bitcoin Node Dashboard API is documented using the OpenAPI 3.0 specification. The OpenAPI schema is **generated at build time** and saved in the `target/` directory, but **not exposed at runtime** for security reasons.

## 📦 Generated Files

The OpenAPI specification files are located at the project root:

- `openapi.json` - JSON format (versioned in git)
- `openapi.yaml` - YAML format (versioned in git)

## 🔨 Generating the Specification

The OpenAPI spec is automatically generated during Maven compilation:

```bash
# Generate/Update OpenAPI spec
mvn clean package -DskipTests

# The file will be at project root
cat openapi.yaml
```

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
docker run -p 8080:80 \
  -v $(pwd):/usr/share/nginx/html/spec \
  -e SPEC_URL=spec/openapi.yaml \
  redocly/redoc
```

Access at: http://localhost:8080

### Option 4: Swagger UI (Docker)

```bash
# Serve with Swagger UI
docker run -p 8080:8080 \
  -e SWAGGER_JSON=/spec/openapi.yaml \
  -v $(pwd):/spec \
  swaggerapi/swagger-ui
```

Access at: http://localhost:8080

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
mvn clean package -DskipTests
```

The file will be created/updated at project root:
- `openapi.yaml` (versioned in git)

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
quarkus.swagger-ui.always-include=false
mp.openapi.extensions.smallrye.info.title=Bitcoin Node Dashboard API
mp.openapi.extensions.smallrye.info.version=1.5.0
```

### Enabling Swagger UI for Development

If you need Swagger UI during development, you can enable it in `application-local.properties`:

```properties
quarkus.swagger-ui.always-include=true
quarkus.swagger-ui.path=/swagger-ui
```

Then access it at: http://localhost:8080/swagger-ui (dev mode only)

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

## 📖 Additional Resources

- [MicroProfile OpenAPI Specification](https://github.com/eclipse/microprofile-open-api)
- [Quarkus OpenAPI Guide](https://quarkus.io/guides/openapi-swaggerui)
- [Swagger UI Documentation](https://swagger.io/tools/swagger-ui/)
