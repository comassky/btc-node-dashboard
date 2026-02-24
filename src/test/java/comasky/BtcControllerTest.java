package comasky;

import comasky.api.BitcoinApiController;
import comasky.rpcClass.RpcServices;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
class BtcControllerTest {

    @InjectMock
    RpcServices rpcServices;


    @Test
    void testInstantiationWithMock() {
        RpcServices rpcServices = org.mockito.Mockito.mock(comasky.rpcClass.RpcServices.class);
        BitcoinApiController controller = new BitcoinApiController(rpcServices);
        assertNotNull(controller);
    }
}
