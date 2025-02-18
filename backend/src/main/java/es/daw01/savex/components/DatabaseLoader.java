package es.daw01.savex.components;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import es.daw01.savex.model.Post;
import es.daw01.savex.model.User;
import es.daw01.savex.model.UserType;
import es.daw01.savex.repository.PostRepository;
import es.daw01.savex.repository.UserRepository;
import es.daw01.savex.service.MarkdownService;
import jakarta.annotation.PostConstruct;

@Component
public class DatabaseLoader {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseLoader.class);

    @Autowired
    private MarkdownService markdownService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    private void init() {
        this.initUsers();
        this.initPosts();
    }

    /**
     * Load default users into the database
    */
    private void initUsers() {
        // Load default user
        if (
            !userRepository.existsByUsername("userDefault") &&
            !userRepository.existsByEmail("userEmail@gmail.com")
        ) {
            userRepository.save(
                new User(
                    "userEmail@gmail.com",
                    "userDefault",
                    "User",
                    passwordEncoder.encode("pass"),
                    null,
                    UserType.USER
                )
            );
        }

        // Load default admin
        if (
            !userRepository.existsByUsername("adminDefault") &&
            !userRepository.existsByEmail("adminEmail@gmail.com")
        ) {
            userRepository.save(
                new User(
                    "adminEmail@gmail.com",
                    "adminDefault",
                    "Admin",
                    passwordEncoder.encode("admin"),
                    null,
                    UserType.ADMIN
                )
            );
        }
    }

    /**
     * Load default posts into the database
    */
    private void initPosts() {
        Path postsDir = Paths.get("src/main/resources/static/assets/posts");

        // Get all markdown files in the posts directory
        List<Path> paths = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(postsDir)) {
            paths = stream.filter(path -> Files.isRegularFile(path)).collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Load default posts by reading the markdown files
        for (Path path : paths) {
            String markdown = null;
            Map<String, List<String>> yamlFrontMatter = null;
    
            try {
                markdown = Files.readString(path);
                yamlFrontMatter = markdownService.extractYamlFrontMatter(markdown);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (postRepository.existsByTitle(yamlFrontMatter.get("title").get(0))) {
                DatabaseLoader.logger.info("Post already exists, skipping...");
                continue;
            }

            // Save the post if the markdown content is not empty
            if (markdown != null && !markdown.isEmpty()) {
                postRepository.save(new Post(
                    markdown,
                    yamlFrontMatter
                ));
            }
        }
    }
}
