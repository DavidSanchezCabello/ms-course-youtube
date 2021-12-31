package academy.digitallab.store.product.service;

import academy.digitallab.store.product.entity.Category;
import academy.digitallab.store.product.entity.Product;
import academy.digitallab.store.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
//Gracias a la siguiente anotación podemos hacer una inyección de dependencias por constructor
@RequiredArgsConstructor
public class ProductServiceImpl  implements ProductService{


    private final ProductRepository productRepository;

    @Override
    public List<Product> listAllProduct() {
        return productRepository.findAll();
    }

    @Override
    public Product getProduct(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    @Override
    public Product createProduct(Product product) {
        //Setteamos su estado inicial
        product.setStatus("CREATED");
        //Creamos el nuevo producto
        product.setCreateAt(new Date());
        //Lo guardamos en nuestra base de datos
        return productRepository.save(product);
    }

    @Override
    public Product updateProduct(Product product) {
        //Buscamos el producto a tratar
        Product productDB = getProduct(product.getId());
        if (null == productDB){
            return null;
        }
        //Actualizamos todos los campos del producto
        productDB.setName(product.getName());
        productDB.setDescription(product.getDescription());
        productDB.setCategory(product.getCategory());
        productDB.setPrice(product.getPrice());
        return productRepository.save(productDB);
    }

    @Override
    public Product deleteProduct(Long id) {
        //Buscamos el producto a tratar
        Product productDB = getProduct(id);
        if (null == productDB){
            return null;
        }
        //Cambiamos su estado a eliminado porque inicialmente los datos NO deben ser eliminados totalmente.(Por razones de Legalidad y protección de datos)
        productDB.setStatus("DELETED");
        return productRepository.save(productDB);
    }

    @Override
    public List<Product> findByCategory(Category category) {
        return productRepository.findByCategory(category);
    }

    @Override
    public Product updateStock(Long id, Double quantity) {
        //Buscamos el producto a tratar
        Product productDB = getProduct(id);
        if (null == productDB){
            return null;
        }
        //Cuando se actualiza el stock le sumamos a la cantidad existente de la siguiente forma
        Double stock =  productDB.getStock() + quantity;
        //Actualizamos el stock
        productDB.setStock(stock);
        //Guardamos el stock
        return productRepository.save(productDB);
    }
}
