package org.example.helloworld.News;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/news")
public class Controller {

    private  final NewsService newsService;
    public Controller(NewsService newsService) {
        this.newsService = newsService;
    }

    @GetMapping
    public Map<String,Object> FindAll(
            @RequestParam(required = false, defaultValue = "0" ) int page,
            @RequestParam(required = false, defaultValue = "5") int size)
    {
        Page<News> news=newsService.FindAll(page,size);

        return Map.of("news",news.getContent(),"page",news.getNumber(),"size",news.getSize());
    }

    @GetMapping("/{id}")
    public ResponseEntity<News> getNewsById(@PathVariable("id") Long id) {
        Optional<News> value = newsService.findById(id);

        if (value.isPresent()) {
            return ResponseEntity.ok(value.get());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<News> create(@RequestBody News news) {
        return ResponseEntity.status(HttpStatus.CREATED).body(newsService.create(news));
    }

    @PutMapping("/{id}")
    public ResponseEntity<News> update(@PathVariable("id") long id ,@RequestBody News news) {
        return ResponseEntity.ok(newsService.update(id,news));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<News> delete(@PathVariable long id) {
        newsService.deleteById(id);
        return ResponseEntity.ok().build();
    }

}
