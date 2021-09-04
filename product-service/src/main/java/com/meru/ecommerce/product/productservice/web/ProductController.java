package com.meru.ecommerce.product.productservice.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.meru.ecommerce.product.productservice.entity.Product;
import com.meru.ecommerce.product.productservice.service.ProductService;



@Controller
public class ProductController {
    static String WELCOME_MSG = "Welcome to Product Service. This is not a valid service path.";
    static String CREATE_SUB_COMPONENT_MSG = "Create %s for product %d in Product is %s";
    static String UPDATE_SUB_COMPONENT_MSG = "Update %s for product %d in Product is %s";
    static String DELETE_MSG = "Delete Product for ProductID: %s %s";
    static String SUCCESS = "Success";
    static String ERROR = "Failed";
    static String COMPONENT_PRODUCT = "Product";
    static String RETURN_TEMPLATE = "{\"message\":\"%s\"}";

    @Autowired
    private ProductService ps;
    
    

    
    @RequestMapping(value={"/index"},method = RequestMethod.GET)
	public String index(Model model){
		List<Product> products= ps.getAllProducts();
		model.addAttribute("products", products);
		
		return "index";
	}
	
	@RequestMapping(value="/search",method = RequestMethod.GET)
	public String search(Product search){
		return "Search";
	}
	
	
	@RequestMapping(value="/searchbyid",method = RequestMethod.GET)
	public String searchById(@RequestParam(value = "search", required = false) String id, Model model){
		
		Product product=  ps.getProductById(Integer.parseInt(id));
		 
		 model.addAttribute("search", product);
		return "Search";
		
		
	}
	
	
	@RequestMapping(value="add")
	public String addProduct(Model model) {
		
		model.addAttribute("product", new Product());
		return "addProduct";
	}
	
	@RequestMapping(value="save", method=RequestMethod.POST)
	public String saveProduct(Product product) {
		ps.createOrUpdateProduct(product);
		return "redirect:/index";
	}
	
	@RequestMapping(value="/delete/{productId}", method=RequestMethod.GET)
	public String deleteProduct(@PathVariable("productId") int productId, Model model) {
		 ps.removeProduct(productId);
		return "redirect:/index";
	}
	
    
    
    @RequestMapping(method = RequestMethod.GET, value = "/")
    public ResponseEntity<String> showInfo() {
    	System.out.println("showinfo...........");
        return ResponseEntity.badRequest().headers(new HttpHeaders()).body(WELCOME_MSG);
    }

    @CrossOrigin
    @RequestMapping(method = RequestMethod.GET, value = "/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable int id) {
        Product pvm = ps.getProductById(id);
        HttpStatus status = HttpStatus.OK;
        if (null == pvm) {
            status = HttpStatus.NO_CONTENT;
        }
        return new ResponseEntity<>(pvm, new HttpHeaders(), status);
    }

    @CrossOrigin
    @RequestMapping(method = RequestMethod.GET, value = "/all")
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok().body(ps.getAllProducts());
    }

    @CrossOrigin
    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable int id) {
        boolean deleted = ps.removeProduct(id);
        HttpStatus status = HttpStatus.OK;
        String msg = String.format(DELETE_MSG, id, SUCCESS);
        if (!deleted) {
            msg = String.format(DELETE_MSG, id, ERROR);
            status = HttpStatus.BAD_REQUEST;
        }
        return new ResponseEntity<>(String.format(RETURN_TEMPLATE, msg), new HttpHeaders(), status);
    }

    @CrossOrigin
    @RequestMapping(method = RequestMethod.POST, value = "/")
    public ResponseEntity<String> addProduct(@RequestBody Product product) {
        boolean created = ps.createOrUpdateProduct(product);
        HttpStatus status = HttpStatus.OK;
        String msg = String.format(CREATE_SUB_COMPONENT_MSG, COMPONENT_PRODUCT, product.getProductId(), SUCCESS);
        if (!created) {
            String.format(CREATE_SUB_COMPONENT_MSG, COMPONENT_PRODUCT, product.getProductId(), ERROR);
            status = HttpStatus.BAD_REQUEST;
        }
        return new ResponseEntity<>(String.format(RETURN_TEMPLATE, msg), new HttpHeaders(), status);
    }

    @CrossOrigin
    @RequestMapping(method = RequestMethod.PUT, value = "/{id}")
    public ResponseEntity<String> updateProductInProductView(@PathVariable int id, @RequestBody Product product) {
        boolean updated = ps.createOrUpdateProduct(product);
        HttpStatus status = HttpStatus.OK;
        String msg = String.format(UPDATE_SUB_COMPONENT_MSG, COMPONENT_PRODUCT, id, SUCCESS);
        if (!updated) {
            msg = String.format(UPDATE_SUB_COMPONENT_MSG, COMPONENT_PRODUCT, id, ERROR);
            status = HttpStatus.BAD_REQUEST;
        }
        return new ResponseEntity<>(String.format(RETURN_TEMPLATE, msg), new HttpHeaders(), status);
    }
}
