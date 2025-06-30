package com.marketplace.product.service;

import com.marketplace.aws.exception.AwsPhotoUploadException;
import com.marketplace.common.exception.EntityNotFoundException;
import com.marketplace.product.config.ProductApplicationConfig;
import com.marketplace.product.kafka.producer.ProductEventProducer;
import com.marketplace.product.repository.ProductRepository;
import com.marketplace.product.util.MockHelper;
import com.marketplace.product.util.ProductDataBuilder;
import com.marketplace.product.util.UserDataBuilder;
import com.marketplace.product.web.dto.ProductRequest;
import com.marketplace.product.web.model.Product;
import com.marketplace.usercore.model.User;
import com.marketplace.usercore.model.UserRole;
import com.marketplace.usercore.security.AuthenticationUserService;
import com.marketplace.usercore.service.DefaultUserValidationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest(classes = ProductApplicationConfig.class)
public class MongoProductCrudServiceTest {

    @MockitoBean
    private ProductRepository productRepository;

    @MockitoBean
    private AuthenticationUserService authenticationUserService;

    @MockitoBean
    private DefaultUserValidationService defaultUserValidationService;

    @MockitoBean
    private ProductEventProducer productEventProducer;

    @Autowired
    private MockHelper mockHelper;

    @Autowired
    private MongoProductCrudService mongoProductCrudService;

    @AfterEach
    public void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void create_shouldCreateProduct() {
        Product product = ProductDataBuilder.buildProductWithAllFields().build();
        MockMultipartFile mockMultipartFile = new MockMultipartFile("data", "photo.png", "image/*", "photo_content".getBytes());
        ProductRequest productRequest = ProductRequest.builder()
                .name(product.getName())
                .description(product.getDescription())
                .photo(mockMultipartFile)
                .price(product.getPrice())
                .build();

        User user = mockHelper.mockAuthenticationAndSetContext();

        when(authenticationUserService.getAuthenticatedUser()).thenReturn(user);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product responseProduct = mongoProductCrudService.create(productRequest);

        assertThat(product.getName()).isEqualTo(responseProduct.getName());
        assertThat(product.getDescription()).isEqualTo(responseProduct.getDescription());
        assertThat(product.getPrice()).isEqualTo(responseProduct.getPrice());

        verify(authenticationUserService).getAuthenticatedUser();
        verify(productRepository).save(any(Product.class));
    }

    @Test
    public void create_shouldThrowException_WhenPhotoExistsWithWrongFormat() {
        Product product = ProductDataBuilder.buildProductWithAllFields().build();
        MockMultipartFile mockMultipartFile = new MockMultipartFile("data", "photo.svg", "image/svg", "photo_content".getBytes());
        ProductRequest productRequest = ProductRequest.builder()
                .name(product.getName())
                .description(product.getDescription())
                .photo(mockMultipartFile)
                .price(product.getPrice())
                .build();

        User user = mockHelper.mockAuthenticationAndSetContext();

        when(authenticationUserService.getAuthenticatedUser()).thenReturn(user);

        assertThatThrownBy(() -> mongoProductCrudService.create(productRequest)).isInstanceOf(AwsPhotoUploadException.class);

        verify(authenticationUserService).getAuthenticatedUser();
    }

