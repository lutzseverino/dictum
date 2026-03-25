package dev.dictum.api.config;

import dev.dictum.api.content.store.FilesystemPostStore;
import dev.dictum.api.content.store.InMemoryPostStore;
import dev.dictum.api.content.store.PostStore;
import dev.dictum.api.site.store.FilesystemSiteSettingsStore;
import dev.dictum.api.site.store.InMemorySiteSettingsStore;
import dev.dictum.api.site.store.SiteSettingsStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ContentStoreConfiguration {

  @Bean
  PostStore postStore(DictumContentProperties contentProperties) {
    return switch (contentProperties.getRepository()) {
      case FILESYSTEM -> new FilesystemPostStore(filesystemContentRoot(contentProperties));
      case IN_MEMORY -> new InMemoryPostStore();
    };
  }

  @Bean
  SiteSettingsStore siteSettingsStore(DictumContentProperties contentProperties) {
    return switch (contentProperties.getRepository()) {
      case FILESYSTEM -> new FilesystemSiteSettingsStore(filesystemContentRoot(contentProperties));
      case IN_MEMORY -> new InMemorySiteSettingsStore();
    };
  }

  private FilesystemContentRoot filesystemContentRoot(DictumContentProperties contentProperties) {
    return FilesystemContentRoot.from(contentProperties.getRoot());
  }
}
