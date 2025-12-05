package id.fahmikudo.productservice.service;

import id.fahmikudo.productservice.model.Product;
import id.fahmikudo.productservice.repository.ProductRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProductService {

    private final Logger logger = LogManager.getLogger(ProductService.class);

    private final ProductRepository repo;

    public ProductService(ProductRepository repo) {
        this.repo = repo;
    }


    public Product create(Product product) {
        return repo.save(product);
    }

    public Optional<Product> findById(String id) {
        logger.info("Finding product by id: {}", id);
        return repo.findById(id);
    }

    public Iterable<Product> findAll() {
        return repo.findAll();
    }

    public Optional<Product> update(String id, Product updated) {
        return repo.findById(id).map(existing -> {
            existing.setName(updated.getName());
            existing.setDescription(updated.getDescription());
            existing.setPrice(updated.getPrice());
            return repo.save(existing);
        });
    }

    public void delete(String id) {
        repo.deleteById(id);
    }

}
