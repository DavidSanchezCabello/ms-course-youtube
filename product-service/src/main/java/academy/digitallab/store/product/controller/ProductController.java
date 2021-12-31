package academy.digitallab.store.product.controller;

import academy.digitallab.store.product.entity.Category;
import academy.digitallab.store.product.entity.Product;
import academy.digitallab.store.product.service.ProductService;
import ch.qos.logback.core.pattern.util.RegularEscapeUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping (value = "/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<List<Product>> listProduct(@RequestParam(name = "categoryId", required = false) Long categoryId){
        //Creamos un listado de productos para rellenarlo posteriormente
        List<Product> products = new ArrayList<>();
        //Comprobamos si existe una categoría...
        if (null ==  categoryId){
            //...de no existir, invocamos nuestro servicio y extraemos todos los productos para guardarlos en nuestra nueva instancia de List<Product>
             products = productService.listAllProduct();
             //Comprobamos si el listado que nos devuelve está o no vacío
            if (products.isEmpty()){
                //Si está vacío devolvemos una respuesta de NOT CONTENT
                return ResponseEntity.noContent().build();
            }
        }else{
            //Para el caso en que si nos devuelva un listado nuestra petición, lo guardamos en elk objeto List<Product> (products)
            products = productService.findByCategory(Category.builder().id(categoryId).build());
            // Comprobamos también en este caso si existen productos de ésa categoría, de lo contrario devolveremos un mensaje de NOT FOUND
            if (products.isEmpty()){
                return ResponseEntity.notFound().build();
            }
        }

        //Y por último de existir productos regresaremos el listado con su contenido
        return ResponseEntity.ok(products);
    }


    @GetMapping(value = "/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable("id") Long id) {
        //Creamos un listado de productos para rellenarlo posteriormente
        Product product =  productService.getProduct(id);
        //Comprobamos si existe una categoría...
        if (null==product){
            //Si está vacío devolvemos una respuesta de NOT FOUND
            return ResponseEntity.notFound().build();
        }
        //Y por último de existir producto regresaremos el producto
        return ResponseEntity.ok(product);
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product, BindingResult result){
        //Verificación de errores que devolverá el mensaje del error si existe
        if (result.hasErrors()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, this.formatMessage(result));
        }
        //Instanciamos un objeto de la clase Product e invocamos a suentro servicio para usar la función de createProduct
        Product productCreate =  productService.createProduct(product);
        //Devolvemos una ResponseEntity de HttpStatus de que se ha creado correctamente y el cuerpo del nuevo producto creado
        return ResponseEntity.status(HttpStatus.CREATED).body(productCreate);
    }

    //Para esta función tendremos que pasarle por parámetros tanto el ID del producto como el objeto Producto que vamos a actualizar
   @PutMapping(value = "/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable("id") Long id, @RequestBody Product product){
        //Seteamos el ID del producto
        product.setId(id);
        //Actualizamos el producto en la base de datos(BBDD)
        Product productDB =  productService.updateProduct(product);
        //Verificamos si el producto fue actualizado o si existe
        if (productDB == null){
            //Si no existe delvolveremos un mensaje de NOT FOUND
            return ResponseEntity.notFound().build();
        }
        //Devolvemos el producto actualizado
        return ResponseEntity.ok(productDB);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Product> deleteProduct(@PathVariable("id") Long id){
        //Instanciamos un objeto de Product e invocamos el método deleteProduct de nuestro servicio
        Product productDelete = productService.deleteProduct(id);
        //Comprobamos que el producto que hemos borrado, ya no existe y devolvemos un mensaje de NOT FOUND
        if (productDelete == null){
            return ResponseEntity.notFound().build();
        }
        //Devolvemos un ResponseEntity de que fue eliminado.
        return ResponseEntity.ok(productDelete);
    }
    @PutMapping (value = "/{id}/stock")
    public ResponseEntity<Product> updateStockProduct(@PathVariable  Long id ,@RequestParam(name = "quantity", required = true) Double quantity){
        //Instanciamos un objeto de Product e invocamos el método updateStock de nuestro servicio, para actualizar el stock
        Product product = productService.updateStock(id, quantity);
        //Comprobamos que el producto que hemos borrado, ya no existe y devolvemos un mensaje de NOT FOUND
        if (product == null){
            return ResponseEntity.notFound().build();
        }
        //Devolvemos un ResponseEntity de que fue actualizado satisfactoriamente.
        return ResponseEntity.ok(product);
    }

    //Métopdo que formateará un mensaje resultante, dependiendo del error introducido
    private String formatMessage( BindingResult result){
        //Recibimos un objeto result con todos los campos de errores capturados
        List<Map<String,String>> errors = result.getFieldErrors().stream()
                .map(err ->{
                    Map<String,String>  error =  new HashMap<>();
                    error.put(err.getField(), err.getDefaultMessage());
                    return error;

                }).collect(Collectors.toList());
        ErrorMessage errorMessage = ErrorMessage.builder()
                .code("01")
                .messages(errors).build();
        //Utilizando Jackson, convertimos nuestro objeto ErrorMessage en un jsonString.
        ObjectMapper mapper = new ObjectMapper();
        String jsonString="";
        try {
            jsonString = mapper.writeValueAsString(errorMessage);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return jsonString;
    }
}
