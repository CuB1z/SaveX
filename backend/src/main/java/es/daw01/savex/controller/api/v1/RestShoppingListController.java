package es.daw01.savex.controller.api.v1;

import java.net.URI;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import es.daw01.savex.DTOs.ApiResponseDTO;
import es.daw01.savex.DTOs.PaginatedDTO;
import es.daw01.savex.DTOs.lists.CreateListRequest;
import es.daw01.savex.DTOs.lists.ShoppingListDTO;
import es.daw01.savex.DTOs.lists.SimpleShoppingListDTO;
import es.daw01.savex.service.ShoppingListService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/v1/lists")
public class RestShoppingListController {

    @Autowired
    private ShoppingListService shoppingListService;

    @Operation(summary = "Get all lists")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lists retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PaginatedDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content
        )
    })
    @GetMapping({ "", "/" })
    public ResponseEntity<Object> getUserLists(
        @PageableDefault(page = 0, size = 5) Pageable pageable
    ) {
        try{
            PaginatedDTO<SimpleShoppingListDTO> lists = shoppingListService.retrieveUserLists(pageable);
            return ApiResponseDTO.ok(lists);
        } catch (Exception e) {
            return ApiResponseDTO.error("Error getting lists");
        }
    }

    @Operation(summary = "Create a new list")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "List created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SimpleShoppingListDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content
        )
    })
    @PostMapping({ "", "/" })
    public ResponseEntity<Object> newList(
        @RequestBody CreateListRequest request
    ) {
        try {
            SimpleShoppingListDTO list = shoppingListService.createShoppingList(request);
            URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(list.id()).toUri();
            return ApiResponseDTO.ok(list, location, 201);
        } catch (Exception e) {
            return ApiResponseDTO.error("Error creating list");
        }
    }

    @Operation(summary = "Add a product to a list")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Product added successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ShoppingListDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "List not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content
        )
    })
    @PostMapping("/{id}/product/{productId}")
    public ResponseEntity<Object> addProductToList(
        @PathVariable Long id,
        @PathVariable String productId
    ) {
        try {
            ShoppingListDTO list = shoppingListService.addProductToList(id, productId);
            URI location = ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/v1/lists/{id}").buildAndExpand(list.id()).toUri();
            return ApiResponseDTO.ok(list, location, 201);
        } catch (NoSuchElementException e) {
            return ApiResponseDTO.error("List not found", 404);
        } catch (Exception e) {
            return ApiResponseDTO.error(e.getMessage());
        }
    }


    @Operation(summary = "Remove a product from a list")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Product removed successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SimpleShoppingListDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "List not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content
        )
    })
    @DeleteMapping("/{id}/product/{productId}")
    public ResponseEntity<Object> removeProductFromList(
        @PathVariable Long id,
        @PathVariable String productId
    ) {
        try {
            SimpleShoppingListDTO shoppingList = shoppingListService.removeProductFromList(id, productId);
            return ApiResponseDTO.ok(shoppingList);
        } catch (NoSuchElementException e) {
            return ApiResponseDTO.error("List not found", 404);
        } catch (RuntimeException e) {
            return ApiResponseDTO.error(e.getMessage());
        } catch (Exception e) {
            return ApiResponseDTO.error("Error removing product from list");
        }
    }

    @Operation(summary = "Remove a list")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "List removed successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ShoppingListDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "List not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content
        )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> removeList(@PathVariable Long id) {
        try {
            ShoppingListDTO list = shoppingListService.deleteById(id);
            return ApiResponseDTO.ok(list);
        } catch (NoSuchElementException e) {
            return ApiResponseDTO.error("List not found", 404);
        } catch (Exception e) {
            return ApiResponseDTO.error("Error removing list");
        }
    }

    @Operation(summary = "Get a list")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "List retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ShoppingListDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "List not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<Object> getShoppingList(@PathVariable long id) {
        try {
            ShoppingListDTO shoppingList = shoppingListService.getListById(id);
            return ApiResponseDTO.ok(shoppingList);
        } catch (NoSuchElementException e) {
            return ApiResponseDTO.error("List not found", 404);
        } catch (Exception e) {
            return ApiResponseDTO.error("Error getting list");
        }
    }

    @Operation(summary = "Update a list")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "List updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ShoppingListDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "List not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content
        )
    })
    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateList(
        @PathVariable Long id,
        @RequestBody CreateListRequest request
    ) {
        try {
            ShoppingListDTO shoppingList = shoppingListService.updateList(id, request);
            return ApiResponseDTO.ok(shoppingList);
        } catch (NoSuchElementException e) {
            return ApiResponseDTO.error("List not found", 404);            
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponseDTO.error("Error updating list: " + e.getMessage());
        }
    }

}
