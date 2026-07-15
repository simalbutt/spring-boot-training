package org.example.helloworld.News;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NewsMediaService {

    private final NewsMediaRepository mediaRepository;
    private final NewsRepository newsRepository;

    public NewsMediaService(NewsMediaRepository mediaRepository,
                            NewsRepository newsRepository) {
        this.mediaRepository = mediaRepository;
        this.newsRepository = newsRepository;
    }

    public NewsMedia addMedia(Long newsId, @NonNull NewsMedia media) {

        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new RuntimeException("News not found"));

        media.setNews(news);

        return mediaRepository.save(media);
    }

    public List<NewsMedia> getMediaByNews(Long newsId) {
        return mediaRepository.findByNewsId(newsId);
    }

    public void deleteMedia(Long mediaId) {

        if (!mediaRepository.existsById(mediaId)) {
            throw new RuntimeException("Media not found");
        }

        mediaRepository.deleteById(mediaId);
    }

}