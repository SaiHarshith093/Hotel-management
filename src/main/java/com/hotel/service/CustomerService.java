package com.hotel.service;

import com.hotel.dao.CustomerDao;
import com.hotel.exception.CustomerNotFoundException;
import com.hotel.exception.HotelException;
import com.hotel.model.Customer;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerDao customerDao;

    public CustomerService(CustomerDao customerDao) {
        this.customerDao = customerDao;
    }

    public List<Customer> findCustomers(String search) {
        return customerDao.findAll(search);
    }

    public Customer getCustomerById(Long id) {
        return customerDao.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }

    @Transactional
    public Customer createCustomer(Customer customer) {
        Long id = customerDao.save(customer);
        customer.setId(id);
        return customer;
    }

    @Transactional
    public Customer updateCustomer(Long id, Customer customer) {
        getCustomerById(id);
        customer.setId(id);
        customerDao.update(customer);
        return customer;
    }

    @Transactional
    public void deleteCustomer(Long id) {
        getCustomerById(id);
        try {
            int deleted = customerDao.deleteById(id);
            if (deleted == 0) {
                throw new CustomerNotFoundException(id);
            }
        } catch (DataIntegrityViolationException ex) {
            throw new HotelException("Cannot delete customer. They may have active bookings.");
        }
    }
}
