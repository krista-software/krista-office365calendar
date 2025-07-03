# Example: Adding New Annotation Type with Strategy Pattern

## Scenario
Let's say we want to add support for a new `@Field.Email` annotation type that validates email addresses and has email-specific attributes.

## Before Strategy Pattern (Old Approach)
Adding a new annotation type would require changes in **3 different places**:

### 1. Update `extractFieldAttributes()` method
```java
// Would need to add this to the existing if-statement chain
Field.Email emailAnnotation = parameter.getAnnotation(Field.Email.class);
if (emailAnnotation != null) {
    if (fieldName.equals(emailAnnotation.name())) {
        return extractAttributesFromEmailAnnotation(emailAnnotation);
    }
}
```

### 2. Update `extractFieldType()` method
```java
// Would need to add this to another if-statement chain
Field.Email emailAnnotation = parameter.getAnnotation(Field.Email.class);
if (emailAnnotation != null) {
    if (fieldName.equals(emailAnnotation.name())) {
        return "Email";
    }
}
```

### 3. Update `createFieldNameToResourceMapping()` method
```java
// Would need to add this to a third if-statement chain
Field.Email emailAnnotation = parameter.getAnnotation(Field.Email.class);
if (emailAnnotation != null) {
    fieldName = emailAnnotation.name();
    resource = mapFieldTypeToValidationResource("Email", fieldName);
}
```

### 4. Add private helper method
```java
private static Map<String, Object> extractAttributesFromEmailAnnotation(Field.Email emailAnnotation) {
    // Implementation...
}
```

**Total**: 4 places to modify, scattered across the file.

## After Strategy Pattern (New Approach)
Adding a new annotation type requires changes in **only 2 places**:

### 1. Create New Strategy Class
**File**: `EmailFieldAnnotationStrategy.java`
```java
package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators.strategy;

import app.krista.extension.impl.anno.Attribute;
import app.krista.extension.impl.anno.Field;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.validators.Validator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

/**
 * Strategy for handling @Field.Email annotations.
 */
public class EmailFieldAnnotationStrategy implements FieldAnnotationStrategy {

    @Override
    public boolean canHandle(Parameter parameter, String fieldName) {
        Field.Email emailAnnotation = parameter.getAnnotation(Field.Email.class);
        return emailAnnotation != null && fieldName.equals(emailAnnotation.name());
    }

    @Override
    public Map<String, Object> extractAttributes(Parameter parameter, String fieldName) {
        Field.Email emailAnnotation = parameter.getAnnotation(Field.Email.class);
        if (emailAnnotation == null || !fieldName.equals(emailAnnotation.name())) {
            return Map.of();
        }

        Map<String, Object> attributes = new HashMap<>();
        
        // Add email-specific attributes
        if (emailAnnotation.validateDomain()) {
            attributes.put("validateDomain", "true");
        }
        
        if (!emailAnnotation.allowMultiple()) {
            attributes.put("allowMultiple", "false");
        }
        
        // Add custom attributes
        for (Attribute attribute : emailAnnotation.attributes()) {
            attributes.put(attribute.name(), attribute.value());
        }
        
        return attributes;
    }

    @Override
    public String extractFieldType(Parameter parameter, String fieldName) {
        Field.Email emailAnnotation = parameter.getAnnotation(Field.Email.class);
        if (emailAnnotation == null || !fieldName.equals(emailAnnotation.name())) {
            return null;
        }
        return "Email";
    }

    @Override
    public Validator.ValidationResource mapToValidationResource(Parameter parameter, String fieldName) {
        Field.Email emailAnnotation = parameter.getAnnotation(Field.Email.class);
        if (emailAnnotation == null || !fieldName.equals(emailAnnotation.name())) {
            return null;
        }
        
        // Map email fields to EMAIL validation resource
        return Validator.ValidationResource.EMAIL;
    }

    @Override
    public Class<? extends Annotation> getAnnotationType() {
        return Field.Email.class;
    }

    @Override
    public String getStrategyName() {
        return "EmailField";
    }
}
```

### 2. Register Strategy in Registry
**File**: `FieldAnnotationStrategyRegistry.java`
```java
public FieldAnnotationStrategyRegistry() {
    this.strategies = List.of(
        new BasicFieldAnnotationStrategy(),
        new DateFieldAnnotationStrategy(),
        new TextFieldAnnotationStrategy(),
        new BooleanFieldAnnotationStrategy(),
        new DescFieldAnnotationStrategy(),
        new EmailFieldAnnotationStrategy()  // ← Just add this line!
    );
}
```

**Total**: 2 places to modify, well-organized and contained.

## Comparison

| Aspect | Before (If-Statements) | After (Strategy Pattern) |
|--------|----------------------|-------------------------|
| **Files to modify** | 1 file, 4 locations | 2 files, 2 locations |
| **Code organization** | Scattered across methods | Centralized in strategy class |
| **Testing** | Hard to test in isolation | Easy to unit test strategy |
| **Code reuse** | Duplicated logic patterns | Reusable strategy interface |
| **Maintainability** | High coupling, hard to maintain | Low coupling, easy to maintain |
| **Risk of bugs** | High (multiple places to update) | Low (single strategy to implement) |

## Benefits Demonstrated

### ✅ **Reduced Complexity**
- **Before**: 4 scattered modifications across existing methods
- **After**: 1 self-contained strategy class + 1 line registration

### ✅ **Better Organization**
- **Before**: Email logic mixed with other annotation logic
- **After**: Email logic isolated in dedicated class

### ✅ **Easier Testing**
- **Before**: Must test through main extractor methods
- **After**: Can unit test `EmailFieldAnnotationStrategy` independently

### ✅ **Lower Risk**
- **Before**: Easy to forget updating one of the 4 locations
- **After**: Impossible to forget - strategy handles all aspects

### ✅ **Cleaner Code**
- **Before**: Long if-statement chains get longer
- **After**: Registry automatically handles new strategy

## Conclusion

The Strategy pattern refactoring transforms adding new annotation types from a **complex, error-prone process** requiring changes in multiple scattered locations into a **simple, safe process** requiring only a new strategy class and one-line registration.

This demonstrates the power of good design patterns in making code more maintainable and extensible.
