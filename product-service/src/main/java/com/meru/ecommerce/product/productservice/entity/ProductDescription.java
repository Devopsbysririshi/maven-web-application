package com.meru.ecommerce.product.productservice.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ProductDescription implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column
    private String details; // description of the item

    @Column
    private String specification; // all product specs

    @Column
    private double weight; // weight in grams

    public ProductDescription() {
    }

    public ProductDescription(String details, String specification, double weight) {
        this.details = details;
        this.specification = specification;
        this.weight = weight;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getSpecification() {
        return specification;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "ProductDescription [details=" + details + ", specification=" + specification + ", weight=" + weight
                + "]";
    }
}
