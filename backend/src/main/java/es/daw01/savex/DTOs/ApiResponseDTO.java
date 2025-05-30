package es.daw01.savex.DTOs;

import java.net.URI;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import es.daw01.savex.components.ApiError;


/**
 * ApiResponseDTO
 * 
 * Represents a response from the API:
 * 
 * <hr/>
 * <b>Successful responses:</b>
 * <ul>
 *  <li><pre>ApiResponseDTO.ok(Object data)</pre> - 200 OK</li>
 *  <li><pre>ApiResponseDTO.ok(Object data, int code)</pre> - Custom code</li>
 *  <li><pre>ApiResponseDTO.ok(Object data, URI location)</pre> - 201 Created</li>
 *  <li><pre>ApiResponseDTO.ok(Object data, URI location, int code)</pre> - Custom code</li>
 *  <li><pre>ApiResponseDTO.ok(Resource data)</pre> - 200 OK with image</li>
 * </ul>
 * 
 * <b>Error responses:</b>
 * <ul>
 *  <li><pre>ApiResponseDTO.error(String error)</pre> - 500 Internal Server Error</li>
 *  <li><pre>ApiResponseDTO.error(String error, int code)</pre> - Custom code</li>
 *  <li><pre>ApiResponseDTO.error(String error, Object data)</pre> - 500 Internal Server Error with data</li>
 *  <li><pre>ApiResponseDTO.error(String error, int code, Object data)</pre> - Custom code with data</li>
 *  <li><pre>ApiResponseDTO.error(ApiResponseDTO<Object> response)</pre> - 500 Internal Server Error with response</li>
 *  <li><pre>ApiResponseDTO.error(ApiResponseDTO<Object> response, int code)</pre> - Custom code with response</li>
 * </ul>
 * 
 * <hr/>
 * 
 * @param <T> The type of the data
 */
public class ApiResponseDTO<T> {
    private T data;
    private ApiError error;

    // ====================[ Default Constructors ]====================

    public ApiResponseDTO() {
        this.data = null;
        this.error = null;
    }

    // ====================[ Data constructors ]====================

    public ApiResponseDTO(T data) {
        this.data = data;
        this.error = null;
    }

    // ====================[ Error constructors ]====================
    public ApiResponseDTO(ApiError error) {
        this.data = null;
        this.error = error;
    }


    public ApiResponseDTO(ApiError error, T data) {
        this.data = data;
        this.error = error;
    }

    // ====================[ Methods ]====================

    public static ResponseEntity<Object> ok() {
        return ResponseEntity.ok(new ApiResponseDTO<Object>());
    }

    public static ResponseEntity<Object> ok(Object data) {
        return ResponseEntity.ok(new ApiResponseDTO<Object>(data));
    }

    public static ResponseEntity<Object> ok(int code) {
        return ApiResponseDTO.ok(null, code);
    }

    public static ResponseEntity<Object> ok(Resource data) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/png")
                .body(data);
    }

    public static ResponseEntity<Object> ok(Object data, int code) {
        if (data == null) {
            return ResponseEntity.status(code).body(new ApiResponseDTO<Object>());
        }
        return ResponseEntity.status(code).body(new ApiResponseDTO<Object>(data));
    }

    public static ResponseEntity<Object> ok(Object data, URI location) {
        return ResponseEntity.created(location).body(new ApiResponseDTO<Object>(data));
    }

    public static ResponseEntity<Object> ok(Object data, URI location, int code) {
        return ResponseEntity.status(code).location(location).body(new ApiResponseDTO<Object>(data));
    }

    public static ResponseEntity<Object> error(String error) {
        return ResponseEntity.status(500).body(new ApiResponseDTO<Object>(new ApiError(error)));
    }

    public static ResponseEntity<Object> error(String error, int code) {
        return ResponseEntity.status(code).body(new ApiResponseDTO<Object>(new ApiError(error), code));
    }

    public static ResponseEntity<Object> error(String error, Object data) {
        return ResponseEntity.status(500).body(new ApiResponseDTO<Object>(new ApiError(error), data));
    }

    public static ResponseEntity<Object> error(String error, int code, Object data) {
        return ResponseEntity.status(code).body(new ApiResponseDTO<Object>(new ApiError(error), data));
    }


    // -------------------[ Non static ]-------------------

    public static ResponseEntity<Object> error(ApiResponseDTO<Object> response) {
        return ResponseEntity.status(500).body(response);
    }

    public static ResponseEntity<Object> error(ApiResponseDTO<Object> response, int code) {
        return ResponseEntity.status(code).body(response);
    }

    // ====================[ Getters ]====================

    public T getData() {
        return data;
    }

    public ApiError getError() {
        return error;
    }

    // ====================[ Setters ]====================

    public void setData(T data) {
        this.data = data;
    }

    public void setError(ApiError error) {
        this.error = error;
    }
}
