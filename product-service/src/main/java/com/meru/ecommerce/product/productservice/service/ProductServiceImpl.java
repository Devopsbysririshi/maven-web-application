package com.meru.ecommerce.product.productservice.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.meru.ecommerce.product.productservice.dao.ProductRepository;
import com.meru.ecommerce.product.productservice.entity.Product;

@Service("ProductService")
public class ProductServiceImpl implements ProductService {

	@Autowired
	ProductRepository pr;

	@Override
	public List<Product> getAllProducts() {
		return pr.findAll();
	}

	@Override
	public Product getProductById(int productId) {
		return pr.findOne(productId);
	}

	@Override
	public boolean createOrUpdateProduct(Product product) {
		Product p = pr.save(product);
		boolean status = false;
		if (null != p) {
			status = true;
		}
		// also trigger the jms message to the topic using the updated product
		return status;
	}

	@Override
	public boolean removeProduct(int productId) {
		Product p = pr.findOne(productId);
		pr.delete(productId);
		// trigger and update via jms
		if (null != p) {
		}
		return true;
	}

}
