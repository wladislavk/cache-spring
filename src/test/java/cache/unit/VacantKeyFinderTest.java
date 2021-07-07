package cache.unit;

import cache.CacheConfiguration;
import cache.CacheItem;
import cache.CacheItems;
import cache.VacantKeyFinder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

@ExtendWith(SpringExtension.class)
public class VacantKeyFinderTest {
    @Mock
    private CacheConfiguration cacheConfiguration;

    private int numberOfSlots = 3;

    private CacheItems cacheItems;

    @InjectMocks
    private VacantKeyFinder vacantKeyFinder;

    @BeforeEach
    public void setUp() {
        this.cacheItems = new CacheItems();

        CacheItem firstItem = new CacheItem();
        firstItem.setId(11);
        firstItem.setCreatedAt(LocalDateTime.of(2020, 3, 28, 0, 0));
        CacheItem secondItem = new CacheItem();
        secondItem.setId(12);
        secondItem.setCreatedAt(LocalDateTime.of(2021, 5, 3, 0, 0));
        CacheItem thirdItem = new CacheItem();
        thirdItem.setId(13);
        thirdItem.setCreatedAt(LocalDateTime.of(2020, 12, 19, 0, 0));
        this.cacheItems.put(0, firstItem);
        this.cacheItems.put(1, secondItem);
        this.cacheItems.put(2, thirdItem);

        ReflectionTestUtils.setField(this.vacantKeyFinder, "cacheItems", this.cacheItems);

        Mockito.when(this.cacheConfiguration.getNumberOfSlots()).thenAnswer(invocation -> this.numberOfSlots);
    }

    @Test
    public void testFindVacantKey() {
        this.numberOfSlots = 5;
        Mockito.when(this.cacheConfiguration.getEvictionPolicy())
                .thenReturn(CacheConfiguration.EvictionPolicies.REJECT);

        int key = this.vacantKeyFinder.findVacantKey();

        Assertions.assertEquals(3, key);
    }

    @Test
    public void testWithEvictionOldest() {
        Mockito.when(this.cacheConfiguration.getEvictionPolicy())
                .thenReturn(CacheConfiguration.EvictionPolicies.OLDEST_FIRST);

        int key = this.vacantKeyFinder.findVacantKey();

        Assertions.assertEquals(0, key);
    }

    @Test
    public void testWithEvictionOldestAndEmpty() {
        this.numberOfSlots = 0;
        this.cacheItems.clear();
        Mockito.when(this.cacheConfiguration.getEvictionPolicy())
                .thenReturn(CacheConfiguration.EvictionPolicies.OLDEST_FIRST);

        int key = this.vacantKeyFinder.findVacantKey();

        Assertions.assertEquals(-1, key);
    }

    @Test
    public void testWithEvictionNewest() {
        Mockito.when(this.cacheConfiguration.getEvictionPolicy())
                .thenReturn(CacheConfiguration.EvictionPolicies.NEWEST_FIRST);

        int key = this.vacantKeyFinder.findVacantKey();

        Assertions.assertEquals(1, key);
    }

    @Test
    public void testWithEvictionNewestAndEmpty() {
        this.numberOfSlots = 0;
        this.cacheItems.clear();
        Mockito.when(this.cacheConfiguration.getEvictionPolicy())
                .thenReturn(CacheConfiguration.EvictionPolicies.NEWEST_FIRST);

        int key = this.vacantKeyFinder.findVacantKey();

        Assertions.assertEquals(-1, key);
    }

    @Test
    public void testWithEvictionReject() {
        Mockito.when(this.cacheConfiguration.getEvictionPolicy())
                .thenReturn(CacheConfiguration.EvictionPolicies.REJECT);

        int key = this.vacantKeyFinder.findVacantKey();

        Assertions.assertEquals(-1, key);
    }
}
