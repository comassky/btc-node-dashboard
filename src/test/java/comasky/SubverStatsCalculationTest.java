package comasky;

import comasky.client.RpcClient;
import comasky.rpcClass.GlobalResponse;
import comasky.rpcClass.RpcServices;
import comasky.rpcClass.SubverStats;
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
    void testSubverDistribution_singleVersion() {
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

        GlobalResponse response = rpcServices.getData().await().indefinitely(); // Await the Uni

        assertNotNull(response.subverDistribution()); // Use record accessor
        List<SubverStats> inboundStats = response.subverDistribution().inbound(); // Use record accessor

        assertEquals(1, inboundStats.size());
        assertEquals("/Satoshi:27.0.0/", inboundStats.get(0).server()); // Use record accessor
        assertEquals(100.0, inboundStats.get(0).percentage()); // Use record accessor
    }

    @Test
    void testSubverDistribution_multipleVersions() {
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

        GlobalResponse response = rpcServices.getData().await().indefinitely(); // Await the Uni

        List<SubverStats> inboundStats = response.subverDistribution().inbound(); // Use record accessor

        assertEquals(3, inboundStats.size());

        SubverStats v27Stats = inboundStats.stream()
                .filter(s -> s.server().equals("/Satoshi:27.0.0/")) // Use record accessor
                .findFirst()
                .orElseThrow();

        assertEquals(50.0, v27Stats.percentage()); // Use record accessor
    }

    @Test
    void testSubverDistribution_withNullSubver() {
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

        GlobalResponse response = rpcServices.getData().await().indefinitely(); // Await the Uni

        List<SubverStats> inboundStats = response.subverDistribution().inbound(); // Use record accessor

        // Only 1 distinct subversion, but percentage is calculated on total peers (including null)
        // 2 peers with /Satoshi:27.0.0/ out of 3 total = 66.67%
        assertEquals(1, inboundStats.size());
        assertEquals("/Satoshi:27.0.0/", inboundStats.get(0).server()); // Use record accessor
        assertEquals(66.67, inboundStats.get(0).percentage(), 0.01); // Use record accessor
    }

    @Test
    void testSubverDistribution_inboundVsOutbound() {
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

        GlobalResponse response = rpcServices.getData().await().indefinitely(); // Await the Uni

        List<SubverStats> inboundStats = response.subverDistribution().inbound(); // Use record accessor
        List<SubverStats> outboundStats = response.subverDistribution().outbound(); // Use record accessor

        assertEquals(1, inboundStats.size());
        assertEquals(1, outboundStats.size());

        assertEquals("/Satoshi:27.0.0/", inboundStats.get(0).server()); // Use record accessor
        assertEquals(100.0, inboundStats.get(0).percentage()); // Use record accessor

        assertEquals("/Satoshi:26.0.0/", outboundStats.get(0).server()); // Use record accessor
        assertEquals(100.0, outboundStats.get(0).percentage()); // Use record accessor
    }

    @Test
    void testSubverDistribution_percentageCalculation() {
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

        GlobalResponse response = rpcServices.getData().await().indefinitely(); // Await the Uni

        List<SubverStats> inboundStats = response.subverDistribution().inbound(); // Use record accessor

        double totalPercentage = inboundStats.stream()
                .mapToDouble(SubverStats::percentage) // Use record accessor
                .sum();

        assertEquals(100.0, totalPercentage, 0.01);
    }

    @Test
    void testSubverDistribution_emptyPeers() {
        String mockPeerInfoResponse = """
            {
                "result": [],
                "error": null,
                "id": "quarkus-getpeerinfo"
            }
            """;

        mockAllRpcCalls(mockPeerInfoResponse);

        GlobalResponse response = rpcServices.getData().await().indefinitely(); // Await the Uni

        assertTrue(response.subverDistribution().inbound().isEmpty()); // Use record accessor
        assertTrue(response.subverDistribution().outbound().isEmpty()); // Use record accessor
    }

    @Test
    void testSubverDistribution_roundingPrecision() {
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

        GlobalResponse response = rpcServices.getData().await().indefinitely(); // Await the Uni

        List<SubverStats> inboundStats = response.subverDistribution().inbound(); // Use record accessor

        SubverStats v27 = inboundStats.stream()
                .filter(s -> s.server().equals("/Satoshi:27.0.0/")) // Use record accessor
                .findFirst()
                .orElseThrow();

        assertEquals(33.33, v27.percentage(), 0.01); // Use record accessor
    }

    @SuppressWarnings("unchecked")
    private void mockAllRpcCalls(String peerInfoResponse) {
        String mockBlockchainResponse = """
            {
                "result": {"blocks": 870000, "chain": "main", "verificationprogress": 0.99, "initialblockdownload": false},
                "error": null,
                "id": "quarkus-getblockchaininfo"
            }
            """;

        String mockNodeInfoResponse = """
            {
                "result": {"version": 270000, "subversion": "/Satoshi:27.0.0/", "protocolversion": 70016, "connections": 10, "networkactive": true},
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
                "result": {"hash": "00000000000000000001abc", "time": 1733443200, "nTx": 2500},
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