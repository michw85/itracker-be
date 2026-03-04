package de.upteams.entity;

import de.upteams.tasktracker.user.entity.AppUser;
import de.upteams.tasktracker.utils.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Entity Classes Validation Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EntityValidationTest {

    private static Set<Class<?>> entityClasses;

    @BeforeAll
    public static void setUp() {
        final Collection<URL> classpathUrls = ClasspathHelper.forPackage("de.upteams");
        final Set<URL> packageUrls = new HashSet<>(classpathUrls);

        final Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(packageUrls)
                .setScanners(Scanners.TypesAnnotated));

        entityClasses = reflections.getTypesAnnotatedWith(Entity.class)
                .stream()
                .sorted(Comparator.comparing(Class::getName))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (entityClasses.isEmpty()) {
            fail("Entity's list for test not found!");
        }
    }


    @Test
    @DisplayName("Check that all entities extend BaseEntity")
    @Order(1)
    void testEntitiesExtendBaseEntity() {
        for (Class<?> entityClass : entityClasses) {
            assertNotNull(entityClass.getSuperclass(),
                    () -> entityClass.getName() + " does not have a superclass");
            assertEquals(BaseEntity.class, entityClass.getSuperclass(),
                    () -> entityClass.getName() + " must extend BaseEntity");
        }
    }

    @Test
    @DisplayName("Check that all entities have a public no-args constructor")
    @Order(2)
    void testEntitiesHaveNoArgsConstructor() {
        for (Class<?> entityClass : entityClasses) {
            boolean hasNoArgsConstructor = Arrays.stream(entityClass.getDeclaredConstructors())
                    .anyMatch(constructor -> constructor.getParameterCount() == 0 && Modifier.isPublic(constructor.getModifiers()));
            assertTrue(hasNoArgsConstructor, () -> entityClass.getName() + " must have a public no-args constructor!");
        }
    }

    @Nested
    @DisplayName("Setters Test")
    class SettersTest {

        @Test
        @DisplayName("Check if all entity classes have setters for all fields except ID")
        public void testSetters() {
            for (Class<?> entityClass : entityClasses) {
                validateSetters(entityClass);
            }
        }

        private void validateSetters(Class<?> entityClass) {
            String idFieldName = getIdFieldName(entityClass);

            List<String> fieldNames = Arrays.stream(entityClass.getDeclaredFields())
                    .filter(field -> !field.getName().equals(idFieldName))  // Skip the id field
                    .filter(field -> !Modifier.isFinal(field.getModifiers())) // Skip final fields
                    .map(Field::getName)
                    .toList();

            List<String> methodNames = Arrays.stream(entityClass.getMethods())
                    .map(Method::getName)
                    .toList();

            for (String fieldName : fieldNames) {
                String capitalizedFieldName = capitalize(fieldName);
                String setter = "set" + capitalizedFieldName;

                assertTrue(methodNames.contains(setter),
                        () -> entityClass.getName() + " is missing setter for field: " + fieldName);
            }
        }
    }

    @Nested
    @DisplayName("Getters Test")
    class GettersTest {

        @Test
        @DisplayName("Check if all entity classes have getters for all fields except ID")
        public void testGetters() {
            for (Class<?> entityClass : entityClasses) {
                validateGetters(entityClass);
            }
        }

        private void validateGetters(Class<?> entityClass) {
            String idFieldName = getIdFieldName(entityClass);

            List<String> fieldNames = Arrays.stream(entityClass.getDeclaredFields())
                    .map(Field::getName)
                    .filter(name -> !name.equals(idFieldName))  // Skip the id field
                    .toList();

            List<String> methodNames = Arrays.stream(entityClass.getMethods())
                    .map(Method::getName)
                    .toList();

            for (String fieldName : fieldNames) {
                String capitalizedFieldName = capitalize(fieldName);
                String getter = "get" + capitalizedFieldName;

                assertTrue(methodNames.contains(getter) || methodNames.contains("is" + capitalizedFieldName),
                        () -> entityClass.getName() + " is missing getter for field: " + fieldName);
            }
        }
    }

    @Nested
    @DisplayName("Equals Method Test")
    class EqualsMethodTest {

        @Test
        @DisplayName("Check if all entity classes have correctly implemented equals method based only on ID")
        public void testEqualsMethod() {
            for (Class<?> entityClass : entityClasses) {
                validateEqualsMethod(entityClass);
            }
        }

        private void validateEqualsMethod(Class<?> entityClass) {
            String idFieldName = getIdFieldName(entityClass);

            try {
                Object instance1 = entityClass.getConstructor().newInstance();
                Object instance2 = entityClass.getConstructor().newInstance();

                UUID entityId = UUID.randomUUID();
                setEntityId(instance1, entityId, idFieldName);
                setEntityId(instance2, entityId, idFieldName);

                Method equalsMethod = entityClass.getMethod("equals", Object.class);

                // Check equality
                assertTrue((boolean) equalsMethod.invoke(instance1, instance2),
                        () -> entityClass.getName() + " equals method should be based on '" + idFieldName + "' field");

                // Change non-id fields and verify equality is unaffected
                List<Field> fields = Arrays.stream(entityClass.getDeclaredFields())
                        .filter(field -> !field.getName().equals(idFieldName))
                        .toList();

                for (Field field : fields) {
                    setDummyValue(field, instance1);
                }

                assertTrue((boolean) equalsMethod.invoke(instance1, instance2),
                        () -> entityClass.getName() + " equals method should not be affected by non-id fields");

                // Change ID and verify inequality
                setEntityId(instance2, UUID.randomUUID(), idFieldName);

                assertFalse((boolean) equalsMethod.invoke(instance1, instance2),
                        () -> entityClass.getName() + " equals method should reflect changes in '" + idFieldName + "' field");

            } catch (Exception e) {
                fail("Test " + entityClass.getName() + " fail!", e);
            }
        }
    }

    @Nested
    @DisplayName("HashCode Method Test")
    class HashCodeMethodTest {

        @Test
        @DisplayName("Check if all entity classes have correctly implemented hashCode method based only on ID")
        public void testHashCodeMethod() {
            for (Class<?> entityClass : entityClasses) {
                validateHashCodeMethod(entityClass);
            }
        }

        private void validateHashCodeMethod(Class<?> entityClass) {
            String idFieldName = getIdFieldName(entityClass);

            try {
                Object instance1 = entityClass.getConstructor().newInstance();
                Object instance2 = entityClass.getConstructor().newInstance();

                UUID entityId = UUID.randomUUID();
                setEntityId(instance1, entityId, idFieldName);
                setEntityId(instance2, entityId, idFieldName);

                Method hashCodeMethod = entityClass.getMethod("hashCode");

                // Compare hash codes
                assertEquals(hashCodeMethod.invoke(instance1), hashCodeMethod.invoke(instance2),
                        () -> entityClass.getName() + " hashCode method should be based on '" + idFieldName + "' field");

                // Change non-id fields and verify hash code is unaffected
                List<Field> fields = Arrays.stream(entityClass.getDeclaredFields())
                        .filter(field -> !field.getName().equals(idFieldName))
                        .toList();

                for (Field field : fields) {
                    setDummyValue(field, instance1);
                }

                assertEquals(hashCodeMethod.invoke(instance1), hashCodeMethod.invoke(instance2),
                        () -> entityClass.getName() + " hashCode method should not be affected by non-id fields");

                // Change ID and verify hash code change
                setEntityId(instance2, UUID.randomUUID(), idFieldName);

                assertNotEquals(hashCodeMethod.invoke(instance1), hashCodeMethod.invoke(instance2),
                        () -> entityClass.getName() + " hashCode method should reflect changes in '" + idFieldName + "' field");

            } catch (Exception e) {
                fail("Test " + entityClass.getName() + " fail!", e);
            }
        }
    }

    @Nested
    @DisplayName("Collections Initialization Test")
    class CollectionsInitializationTest {

        @Test
        @DisplayName("Check if collection fields are initialized in constructors")
        public void testCollectionsInitialization() {
            for (Class<?> entityClass : entityClasses) {
                validateCollectionsInitialization(entityClass);
            }
        }

        private void validateCollectionsInitialization(Class<?> entityClass) {
            try {
                Object instance = entityClass.getConstructor().newInstance();

                List<Field> collectionFields = Arrays.stream(entityClass.getDeclaredFields())
                        .filter(field -> Collection.class.isAssignableFrom(field.getType()) || Map.class.isAssignableFrom(field.getType()))
                        .toList();

                for (Field field : collectionFields) {
                    field.setAccessible(true);
                    Object value = field.get(instance);

                    assertNotNull(value, () -> entityClass.getName() + " collection field '" + field.getName() + "' is not initialized (null)");

                    if (value instanceof Collection<?>) {
                        assertTrue(((Collection<?>) value).isEmpty(),
                                () -> entityClass.getName() + " collection field '" + field.getName() + "' should be empty after initialization");
                    } else if (value instanceof Map<?, ?>) {
                        assertTrue(((Map<?, ?>) value).isEmpty(),
                                () -> entityClass.getName() + " map field '" + field.getName() + "' should be empty after initialization");
                    }
                }
            } catch (Exception e) {
                fail("Error validating collections initialization for " + entityClass.getName(), e);
            }
        }
    }

    @Nested
    @DisplayName("ToString Method Test")
    class ToStringMethodTest {

        @Test
        @DisplayName("Check if toString() method returns non-null and non-empty string")
        public void testToStringMethod() {
            for (Class<?> entityClass : entityClasses) {
                validateToStringMethod(entityClass);
            }
        }

        private void validateToStringMethod(Class<?> entityClass) {
            try {
                Object instance = entityClass.getConstructor().newInstance();
                Method toStringMethod = entityClass.getMethod("toString");

                String result = (String) toStringMethod.invoke(instance);

                assertNotNull(result, () -> entityClass.getName() + " toString() returned null");
                assertFalse(result.trim().isEmpty(), () -> entityClass.getName() + " toString() returned empty string");
                assertTrue(result.contains(entityClass.getSimpleName()) || result.contains("id"),
                        () -> entityClass.getName() + " toString() should mention class name or id");

            } catch (Exception e) {
                fail("Error validating toString method for " + entityClass.getName(), e);
            }
        }
    }

    private void setEntityId(Object instance, UUID id, String idFieldName) throws NoSuchFieldException, IllegalAccessException {
        Field idField = instance.getClass().getSuperclass().getDeclaredField(idFieldName);
        idField.setAccessible(true);
        idField.set(instance, id);
    }

    private void setDummyValue(Field field, Object instance) throws IllegalAccessException, NoSuchFieldException {
        field.setAccessible(true);
        if (field.getType().equals(String.class)) {
            field.set(instance, "TestString");
        } else if (field.getType().equals(int.class) || field.getType().equals(Integer.class)) {
            field.set(instance, 123);
        } else if (field.getType().equals(AppUser.class)) {
            AppUser appUser = new AppUser();
            Field appUserIdField = AppUser.class.getSuperclass().getDeclaredField("id");
            appUserIdField.setAccessible(true);
            appUserIdField.set(appUser, UUID.randomUUID());
            field.set(instance, appUser);
        }
    }

    private String getIdFieldName(Class<?> entityClass) {
        return Arrays.stream(entityClass.getSuperclass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .map(Field::getName)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Entity " + entityClass.getName() + " does not have annotation for ID field"));
    }

    private String capitalize(String str) {
        return StringUtils.capitalize(str);
    }
}
