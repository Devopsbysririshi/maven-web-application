package com.meru.ecommerce.product.productservice.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.meru.ecommerce.product.productservice.entity.Product;

@Repository("ProductRepository")
public interface ProductRepository extends JpaRepository<Product, Integer> {

}
