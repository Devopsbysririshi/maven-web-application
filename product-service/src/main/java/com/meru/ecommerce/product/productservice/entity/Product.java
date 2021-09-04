package com.meru.ecommerce.product.productservice.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "Product")
public class Product implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int productId;
    @Column
    private String productName;
    @Column
    private String category;
    @Column
    private String productStatus;
    @Embedded
    private ProductDescription productDescription;

    public Product() {
    }

    public Product(String productName, String category, String productStatus, ProductDescription productDescription) {
        this.productName = productName;
        this.category = category;
        this.productStatus = productStatus;
        this.productDescription = productDescription;
    }

    public Product(int productId, String productName, String category, String productStatus,
            ProductDescription productDescription) {
        this.productId = productId;
        this.productName = productName;
        this.category = category;
        this.productStatus = productStatus;
        this.productDescription = productDescription;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getProductStatus() {
        return productStatus;
    }

    public void setProductStatus(String productStatus) {
        this.productStatus = productStatus;
    }

    public ProductDescription getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(ProductDescription productDescription) {
        this.productDescription = productDescription;
    }

    @Override
    public String toString() {
        return "Product [productId=" + productId + ", productName=" + productName + ", category=" + category
                + ", productStatus=" + productStatus + ", productDescription=" + productDescription + "]";
    }
}
