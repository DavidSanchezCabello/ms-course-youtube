package academy.digitallab.store.shopping.service;

import academy.digitallab.store.shopping.client.CustomerClient;
import academy.digitallab.store.shopping.client.ProductClient;
import academy.digitallab.store.shopping.entity.InvoiceItem;
import academy.digitallab.store.shopping.model.Customer;
import academy.digitallab.store.shopping.model.Product;
import academy.digitallab.store.shopping.repository.InvoiceItemsRepository;
import academy.digitallab.store.shopping.repository.InvoiceRepository;
import academy.digitallab.store.shopping.entity.Invoice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class InvoiceServiceImpl implements InvoiceService {

    // Inyección de dependencias que vamos a utilizar en las siguientes funciones
    @Autowired
    InvoiceRepository invoiceRepository;

    @Autowired
    InvoiceItemsRepository invoiceItemsRepository;
    @Autowired
    CustomerClient customerClient;

    @Autowired
    ProductClient productClient;

    @Override
    public List<Invoice> findInvoiceAll() {
        return  invoiceRepository.findAll();
    }


    @Override
    public Invoice createInvoice(Invoice invoice) {
        Invoice invoiceDB = invoiceRepository.findByNumberInvoice ( invoice.getNumberInvoice () );
        if (invoiceDB !=null){
            return  invoiceDB;
        }
        invoice.setState("CREATED");
        invoiceDB = invoiceRepository.save(invoice);
        //recorremos todos los items de nuestra BD y actualizando el stock de nuestros productos para cada uno de los Item de nuestra factura, restandole la cantidad del producto que estamos consumiento aquí en la factura.
        invoiceDB.getItems().forEach( invoiceItem -> {
            productClient.updateStockProduct( invoiceItem.getProductId(), invoiceItem.getQuantity() * -1);
        });

        return invoiceDB;
    }


    @Override
    public Invoice updateInvoice(Invoice invoice) {
        Invoice invoiceDB = getInvoice(invoice.getId());
        if (invoiceDB == null){
            return  null;
        }
        invoiceDB.setCustomerId(invoice.getCustomerId());
        invoiceDB.setDescription(invoice.getDescription());
        invoiceDB.setNumberInvoice(invoice.getNumberInvoice());
        invoiceDB.getItems().clear();
        invoiceDB.setItems(invoice.getItems());
        return invoiceRepository.save(invoiceDB);
    }


    @Override
    public Invoice deleteInvoice(Invoice invoice) {
        Invoice invoiceDB = getInvoice(invoice.getId());
        if (invoiceDB == null){
            return  null;
        }
        invoiceDB.setState("DELETED");
        return invoiceRepository.save(invoiceDB);
    }

    @Override
    public Invoice getInvoice(Long id) {

        //Recuperamos la factura que vamos a retornar, para poder actualizar una vez tratada con los microservicio de producto (product-service) y del cliente (customer-service)
        Invoice invoice= invoiceRepository.findById(id).orElse(null);
        //si existe una factura
        if (null != invoice ){
            //realizamos la busqueda del cliente
            Customer customer = customerClient.getCustomer(invoice.getCustomerId()).getBody();
            //lo incluímos en la factura con el método set()
            invoice.setCustomer(customer);
            //recuperamos todos los datos de cada uno de los productos de nuestra factura y los mapeamos en la lista
            List<InvoiceItem> listItem=invoice.getItems().stream().map(invoiceItem -> {
                Product product = productClient.getProduct(invoiceItem.getProductId()).getBody();
                //los incluímos en nuestro objeto Lista de InvoiceItem
                invoiceItem.setProduct(product);
                return invoiceItem;
                //como lo que hasta ahora hemos hecho, nos devuelve un flujo, debemos convertirlo en una colección, así que con este último paso lo convertiremos en una lista.
            }).collect(Collectors.toList());
            //seteamos ésta nueva lista de intems en nuestro objeto invoice(factura)
            invoice.setItems(listItem);
        }
        return invoice ;
    }
}
