package comasky;

import comasky.client.RpcClient;
import comasky.rpcClass.*;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@QuarkusTest
class SubverStatsCalculationTest {

    @InjectMock
    RpcClient rpcClient;

    @Inject
    RpcServices rpcServices;

    @Test
    void testSubverDistribution_singleVersion() throws Exception {
        String mockPeerInfoResponse = """
            {
                "result": [
                    {
                        "id": 1,
                        "inbound": true,
                        "subver": "/Satoshi:27.0.0/"
                    },
                    {
                        "id": 2,
                        "inbound": true,
                        "subver": "/Satoshi:27.0.0/"
                    },
                    {
                        "id": 3,
                        "inbound": true,
                        "subver": "/Satoshi:27.0.0/"
                    }
                ],
                "error": null,
                "id": "quarkus-getpeerinfo"
            }
            """;

        mockAllRpcCalls(mockPeerInfoResponse);

        GlobalResponse response = rpcServices.getData();

        assertNotNull(response.getSubverDistribution());
        List<SubverStats> inboundStats = response.getSubverDistribution().inbound();
        
        assertEquals(1, inboundStats.size());
        assertEquals("/Satoshi:27.0.0/", inboundStats.get(0).getServer());
        assertEquals(100.0, inboundStats.get(0).getPercentage());
    }

    @Test
    void testSubverDistribution_multipleVersions() throws Exception {
        String mockPeerInfoResponse = """
            {
                "result": [
                    {
                        "id": 1,
                        "inbound": true,
                        "subver": "/Satoshi:27.0.0/"
                    },
                    {
                        "id": 2,
                        "inbound": true,
                        "subver": "/Satoshi:27.0.0/"
                    },
                    {
                        "id": 3,
                        "inbound": true,
                        "subver": "/Satoshi:26.0.0/"
                    },
                    {
                        "id": 4,
                        "inbound": true,
                        "subver": "/Satoshi:25.0.0/"
                    }
                ],
                "error": null,
                "id": "quarkus-getpeerinfo"
            }
            """;

        mockAllRpcCalls(mockPeerInfoResponse);

        GlobalResponse response = rpcServices.getData();

        List<SubverStats> inboundStats = response.getSubverDistribution().inbound();
        
        assertEquals(3, inboundStats.size());
        
        SubverStats v27Stats = inboundStats.stream()
            .filter(s -> s.getServer().equals("/Satoshi:27.0.0/"))
            .findFirst()
            .orElseThrow();
        
        assertEquals(50.0, v27Stats.getPercentage());
    }

    @Test
    void testSubverDistribution_withNullSubver() throws Exception {
        String mockPeerInfoResponse = """
            {
                "result": [
                    {
                        "id": 1,
                        "inbound": true,
                        "subver": "/Satoshi:27.0.0/"
                    },
                    {
                        "id": 2,
                        "inbound": true,
                        "subver": null
                    },
                    {
                        "id": 3,
                        "inbound": true,
                        "subver": "/Satoshi:27.0.0/"
                    }
                ],
                "error": null,
                "id": "quarkus-getpeerinfo"
            }
            """;

        mockAllRpcCalls(mockPeerInfoResponse);

        GlobalResponse response = rpcServices.getData();

        List<SubverStats> inboundStats = response.getSubverDistribution().inbound();
        
        // Only 1 distinct subversion, but percentage is calculated on total peers (including null)
        // 2 peers with /Satoshi:27.0.0/ out of 3 total = 66.67%
        assertEquals(1, inboundStats.size());
        assertEquals("/Satoshi:27.0.0/", inboundStats.get(0).getServer());
        assertEquals(66.67, inboundStats.get(0).getPercentage(), 0.01);
    }

    @Test
    void testSubverDistribution_inboundVsOutbound() throws Exception {
        String mockPeerInfoResponse = """
            {
                "result": [
                    {
                        "id": 1,
                        "inbound": true,
                        "subver": "/Satoshi:27.0.0/"
                    },
                    {
                        "id": 2,
                        "inbound": false,
                        "subver": "/Satoshi:26.0.0/"
                    },
                    {
                        "id": 3,
                        "inbound": true,
                        "subver": "/Satoshi:27.0.0/"
                    },
                    {
                        "id": 4,
                        "inbound": false,
                        "subver": "/Satoshi:26.0.0/"
                    }
                ],
                "error": null,
                "id": "quarkus-getpeerinfo"
            }
            """;

        mockAllRpcCalls(mockPeerInfoResponse);

        GlobalResponse response = rpcServices.getData();

        List<SubverStats> inboundStats = response.getSubverDistribution().inbound();
        List<SubverStats> outboundStats = response.getSubverDistribution().outbound();
        
        assertEquals(1, inboundStats.size());
        assertEquals(1, outboundStats.size());
        
        assertEquals("/Satoshi:27.0.0/", inboundStats.get(0).getServer());
        assertEquals(100.0, inboundStats.get(0).getPercentage());
        
        assertEquals("/Satoshi:26.0.0/", outboundStats.get(0).getServer());
        assertEquals(100.0, outboundStats.get(0).getPercentage());
    }

