package cl.duoc.ms_customers_bff.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cl.duoc.ms_customers_bff.clients.CustomersBsFeignClient;

@Service
public class CustomerService {

    @Autowired
    CustomersBsFeignClient customersBsFeignClient;
}
