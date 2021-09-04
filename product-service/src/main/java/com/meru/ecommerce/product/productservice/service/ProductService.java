package com.meru.ecommerce.product.productservice.service;

import java.util.List;

import com.meru.ecommerce.product.productservice.entity.Product;

public interface ProductService {

    public List<Product> getAllProducts();

    public Product getProductById(int productId);

    public boolean createOrUpdateProduct(Product product);

    public boolean removeProduct(int productId);
}
