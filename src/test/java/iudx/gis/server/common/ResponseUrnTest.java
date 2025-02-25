package iudx.gis.server.common;

import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import iudx.gis.server.apiserver.util.RequestType;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(VertxExtension.class)
public class ResponseUrnTest {
    @ParameterizedTest
    @EnumSource
    public void test(ResponseUrn responseUrn, VertxTestContext testContext) {
        assertNotNull(responseUrn);
        testContext.completeNow();
    }
}
