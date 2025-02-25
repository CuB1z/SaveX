package es.daw01.savex.controller;

import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hibernate.engine.jdbc.BlobProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.daw01.savex.components.ControllerUtils;
import es.daw01.savex.model.Comment;
import es.daw01.savex.model.CommentDTO;
import es.daw01.savex.model.Post;
import es.daw01.savex.model.User;
import es.daw01.savex.service.CommentService;
import es.daw01.savex.service.PostService;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api")
public class RestPostsController {

    private final static String TEMPLATE_IMAGE_PATH = "static/assets/template_image.png";

    @Autowired
    private CommentService commentService;

    @Autowired
    private PostService postService;

    @Autowired
    private ControllerUtils controllerUtils;

    @GetMapping("/posts/{id}/banner")
    public ResponseEntity<Object> getPostBanner(@PathVariable long id) throws SQLException {
        Blob banner = null;

        Optional<Post> op = postService.findById(id);

        // If the post does not exist or the banner is null, return a 404
        if (!op.isPresent() || op.get().getBanner() == null) {
            Resource img = new ClassPathResource(TEMPLATE_IMAGE_PATH);
            try {
                banner = BlobProxy.generateProxy(img.getInputStream(), img.contentLength());
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .contentLength(banner.length())
                    .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                    .body(new InputStreamResource(banner.getBinaryStream()));
        }

        // Get the post banner if it exists and return it
        Post post = op.get();
        banner = post.getBanner();
        Resource resource = new InputStreamResource(banner.getBinaryStream());

        return ResponseEntity.ok()
                .contentLength(banner.length())
                .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                .body(resource);
    }

    @GetMapping("/posts/{id}/comments")
    public ResponseEntity<Map<String, Object>> getComments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "1") int size) {
        // Retrieve post and return 404 if it does not exist
        Optional<Post> op = postService.findById(id);
        if (op.isEmpty())
            return ResponseEntity.notFound().build();

        Post post = op.get();
        User currentUser = controllerUtils.getAuthenticatedUser();

        // If the post is private return 403
        if (!post.isPublic())
            return ResponseEntity.status(403).build();

        // Retrieve comments of the post paginated
        Page<Comment> commentPage = commentService.findByPostOrderByCreatedAtDesc(
                post,
                PageRequest.of(page, size));

        // Create comment DTO list
        List<CommentDTO> commentDTOs = commentService.getCommentsDTO(commentPage.getContent(), currentUser);

        // Generate response map
        Map<String, Object> response = new HashMap<>();
        response.put("comments", commentDTOs);
        response.put("currentPage", commentPage.getNumber());
        response.put("totalItems", commentPage.getTotalElements());
        response.put("totalPages", commentPage.getTotalPages());
        response.put("isLastPage", commentPage.isLast());

        return ResponseEntity.ok(response);
    }

}