    @Test
    void testSubverDistribution_percentageCalculation() throws Exception {
        String mockPeerInfoResponse = """
            {
                "result": [
                    {
                        "id": 1,
                        "inbound": true,
                        "subver": "/Satoshi:27.0.0/"
                    },
                    {
                        "id": 2,
                        "inbound": true,
                        "subver": "/Satoshi:27.0.0/"
                    },
                    {
                        "id": 3,
                        "inbound": true,
                        "subver": "/Satoshi:27.0.0/"
                    },
                    {
                        "id": 4,
                        "inbound": true,
                        "subver": "/Satoshi:26.0.0/"
                    }
                ],
                "error": null,
                "id": "quarkus-getpeerinfo"
            }
            """;

        mockAllRpcCalls(mockPeerInfoResponse);

        GlobalResponse response = rpcServices.getData();

        List<SubverStats> inboundStats = response.getSubverDistribution().inbound();
        
        double totalPercentage = inboundStats.stream()
            .mapToDouble(SubverStats::getPercentage)
            .sum();
        
        assertEquals(100.0, totalPercentage, 0.01);
    }

    @Test
    void testSubverDistribution_emptyPeers() throws Exception {
        String mockPeerInfoResponse = """
            {
                "result": [],
                "error": null,
                "id": "quarkus-getpeerinfo"
            }
            """;

        mockAllRpcCalls(mockPeerInfoResponse);

        GlobalResponse response = rpcServices.getData();

        assertTrue(response.getSubverDistribution().inbound().isEmpty());
        assertTrue(response.getSubverDistribution().outbound().isEmpty());
    }

    @Test
    void testSubverDistribution_roundingPrecision() throws Exception {
        String mockPeerInfoResponse = """
            {
                "result": [
                    {
                        "id": 1,
                        "inbound": true,
                        "subver": "/Satoshi:27.0.0/"
                    },
                    {
                        "id": 2,
                        "inbound": true,
                        "subver": "/Satoshi:26.0.0/"
                    },
                    {
                        "id": 3,
                        "inbound": true,
                        "subver": "/Satoshi:26.0.0/"
                    }
                ],
                "error": null,
                "id": "quarkus-getpeerinfo"
            }
            """;

        mockAllRpcCalls(mockPeerInfoResponse);

        GlobalResponse response = rpcServices.getData();

        List<SubverStats> inboundStats = response.getSubverDistribution().inbound();
        
        SubverStats v27 = inboundStats.stream()
            .filter(s -> s.getServer().equals("/Satoshi:27.0.0/"))
            .findFirst()
            .orElseThrow();
        
        assertEquals(33.33, v27.getPercentage(), 0.01);
    }

    @SuppressWarnings("unchecked")
    private void mockAllRpcCalls(String peerInfoResponse) {
        String mockBlockchainResponse = """
            {
                "result": {"blocks": 870000, "chain": "main"},
                "error": null,
                "id": "quarkus-getblockchaininfo"
            }
            """;

        String mockNodeInfoResponse = """
            {
                "result": {"version": 270000, "subversion": "/Satoshi:27.0.0/"},
                "error": null,
                "id": "quarkus-getnetworkinfo"
            }
            """;

        String mockUptimeResponse = """
            {
                "result": 432000,
                "error": null,
                "id": "quarkus-uptime"
            }
            """;

        String mockBestBlockHashResponse = """
            {
                "result": "00000000000000000001abc",
                "error": null,
                "id": "quarkus-getbestblockhash"
            }
            """;

        String mockBlockInfoResponse = """
            {
                "result": {"time": 1733443200, "nTx": 2500},
                "error": null,
                "id": "quarkus-getblock"
            }
            """;

        when(rpcClient.executeRpcCall(any())).thenAnswer(invocation -> {
            var request = (java.util.Map<String, Object>) invocation.getArgument(0);
            String method = (String) request.get("method");
            
            return switch (method) {
                case "getpeerinfo" -> peerInfoResponse;
                case "getblockchaininfo" -> mockBlockchainResponse;
                case "getnetworkinfo" -> mockNodeInfoResponse;
                case "uptime" -> mockUptimeResponse;
                case "getbestblockhash" -> mockBestBlockHashResponse;
                case "getblock" -> mockBlockInfoResponse;
                default -> throw new IllegalArgumentException("Unexpected method: " + method);
            };
        });
    }
}
