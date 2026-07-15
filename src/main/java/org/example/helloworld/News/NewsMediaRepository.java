package org.example.helloworld.News;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NewsMediaRepository extends JpaRepository<NewsMedia, Long> {
    List<NewsMedia> findByNewsId(Long newsId);

}
