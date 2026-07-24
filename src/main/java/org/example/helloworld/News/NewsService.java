package org.example.helloworld.News;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class NewsService {

    private final NewsRepository Newsrepository;

    public NewsService(NewsRepository NewsRepository) {
        this.Newsrepository = NewsRepository;
    }

    public Page<News> findAll(int page, int size) {
        if (size <= 0) {
            size = 1;
        }
        if (page < 0 || page > 99) {
            page = 0;
        }
        return Newsrepository.findAll(PageRequest.of(page, size));

    }

    public Optional<News> findById(Long id) {
        return Newsrepository.findById(id);
    }

    public NewsDto create(@NonNull NewsDto dto, String username) {
        News news = new News();
        news.setTitle(dto.getTitle());
        news.setContent(dto.getContent());
        news.setReportedBy(username);
        news.setCreatedDate(LocalDateTime.now());
        News savedNews = Newsrepository.save(news);
        return NewsDto.from(savedNews);
    }

    public NewsDto update(long id, @NonNull NewsDto news, @NonNull Authentication auth) {
        News new1 = Newsrepository.findById(id)
                .orElseThrow(() -> new RuntimeException("News not found"));

        boolean isEditor = auth.getAuthorities()
                .stream()
                .anyMatch(role -> Objects.equals(role.getAuthority(), "ROLE_EDITOR"));
        boolean isOwner = new1.getReportedBy()
                .equals(auth.getName());
        if (!isEditor && !isOwner) {
            throw new AccessDeniedException(
                    "You can only update your own news"
            );
        }

        new1.setTitle(news.getTitle());
        new1.setContent(news.getContent());
        Newsrepository.save(new1);
        return NewsDto.from(new1);
    }

    public void deleteById(Long id) {
        Newsrepository.deleteById(id);
    }

    @Async
    public void report()
    {
        for(News news: Newsrepository.findAll()){
            log.info("News: {}, {}, {}",news.getTitle(),news.getContent(),news.getReportedBy());
        }
    }

    @Scheduled(fixedRate = 5000)
    public void printMessage() {
        log.info("Scheduler executed at {}", LocalDateTime.now());
    }
}


