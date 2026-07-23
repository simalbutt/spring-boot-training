package org.example.helloworld.News;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.NonNull;
import java.time.LocalDateTime;

@Getter
@Setter
public class NewsDto  {
        private Long id;
        private String reportedBy;
        @NotBlank
        @Size(max = 100)
        private String title;
        @NotBlank
        @Size(max = 100)
        private String content;
        private LocalDateTime createdDate;

        public static @NonNull NewsDto from (@NonNull News news){
            NewsDto dto = new NewsDto();
            dto.setId(news.getId());
            dto.setContent(news.getContent());
            dto.setCreatedDate(news.getCreatedDate());
            dto.setTitle(news.getTitle());
            dto.setReportedBy(news.getReportedBy());
            return dto;
        }
}
