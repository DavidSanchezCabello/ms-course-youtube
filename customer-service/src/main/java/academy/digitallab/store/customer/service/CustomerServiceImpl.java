package academy.digitallab.store.customer.service;

import academy.digitallab.store.customer.repository.CustomerRepository;
import academy.digitallab.store.customer.repository.entity.Customer;
import academy.digitallab.store.customer.repository.entity.Region;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Slf4j
@Service
public class CustomerServiceImpl  implements CustomerService {

    @Autowired
    CustomerRepository customerRepository;

    @Override
    public List<Customer> findCustomerAll() {
        return customerRepository.findAll();
    }

    @Override
    public List<Customer> findCustomersByRegion(Region region) {
        return customerRepository.findByRegion(region);
    }

    @Override
    public Customer createCustomer(Customer customer) {

        /*
         * Principio de idempotencia aplicado a REST ( a los verbos HTTP ) dice que la ejecución repetida con los mismos parámetros sobre un mismo recurso
         * tendrá el mismo efecto en el estado de nuestro recurso en el sistema si se ejecuta 1 o N veces.
        */

        //Instanciamos un objeto de la clase Customer
        Customer customerDB = customerRepository.findByNumberID ( customer.getNumberID () );
        //Mediante la siguiente validación se resuelve el problema de idempotencia que genera la petición POST
        //Si yá existe le devolvemos el mismo existente
        if (customerDB != null){
            return  customerDB;
        }
        //de lo contrario se creará una sola vez y así cuando vuelva a utilizar este método, si existe lo devolverá directamente.
        customer.setState("CREATED");
        customerDB = customerRepository.save ( customer );
        return customerDB;
    }

    @Override
    public Customer updateCustomer(Customer customer) {
        Customer customerDB = getCustomer(customer.getId());
        if (customerDB == null){
            return  null;
        }
        customerDB.setFirstName(customer.getFirstName());
        customerDB.setLastName(customer.getLastName());
        customerDB.setEmail(customer.getEmail());
        customerDB.setPhotoUrl(customer.getPhotoUrl());

        return  customerRepository.save(customerDB);
    }

    @Override
    public Customer deleteCustomer(Customer customer) {
        Customer customerDB = getCustomer(customer.getId());
        if (customerDB ==null){
            return  null;
        }
        customer.setState("DELETED");
        return customerRepository.save(customer);
    }

    @Override
    public Customer getCustomer(Long id) {
        return  customerRepository.findById(id).orElse(null);
    }
}
