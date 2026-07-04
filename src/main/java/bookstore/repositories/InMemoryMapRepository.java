package bookstore.repositories;

import bookstore.entities.ProductEntity;
import java.util.*;

public class InMemoryMapRepository implements IRepository<ProductEntity> {

    private final Map<Long, ProductEntity> map = new HashMap<>();
    private Long idCounter = 1L;

    @Override
    public ProductEntity save(ProductEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Cannot save a null entity.");
        }

        // Assign ID if new
        if (entity.getId() == null) {
            entity.setId(idCounter++);
        }

        // Upsert (HashMap replaces automatically)
        map.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public ProductEntity findById(Long id) {
        if (id == null) return null;
        return map.get(id); // O(1) lookup
    }

    @Override
    public ProductEntity findByProductId(String productId) {
        if (productId == null) return null;

        // Must scan values (UUID is not the key)
        for (ProductEntity entity : map.values()) {
            if (Objects.equals(entity.getProductId(), productId)) {
                return entity;
            }
        }
        return null;
    }

    @Override
    public List<ProductEntity> findAll() {
        return new ArrayList<>(map.values());
    }

    @Override
    public void delete(Long id) {
        if (id == null) return;
        map.remove(id);
    }

    @Override
    public long count() {
        return map.size();
    }

    @Override
    public int deleteAll() {
        int deleted = map.size();
        map.clear();
        return deleted;
    }

    @Override
    public String getDataSourceType() {
        return "VOLATILE RAM (HashMap - Indexed Search)";
    }

    @Override
    public void close() {
        System.out.println("Volatile memory cleared (InMemoryMapRepository).");
    }
}
