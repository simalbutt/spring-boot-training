package org.example.helloworld.News;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class NewsMediaController {

    private final NewsMediaService mediaService;

    public NewsMediaController(NewsMediaService mediaService) {
        this.mediaService = mediaService;
    }

    @PostMapping("/news/{newsId}/media")
    public ResponseEntity<NewsMedia> addMedia(
            @PathVariable Long newsId,
            @RequestBody NewsMedia media) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mediaService.addMedia(newsId, media));
    }

    @GetMapping("/news/{newsId}/media")
    public ResponseEntity<List<NewsMedia>> getMedia(
            @PathVariable Long newsId) {

        return ResponseEntity.ok(mediaService.getMediaByNews(newsId));
    }

    @DeleteMapping("/media/{mediaId}")
    public ResponseEntity<Void> deleteMedia(
            @PathVariable Long mediaId) {

        mediaService.deleteMedia(mediaId);

        return ResponseEntity.noContent().build();
    }

}