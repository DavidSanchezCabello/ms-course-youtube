package academy.digitallab.store.product;

import academy.digitallab.store.product.entity.Category;
import academy.digitallab.store.product.entity.Product;
import academy.digitallab.store.product.repository.ProductRepository;
import academy.digitallab.store.product.service.ProductService;
import academy.digitallab.store.product.service.ProductServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

@SpringBootTest
public class ProductServiceMockTest {

    @Mock
    private ProductRepository productRepository;

    private ProductService productService;

    @BeforeEach
    public void setup(){
        MockitoAnnotations.initMocks(this);
        productService =  new ProductServiceImpl( productRepository);
        Product computer =  Product.builder()
                .id(1L)
                .name("computer")
                .category(Category.builder().id(1L).build())
                .price(Double.parseDouble("12.5"))
                .stock(Double.parseDouble("5"))
                .build();

        Mockito.when(productRepository.findById(1L))
                .thenReturn(Optional.of(computer));
        //La siguiente situación se dará cuando se actualice nuestro producto Mockito(de pruebas)
        Mockito.when(productRepository.save(computer)).thenReturn(computer);

    }

    //Éste método realizara una busqueda del producto mockeado antes y...
    @Test
   public void whenValidGetID_ThenReturnProduct(){
        //...al ejecutarse el siguiente método nos debe devolver...
        Product found = productService.getProduct(1L);
        //...lo que le pedimos en el siguiente Assertions
       Assertions.assertThat(found.getName()).isEqualTo("computer");

   }

   //Éste método nos validará si actualizando un producto se realiza correctamente
   @Test
   public void whenValidUpdateStock_ThenReturnNewStock(){
        //Realizamos una prueba para la cual modificamos el valor de stock de nuestro producto Mockeado(de pruebas)
        Product newStock = productService.updateStock(1L,Double.parseDouble("8"));
        Assertions.assertThat(newStock.getStock()).isEqualTo(13);
   }
}
