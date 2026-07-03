package bookstore;

import bookstore.entities.ProductEntity;
import bookstore.repositories.IRepository;
import bookstore.repositories.ProductRepository;

public class Main {
    public static void main(String[] args) {
        System.out.println("Bookstore Application Started");
        IRepository<ProductEntity> repo = new ProductRepository();
        App app = new App(repo);
        try {
            app.run();
        } finally {
            app.shutdown();
        }
    }
}