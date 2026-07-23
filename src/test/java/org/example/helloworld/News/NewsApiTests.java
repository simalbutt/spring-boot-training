package org.example.helloworld.News;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
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



    private @NonNull BearerTokenAuthentication reporterAuthentication() {

        DefaultOAuth2AuthenticatedPrincipal principal =
                new DefaultOAuth2AuthenticatedPrincipal(
                        "reporter",
                        Map.of(
                                "sub", "reporter",
                                "scope", "ROLE_REPORTER"
                        ),
                        List.of(
                                new SimpleGrantedAuthority("ROLE_REPORTER")
                        )
                );

        OAuth2AccessToken fakeToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "fake-reporter-token",
                null,
                null
        );

        return new BearerTokenAuthentication(
                principal,
                fakeToken,
                principal.getAuthorities()
        );
    }



    private BearerTokenAuthentication editorAuthentication() {

        DefaultOAuth2AuthenticatedPrincipal principal =
                new DefaultOAuth2AuthenticatedPrincipal(
                        "editor",
                        Map.of(
                                "sub", "editor",
                                "scope", "ROLE_EDITOR"
                        ),
                        List.of(
                                new SimpleGrantedAuthority("ROLE_EDITOR")
                        )
                );

        OAuth2AccessToken fakeToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "fake-editor-token",
                null,
                null
        );

        return new BearerTokenAuthentication(
                principal,
                fakeToken,
                principal.getAuthorities()
        );
    }




    @Test
    void shouldReturnAllNews() throws Exception {

        mockMvc.perform(get("/api/v1/news"))
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


        News saved = newsRepository.save(news);


        mockMvc.perform(get("/api/v1/news/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
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


        MvcResult result =
                mockMvc.perform(post("/api/v1/news")
                                .with(authentication(reporterAuthentication()))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.title").value("Today"))
                        .andExpect(jsonPath("$.content").value("Today is Monday"))
                        .andExpect(jsonPath("$.reportedBy").value("reporter"))
                        .andReturn();



        ObjectMapper mapper = new ObjectMapper();

        JsonNode json =
                mapper.readTree(result.getResponse().getContentAsString());


        Long id = json.get("id").asLong();


        assertTrue(newsRepository.findById(id).isPresent());
    }





    @Test
    void shouldUpdateNews() throws Exception {


        News news = new News();

        news.setTitle("Old Title");
        news.setContent("Old Content");
        news.setCreatedDate(LocalDateTime.now());
        news.setReportedBy("reporter");
        News saved = newsRepository.save(news);



        String requestBody = """
        {
            "title": "New Title",
            "content": "New Content",
            "createdDate": "2026-07-13T06:05:28.539"
        }
        """;



        mockMvc.perform(
                        put("/api/v1/news/" + saved.getId())
                                .with(authentication(reporterAuthentication()))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.title").value("New Title"))
                .andExpect(jsonPath("$.content").value("New Content"));

    }





    @Test
    void shouldDeleteNews() throws Exception {


        News news = new News();

        news.setTitle("Delete");
        news.setContent("Delete Content");
        news.setCreatedDate(LocalDateTime.now());


        News saved = newsRepository.save(news);



        mockMvc.perform(
                        delete("/api/v1/news/" + saved.getId())
                                .with(authentication(editorAuthentication()))
                                .with(csrf())
                )
                .andExpect(status().isOk());



        assertFalse(
                newsRepository.findById(saved.getId()).isPresent()
        );

    }

}
