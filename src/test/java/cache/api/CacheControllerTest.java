package cache.api;

import cache.CacheConfiguration;
import cache.CacheItem;
import cache.CacheItems;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class CacheControllerTest {
    @Autowired
    private CacheItems cacheItems;

    @Autowired
    private CacheConfiguration cacheConfiguration;

    @Autowired
    private MockMvc mvc;

    @BeforeEach
    public void setUp() {
        this.cacheConfiguration.setNumberOfSlots(10000);
        this.cacheItems.clear();
    }

    @Test
    public void testShow() throws Exception {
        CacheItem item = new CacheItem();
        item.setId(11);
        item.setCreatedAt(LocalDateTime.now());
        item.setTtl(3600);
        this.cacheItems.put(3, item);

        ResultActions result = mvc.perform(MockMvcRequestBuilders.get("/object/3").contentType(MediaType.APPLICATION_JSON));
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", Matchers.is(11)));
    }

    @Test
    public void testShowWithNonExistentKey() throws Exception {
        CacheItem item = new CacheItem();
        item.setId(11);
        item.setCreatedAt(LocalDateTime.now());
        item.setTtl(3600);
        this.cacheItems.put(3, item);

        ResultActions result = mvc.perform(MockMvcRequestBuilders.get("/object/4").contentType(MediaType.APPLICATION_JSON));
        result.andExpect(status().isNotFound());
    }

    @Test
    public void testShowWithExpired() throws Exception {
        CacheItem item = new CacheItem();
        item.setId(11);
        item.setCreatedAt(LocalDateTime.of(2020, 1, 1, 0, 0));
        item.setTtl(3600);
        this.cacheItems.put(3, item);

        ResultActions result = mvc.perform(MockMvcRequestBuilders.get("/object/3").contentType(MediaType.APPLICATION_JSON));
        result.andExpect(status().isNotFound());
    }

    @Test
    public void testShowWithZeroTTL() throws Exception {
        CacheItem item = new CacheItem();
        item.setId(11);
        item.setCreatedAt(LocalDateTime.of(2020, 1, 1, 0, 0));
        item.setTtl(0);
        this.cacheItems.put(3, item);

        ResultActions result = mvc.perform(MockMvcRequestBuilders.get("/object/3").contentType(MediaType.APPLICATION_JSON));
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", Matchers.is(11)));
    }

    @Test
    public void testInsert() throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String today = LocalDateTime.now().format(formatter);

        ResultActions result = mvc.perform(MockMvcRequestBuilders.post("/object")
                .param("ttl", "500")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\": 12}"));
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", Matchers.is(12)));
        CacheItem item = this.cacheItems.get(0);
        Assertions.assertEquals(12, item.getId());
        Assertions.assertEquals(500, item.getTtl());
        Assertions.assertEquals(today, item.getCreatedAt().format(formatter));
    }

    @Test
    public void testInsertWithoutVacantKey() throws Exception {
        this.cacheConfiguration.setNumberOfSlots(3);
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

        ResultActions result = mvc.perform(MockMvcRequestBuilders.post("/object")
                .param("ttl", "500")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\": 22}"));
        result.andExpect(status().isInsufficientStorage());
    }

    @Test
    public void testUpdate() throws Exception {
        CacheItem item = new CacheItem();
        item.setId(11);
        item.setCreatedAt(LocalDateTime.of(2020, 1, 1, 0, 0));
        item.setTtl(3600);
        this.cacheItems.put(3, item);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String today = LocalDateTime.now().format(formatter);

        ResultActions result = mvc.perform(MockMvcRequestBuilders.put("/object/3")
                .param("ttl", "500")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\": 12}"));
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", Matchers.is(12)));
        item = this.cacheItems.get(3);
        Assertions.assertEquals(12, item.getId());
        Assertions.assertEquals(500, item.getTtl());
        Assertions.assertEquals(today, item.getCreatedAt().format(formatter));
    }

    @Test
    public void testUpdateWithNegativeKey() throws Exception {
        CacheItem item = new CacheItem();
        item.setId(11);
        item.setCreatedAt(LocalDateTime.of(2020, 1, 1, 0, 0));
        item.setTtl(3600);
        this.cacheItems.put(3, item);

        ResultActions result = mvc.perform(MockMvcRequestBuilders.put("/object/-1")
                .param("ttl", "500")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\": 12}"));
        result.andExpect(status().isInsufficientStorage());
    }

    @Test
    public void testUpdateWithKeyOverSlotNumber() throws Exception {
        CacheItem item = new CacheItem();
        item.setId(11);
        item.setCreatedAt(LocalDateTime.of(2020, 1, 1, 0, 0));
        item.setTtl(3600);
        this.cacheItems.put(3, item);

        ResultActions result = mvc.perform(MockMvcRequestBuilders.put("/object/10001")
                .param("ttl", "500")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\": 12}"));
        result.andExpect(status().isInsufficientStorage());
    }

    @Test
    public void testDestroy() throws Exception {
        CacheItem item = new CacheItem();
        item.setId(11);
        item.setCreatedAt(LocalDateTime.now());
        item.setTtl(3600);
        this.cacheItems.put(3, item);

        ResultActions result = mvc.perform(MockMvcRequestBuilders.delete("/object/3").contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk());
        item = this.cacheItems.get(3);
        Assertions.assertNull(item);
    }

    @Test
    public void testDestroyWithNonExistentKey() throws Exception {
        CacheItem item = new CacheItem();
        item.setId(11);
        item.setCreatedAt(LocalDateTime.now());
        item.setTtl(3600);
        this.cacheItems.put(3, item);

        ResultActions result = mvc.perform(MockMvcRequestBuilders.delete("/object/4").contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isNotFound());
    }

    @Test
    public void testDestroyWithExpired() throws Exception {
        CacheItem item = new CacheItem();
        item.setId(11);
        item.setCreatedAt(LocalDateTime.of(2020, 1, 1, 0, 0));
        item.setTtl(3600);
        this.cacheItems.put(3, item);

        ResultActions result = mvc.perform(MockMvcRequestBuilders.delete("/object/3").contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isNotFound());
    }
}
