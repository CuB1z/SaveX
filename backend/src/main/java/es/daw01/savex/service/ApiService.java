package es.daw01.savex.service;

import org.springframework.web.client.RestTemplate;

import es.daw01.savex.DTOs.ProductDTO;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ApiService {
    private final String API_BASE_URL = "https://market-pricings-server.vercel.app/api/v1/markets/";
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Fetches products from the API
     * 
     * @param searchInput The search input
     * @param supermarket The supermarket to search in
     * @return A list of products
    */
    public List<ProductDTO> fetchProducts(
        String searchInput,
        String supermarket
    ) {
        // Format the API URL
        String apiUrl = String.format("%s/%s?product_name=%s", API_BASE_URL, supermarket, searchInput);

        // Make the API request
        ResponseEntity<List<ProductDTO>> response = restTemplate.exchange(
            apiUrl,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<ProductDTO>>() {}
        );

        return response.getBody();
    }
}
