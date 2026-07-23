package org.example.helloworld.News;

import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/news")
public class Controller {

    private final NewsService newsService;

    public Controller(NewsService newsService) {
        this.newsService = newsService;
    }

    @GetMapping
    @PermitAll
    public Map<String, Object> findAll(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "5") int size) {
        Page<NewsDto> news = newsService.findAll(page, size)
                .map(NewsDto::from);

        return Map.of("news", news.getContent(), "page", news.getNumber(), "size", news.getSize());
    }

    @GetMapping("/{id}")
    @PermitAll
    public ResponseEntity<NewsDto> getNewsById(@PathVariable("id") Long id) {
        return newsService.findById(id)
                .map(news -> ResponseEntity.ok(NewsDto.from(news)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<NewsDto> create(
            @Valid @RequestBody NewsDto dto,
            @NonNull Authentication auth
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(newsService.create(dto, auth.getName()));
    }
    @PreAuthorize("hasAnyRole('REPORTER','EDITOR')")
    @PutMapping("/{id}")
    public ResponseEntity<NewsDto> update(@PathVariable("id") long id,
                                       @RequestBody NewsDto dto, Authentication auth) {
        return ResponseEntity.ok(newsService.update(id, dto, auth));
    }

    @PreAuthorize("hasRole('EDITOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<NewsDto> delete(@PathVariable long id) {
        newsService.deleteById(id);
        return ResponseEntity.ok().build();
    }

}
