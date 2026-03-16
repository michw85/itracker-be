package de.upteams.tasktracker.test.util;

import de.upteams.tasktracker.utils.BaseEntity;

import java.lang.reflect.Field;
import java.util.UUID;

/**
 * Utility class for tests to set IDs on entities via reflection.
 * Since entities inherit from BaseEntity and don't have public setId methods,
 * we need reflection to set IDs for testing.
 */
public class TestEntityUtils {

    /**
     * Sets the ID of any BaseEntity using reflection.
     *
     * @param entity the entity to set ID on
     * @param id the UUID to set
     * @param <T> the entity type
     * @throws RuntimeException if reflection fails
     */
    public static <T extends BaseEntity> void setId(T entity, UUID id) {
        try {
            Field idField = entity.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set ID on entity", e);
        }
    }

    /**
     * Creates a new entity with a generated ID.
     *
     * @param entitySupplier supplier that creates the entity without ID
     * @param <T> the entity type
     * @return entity with ID set
     */
    public static <T extends BaseEntity> T withRandomId(java.util.function.Supplier<T> entitySupplier) {
        T entity = entitySupplier.get();
        setId(entity, UUID.randomUUID());
        return entity;
    }
}