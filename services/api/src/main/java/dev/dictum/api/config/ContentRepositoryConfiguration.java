package dev.dictum.api.config;

import dev.dictum.api.content.repository.FilesystemPostRepository;
import dev.dictum.api.content.repository.InMemoryPostRepository;
import dev.dictum.api.content.repository.PostRepository;
import dev.dictum.api.site.repository.FilesystemSiteSettingsRepository;
import dev.dictum.api.site.repository.InMemorySiteSettingsRepository;
import dev.dictum.api.site.repository.SiteSettingsRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ContentRepositoryConfiguration {

  @Bean
  PostRepository postRepository(DictumContentProperties contentProperties) {
    return switch (contentProperties.getRepository()) {
      case FILESYSTEM -> new FilesystemPostRepository(filesystemContentRoot(contentProperties));
      case IN_MEMORY -> new InMemoryPostRepository();
    };
  }

  @Bean
  SiteSettingsRepository siteSettingsRepository(DictumContentProperties contentProperties) {
    return switch (contentProperties.getRepository()) {
      case FILESYSTEM ->
          new FilesystemSiteSettingsRepository(filesystemContentRoot(contentProperties));
      case IN_MEMORY -> new InMemorySiteSettingsRepository();
    };
  }

  private FilesystemContentRoot filesystemContentRoot(DictumContentProperties contentProperties) {
    return FilesystemContentRoot.from(contentProperties.getRoot());
  }
}