    @Test
    public void create_shouldThrowException_WhenNoSecurity() {
        Product product = ProductDataBuilder.buildProductWithAllFields().build();
        ProductRequest productRequest = ProductRequest.builder()
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .build();

        when(authenticationUserService.getAuthenticatedUser()).thenThrow(AuthenticationCredentialsNotFoundException.class);

        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> mongoProductCrudService.create(productRequest));
    }

    @Test
    public void findAll_shouldReturnAllProducts() {
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        when(productRepository.findAll()).thenReturn(List.of(product));

        List<Product> products = mongoProductCrudService.findAll();

        assertEquals(1, products.size());
        assertEquals("Test Product", products.get(0).getName());
    }

    @Test
    public void findById_shouldReturnProductById() {
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        Product result = mongoProductCrudService.getById(product.getId());
        assertEquals(product, result);
    }

    @Test
    public void findById_shouldThrowExceptionIfProductNotFound() {
        String id = UUID.randomUUID().toString();

        when(productRepository.findById(id)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class, () -> mongoProductCrudService.getById(id));
        assertThat(exception.getMessage()).isEqualTo("Product not found!");
    }

    @Test
    public void update_shouldUpdateProduct() {
        User user = mockHelper.mockAuthenticationAndSetContext();
        Product product = ProductDataBuilder.buildProductWithAllFields()
                .ownerId(user.getId())
                .build();
        ProductRequest productRequest = ProductRequest.builder()
                .name("Updated Name")
                .description("Updated Description")
                .amount(1)
                .price(BigDecimal.valueOf(199.99))
                .build();

        when(authenticationUserService.getAuthenticatedUser()).thenReturn(user);
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(defaultUserValidationService.validateEntityOwnerOrAdmin(user, product.getOwnerId())).thenReturn(true);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product resultProduct = mongoProductCrudService.update(product.getId(), productRequest);

        assertThat(productRequest.getName()).isEqualTo(resultProduct.getName());
        assertThat(productRequest.getDescription()).isEqualTo(resultProduct.getDescription());
        assertThat(productRequest.getPrice()).isEqualTo(resultProduct.getPrice());

        verify(authenticationUserService).getAuthenticatedUser();
        verify(productRepository).findById(product.getId());
        verify(defaultUserValidationService).validateEntityOwnerOrAdmin(user, product.getOwnerId());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    public void update_shouldThrowException_WhenNoSecurity() {
        String productId = String.valueOf(UUID.randomUUID());
        ProductRequest productRequest = ProductRequest.builder()
                .name("Updated Name")
                .description("Updated Description")
                .amount(1)
                .price(BigDecimal.valueOf(199.99))
                .build();

        when(authenticationUserService.getAuthenticatedUser()).thenThrow(AuthenticationCredentialsNotFoundException.class);

        assertThatThrownBy(() -> mongoProductCrudService.update(productId, productRequest)).isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }

    @Test
    public void update_shouldThrowException_WhenProductNotFound() {
        String productId = String.valueOf(UUID.randomUUID());
        ProductRequest productRequest = ProductRequest.builder()
                .name("Updated Name")
                .description("Updated Description")
                .amount(1)
                .price(BigDecimal.valueOf(199.99))
                .build();

        when(productRepository.findById(productId)).thenThrow(EntityNotFoundException.class);

        assertThatThrownBy(() -> mongoProductCrudService.update(productId, productRequest)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void update_shouldThrowException_WhenUserNotOwner() {
        User user = mockHelper.mockAuthenticationAndSetContext();
        String ownerId = String.valueOf(UUID.randomUUID());
        Product product = ProductDataBuilder.buildProductWithAllFields()
                .build();
        ProductRequest productRequest = ProductRequest.builder()
                .name("Updated Name")
                .description("Updated Description")
                .amount(1)
                .price(BigDecimal.valueOf(199.99))
                .build();

        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(defaultUserValidationService.validateEntityOwnerOrAdmin(user, ownerId)).thenReturn(false);

        assertThatThrownBy(() -> mongoProductCrudService.update(product.getId(), productRequest)).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    public void update_shouldUpdateSuccessfully_WhenUserNotOwnerButAdmin() {
        User user = UserDataBuilder.buildUserWithAllFields()
                .role(UserRole.ADMIN)
                .build();
        mockHelper.mockAuthenticationAndSetContext(user);
        String ownerId = String.valueOf(UUID.randomUUID());
        Product product = ProductDataBuilder.buildProductWithAllFields()
                .ownerId(ownerId)
                .build();
        ProductRequest productRequest = ProductRequest.builder()
                .name("Updated Name")
                .description("Updated Description")
                .amount(1)
                .price(BigDecimal.valueOf(199.99))
                .build();

        when(authenticationUserService.getAuthenticatedUser()).thenReturn(user);
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(defaultUserValidationService.validateEntityOwnerOrAdmin(user, ownerId)).thenReturn(true);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product resultProduct = mongoProductCrudService.update(product.getId(), productRequest);

        assertThat(productRequest.getName()).isEqualTo(resultProduct.getName());
        assertThat(productRequest.getDescription()).isEqualTo(resultProduct.getDescription());
        assertThat(productRequest.getPrice()).isEqualTo(resultProduct.getPrice());
    }

    @Test
    public void delete_shouldValidateAndSendMessageToKafka() {
        User user = mockHelper.mockAuthenticationAndSetContext();
        Product product = ProductDataBuilder.buildProductWithAllFields()
                .ownerId(user.getId())
                .build();

        when(authenticationUserService.getAuthenticatedUser()).thenReturn(user);
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(defaultUserValidationService.validateEntityOwnerOrAdmin(user, product.getOwnerId())).thenReturn(true);

        mongoProductCrudService.delete(product.getId());

        verify(authenticationUserService).getAuthenticatedUser();
        verify(productRepository).findById(product.getId());
        verify(productEventProducer).sendDeleteProductFromOrdersEvent(product.getId());
    }

    @Test
    public void delete_shouldThrowException_WhenNoSecurity() {
        Product product = ProductDataBuilder.buildProductWithAllFields().build();
        User user = mockHelper.mockAuthenticationAndSetContext();
        product.setOwnerId(user.getId());

        when(authenticationUserService.getAuthenticatedUser()).thenThrow(AuthenticationCredentialsNotFoundException.class);

        assertThatThrownBy(() -> mongoProductCrudService.delete(product.getId())).isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }

    @Test
    public void delete_shouldThrowException_WhenProductNotFound() {
        User user = mockHelper.mockAuthenticationAndSetContext();
        Product product = ProductDataBuilder.buildProductWithAllFields()
                .ownerId(user.getId())
                .build();

        when(productRepository.findById(product.getId())).thenThrow(EntityNotFoundException.class);
        assertThatThrownBy(() -> mongoProductCrudService.delete(product.getId())).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void delete_shouldThrowException_WhenNotOwner() {
        String ownerId = String.valueOf(UUID.randomUUID());
        Product product = ProductDataBuilder.buildProductWithAllFields()
                .ownerId(ownerId)
                .build();
        User user = mockHelper.mockAuthenticationAndSetContext();

        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(defaultUserValidationService.validateEntityOwnerOrAdmin(user, ownerId)).thenReturn(false);

        assertThatThrownBy(() -> mongoProductCrudService.delete(product.getId())).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    public void delete_shouldValidateAndSendMessageToKafka_WhenNotOwnerButAdmin() {
        String ownerId = String.valueOf(UUID.randomUUID());
        Product product = ProductDataBuilder.buildProductWithAllFields()
                .ownerId(ownerId)
                .build();
        User user = UserDataBuilder.buildUserWithAllFields()
                .role(UserRole.ADMIN)
                .build();
        mockHelper.mockAuthenticationAndSetContext(user);

        when(authenticationUserService.getAuthenticatedUser()).thenReturn(user);
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(defaultUserValidationService.validateEntityOwnerOrAdmin(user, ownerId)).thenReturn(true);

        mongoProductCrudService.delete(product.getId());

        verify(productEventProducer).sendDeleteProductFromOrdersEvent(product.getId());
    }
}
