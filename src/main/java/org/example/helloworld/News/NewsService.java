package org.example.helloworld.News;

import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class NewsService {

    private final NewsRepository Newsrepository;

    public NewsService(NewsRepository NewsRepository) {
        this.Newsrepository = NewsRepository;
    }

    public Page<News> FindAll(int page, int size) {
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

    public News create(@NonNull News news, String username) {
        news.setReportedBy(username);
        return Newsrepository.save(news);
    }

    public News update(long id, @NonNull News news, @NonNull Authentication auth) {
        News new1 = Newsrepository.findById(id)
                .orElseThrow(() -> new RuntimeException("News not found"));

        boolean isEditor = auth.getAuthorities()
                .stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_EDITOR"));
        boolean isOwner = new1.getReportedBy()
                .equals(auth.getName());
        if (!isEditor && !isOwner) {
            throw new AccessDeniedException(
                    "You can only update your own news"
            );
        }

        new1.setTitle(news.getTitle());
        new1.setContent(news.getContent());

        return Newsrepository.save(new1);
    }

    public void deleteById(Long id) {
        Newsrepository.deleteById(id);
    }

}
