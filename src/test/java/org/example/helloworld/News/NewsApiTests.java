package org.example.helloworld.News;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.time.LocalDateTime;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class NewsApiTests {
        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private NewsRepository newsRepository;

        @BeforeEach
        void setUp() {
            newsRepository.deleteAll();

            News news1 = new News();
            news1.setTitle("News 1");
            news1.setContent("Content 1");
            news1.setCreatedDate(LocalDateTime.now());

            News news2 = new News();
            news2.setTitle("News 2");
            news2.setContent("Content 2");
            news2.setCreatedDate(LocalDateTime.now());

            newsRepository.save(news1);
            newsRepository.save(news2);
        }

        @Test
        void shouldReturnAllNews() throws Exception {

            mockMvc.perform(get("/api/v1/news")
                            .param("page", "0")
                            .param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.page").value(0))
                    .andExpect(jsonPath("$.size").value(5))
                    .andExpect(jsonPath("$.news.length()").value(2))
                    .andExpect(jsonPath("$.news[0].title").value("News 1"))
                    .andExpect(jsonPath("$.news[1].title").value("News 2"));
        }

    @Test
    void shouldReturnNewsById() throws Exception {

        News news = new News();
        news.setTitle("Today");
        news.setContent("Today is Monday");
        news.setCreatedDate(LocalDateTime.now());

        News savedNews = newsRepository.save(news);

        Long newsId = savedNews.getId();

        mockMvc.perform(get("/api/v1/news/" + newsId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(Matchers.notNullValue()))
                .andExpect(jsonPath("$.id").value(newsId))
                .andExpect(jsonPath("$.title").value("Today"))
                .andExpect(jsonPath("$.content").value("Today is Monday"));
    }

    @Test
    void shouldCreateNews() throws Exception {

        String requestBody = """
        {
            "title": "Today",
            "content": "Today is Monday",
            "createdDate": "2026-07-13T06:05:28.539"
        }
        """;

        MvcResult result = mockMvc.perform(post("/api/v1/news")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(Matchers.notNullValue()))
                .andExpect(jsonPath("$.title").value("Today"))
                .andExpect(jsonPath("$.content").value("Today is Monday"))
                .andReturn();


        String response = result.getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();

        JsonNode json = mapper.readTree(response);

        Long createdId = json.get("id").asLong();


        assertTrue(newsRepository.findById(createdId).isPresent());
    }

    @Test
    void shouldUpdateNews() throws Exception {

        News news = new News();
        news.setId(1L);
        news.setTitle("Old Title");
        news.setContent("Old Content");
        news.setCreatedDate(LocalDateTime.now());

        newsRepository.save(news);

        String requestBody = """
                {
                    "title": "New Title",
                    "content": "New Content",
                    "createdDate": "2026-07-13T06:05:28.539"
                }
                """;

        mockMvc.perform(put("/api/v1/news/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("New Title"))
                .andExpect(jsonPath("$.content").value("New Content"));
    }

    @Test
    void shouldDeleteNews() throws Exception {

        News news = new News();
        news.setTitle("Today");
        news.setContent("Today is Monday");
        news.setCreatedDate(LocalDateTime.now());

        News savedNews = newsRepository.save(news);

        Long newsId = savedNews.getId();

        mockMvc.perform(delete("/api/v1/news/" + newsId))
                .andExpect(status().isOk());

        assertFalse(newsRepository.findById(newsId).isPresent());
    }
}
