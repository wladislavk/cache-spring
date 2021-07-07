package cache.unit;

import cache.CacheConfiguration;
import cache.CacheItem;
import cache.ElementAdditionHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@ExtendWith(SpringExtension.class)
public class ElementAdditionHandlerTest {
    private static int DEFAULT_TTL = 30;

    @Mock
    private CacheConfiguration cacheConfiguration;

    @InjectMocks
    private ElementAdditionHandler elementAdditionHandler;

    @BeforeEach
    public void setUp() {
        Mockito.when(this.cacheConfiguration.getDefaultTtl()).thenReturn(DEFAULT_TTL);
    }

    @Test
    public void testWithTTL() {
        int id = 11;
        int ttl = 40;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String today = LocalDateTime.now().format(formatter);

        CacheItem item = this.elementAdditionHandler.addElement(id, java.util.Optional.of(ttl));

        Assertions.assertEquals(id, item.getId());
        Assertions.assertEquals(today, item.getCreatedAt().format(formatter));
        Assertions.assertEquals(ttl, item.getTtl());
    }

    @Test
    public void testWithoutTTL() {
        int id = 11;

        CacheItem item = this.elementAdditionHandler.addElement(id, java.util.Optional.empty());

        Assertions.assertEquals(id, item.getId());
        Assertions.assertEquals(DEFAULT_TTL, item.getTtl());
    }
}
