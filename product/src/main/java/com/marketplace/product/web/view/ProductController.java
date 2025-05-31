package com.marketplace.product.web.view;

import com.marketplace.product.service.ProductCrudService;
import com.marketplace.product.web.dto.ProductRequest;
import com.marketplace.product.web.model.Product;

import com.marketplace.product.mapper.ProductEntityMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

    private final ProductCrudService productCrudService;

    private final ProductEntityMapper productEntityMapper;

    @GetMapping("/all")
    public String getAllProducts(Model model) {
        List<Product> products = productCrudService.findAll();
        model.addAttribute("products", productEntityMapper.mapProductsToProductResponseDtos(products));
        return "products";
    }

    @GetMapping("/{productId}")
    public String getProductById(
            Model model,
            @PathVariable String productId
    ) {
        Product product = productCrudService.getById(productId);
        model.addAttribute("product", productEntityMapper.mapProductToProductResponseDto(product));
        return "product";
    }

    @GetMapping("/create")
    public String getCreateProduct(Model model) {
        model.addAttribute("productRequest", ProductRequest.builder().build());
        return "product-create";
    }

    @PostMapping("/create")
    public String createProduct(
            @Valid @ModelAttribute ProductRequest productRequest,
            BindingResult bindingResult
    ) {

        if (bindingResult.hasErrors()) {
            return "product-create";
        }

        productCrudService.create(productRequest);
        return "redirect:/products";
    }

    @GetMapping("/{productId}/update")
    public String getUpdateProduct(
            @PathVariable String productId,
            Model model
    ) {
        Product product = productCrudService.getById(productId);

        model.addAttribute("productId", productId);
        model.addAttribute("productRequest", productEntityMapper.mapProductToProductRequestDto(product));
        return "product-update";
    }

    @PutMapping("/{productId}/update")
    public String updateProduct(
             @PathVariable String productId,
             @Valid @ModelAttribute ProductRequest productRequest,
             BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return "product-update";
        }

        Product product = productCrudService.update(productId, productRequest);
        productEntityMapper.mapProductToProductResponseDto(product);

        return "redirect:/products/" + productId;
    }

    // TODO on delete we need to remove product ids in orders as well　(better to use Kafka some day here)
    @DeleteMapping("/{productId}/delete")
    public String deleteProduct(@PathVariable String productId) {
//        productCrudService.delete(productId);
        return "redirect:/products";
    }
}
