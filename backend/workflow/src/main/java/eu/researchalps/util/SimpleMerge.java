package eu.researchalps.util;

import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;

/**
 *
 */
public class SimpleMerge {
    /**
     * Merge only simples attributes of two objects (instances of String, Enum, Number, Boolean or primitive types)
     *
     * @param target The target (not null)
     * @param source The source (nullable)
     * @param <E> The type of merge
     * @return true if anything has been altered in target
     * @throws IllegalArgumentException if target is null
     */
    public static <E> boolean mergeSimpleAttributes(E target, E source) {
        if(source == null)
            return false;
        if(target == null) {
            throw new IllegalArgumentException("Can't merge into a null value");
        }
        try {
            boolean hasChanged = false;

            for (PropertyDescriptor f : BeanUtils.getPropertyDescriptors(target.getClass())) {
                Method readMethod = f.getReadMethod();
                Class<?> c = f.getPropertyType();
                // not assignable
                if(f.getWriteMethod() == null || readMethod == null)
                    continue;
                // not a primitive type
                if (!(String.class.isAssignableFrom(c) || Enum.class.isAssignableFrom(c)
                        || Number.class.isAssignableFrom(c) || Boolean.class.isAssignableFrom(c)
                        || Date.class.isAssignableFrom(c)
                        || c.isPrimitive()))
                    continue;
                Object sourceField = readMethod.invoke(source);
                if (sourceField == null)
                    continue;
                Object targetField = readMethod.invoke(target);
                if (sourceField.equals(targetField))
                    continue;
                hasChanged = true;
                Method writeMethod = f.getWriteMethod();
                writeMethod.invoke(target, sourceField);
            }
            return hasChanged;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }
}
