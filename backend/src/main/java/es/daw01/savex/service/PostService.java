package es.daw01.savex.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import es.daw01.savex.DTOs.PaginatedDTO;
import es.daw01.savex.DTOs.posts.CreatePostRequest;
import es.daw01.savex.DTOs.posts.PostDTO;
import es.daw01.savex.DTOs.posts.PostMapper;
import es.daw01.savex.model.Post;
import es.daw01.savex.model.VisibilityType;
import es.daw01.savex.repository.PostRepository;
import es.daw01.savex.utils.ImageUtils;

@Service
public class PostService {

    @Autowired
    private MarkdownService markdownService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostMapper postMapper;

    /**
     * Retrieves the banner image of a post
     * 
     * @param id The id of the post
     * @return The banner image of the post or null if it doesn't exist
     */
    public Resource getPostBanner(long id) throws SQLException {
        Post post = postRepository.findById(id).orElseThrow();

        if (post.getBanner() == null) {
            throw new NoSuchElementException("Post doesn't have a banner");
        }

        return ImageUtils.blobToResource(post.getBanner());
    }

    /**
     * Retrieves the content of a post parsed to HTML
     * 
     * @param id The id of the post
     * @return The content of the post
     */
    public String getPostContent(long id) {
        Post post = postRepository.findById(id).orElseThrow();
        return markdownService.renderMarkdown(post.getContent());
    }

    /**
     * Retrieves a post by its id
     * 
     * @param id The id of the post
     * @return The post with the given id
     */
    public PostDTO getPost(long id) {
        return postMapper.toDTO(postRepository.findById(id).orElseThrow());
    }

    /**
     * Saves a post in the database
     * 
     * @param post The post to save
     */
    public void save(Post post) {
        postRepository.save(post);
    }

    /**
     * Saves a post in the database
     * 
     * @param post   The post to save
     * @param banner The banner image of the post
     */
    public void save(Post post, MultipartFile banner) {
        try {
            post.saveImage(banner);
            postRepository.save(post);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Finds all posts in the database
     * 
     * @return A list of all posts in the database
     */
    public List<Post> findAll() {
        return postRepository.findAll();
    }

    /**
     * Finds a post by its id
     * 
     * @param id The id of the post to find
     * @return The post with the given id, or null if it doesn't exist
     */
    public Optional<Post> findById(long id) {
        return postRepository.findById(id);
    }

    /**
     * Finds all posts in the database, ordered by creation date and filtered by
     * visibility
     * 
     * @param visibility The visibility of the posts to return
     * @param pageable   The page to return
     * @return A page of posts ordered by creation date
     */
    public Page<Post> findByVisibilityOrderByCreatedAtDesc(VisibilityType visibility, Pageable pageable) {
        return postRepository.findByVisibilityOrderByCreatedAtDesc(visibility, pageable);
    }

    /**
     * Finds all posts in the database, ordered by creation date
     * 
     * @param pageable The page to return
     * @return A paginated DTO of posts
     */
    public PaginatedDTO<PostDTO> retrievePosts(Pageable pageable) {
        // Retrieve posts paginated
        Page<Post> postPage = this.findByVisibilityOrderByCreatedAtDesc(VisibilityType.PUBLIC, pageable);

        // Create post DTO list
        List<PostDTO> postDTOList = this.postMapper.toDTOs(postPage.getContent());

        // Generate response map
        return new PaginatedDTO<PostDTO>(
            postDTOList,
            postPage.getNumber(),
            postPage.getTotalPages(),
            postPage.getTotalElements(),
            postPage.getSize(),
            postPage.isLast()
        );
    }

    /**
     * Deletes a post by its id
     * 
     * @param id The id of the post to delete
     * @return The deleted post
     */
    public PostDTO deleteById(long id) {
        PostDTO post = getPost(id);
        postRepository.deleteById(id);
        return post;
    }

    /**
     * Updates a post from a given request
     *
     * @param id The id of the post to update
     * @param postRequest The request with the new post data
     * @return The updated post
     */
    public PostDTO updatePost(Long id, CreatePostRequest postRequest) throws IOException {
        return updatePost(id, postRequest, null);
    }

    public PostDTO updatePost(Long id, CreatePostRequest postRequest, MultipartFile banner) throws IOException {
        Post toUpdatePost = postRepository.findById(id).orElseThrow();
        Post reqPost = postMapper.toDomain(postRequest);

        toUpdatePost.updatePost(reqPost);
        if (banner != null) toUpdatePost.saveImage(banner);

        return postMapper.toDTO(postRepository.save(toUpdatePost));
    }

    /**
     * Creates a post from a given request
     *
     * @param postRequest The request with the new post data
     * @param banner The banner image of the post
     * @return The created post
    */
    public PostDTO createPost(CreatePostRequest postRequest) throws IOException {
        Post post = new Post();
        postMapper.createPostFromRequest(postRequest, post);
        return postMapper.toDTO(postRepository.save(post));
    }

    /**
     * Updates the banner of a post
     *
     * @param id The id of the post to update
     * @param banner The new banner image
     * @return The updated post
     */
    public PostDTO updatePostBanner(long id, MultipartFile banner) throws IOException {
        Post post = postRepository.findById(id).orElseThrow();
        post.saveImage(banner);
        return postMapper.toDTO(postRepository.save(post));
    }

    /**
     * Deletes the banner of a post
     * 
     * @param id The id of the post to delete the banner from
     * @return The updated post
     */
    public PostDTO deletePostBanner(long id) {
        Post post = postRepository.findById(id).orElseThrow();
        post.removeBanner();
        return postMapper.toDTO(postRepository.save(post));
    }
}
