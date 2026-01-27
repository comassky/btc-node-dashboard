/**
 * Deeply merges a source object into a target object.
 * This function is designed to work with Vue's reactive objects.
 * Uses fast path for simple objects without nested properties.
 *
 * @param target The target object to merge into.
 * @param source The source object to merge from.
 * @returns The merged target object.
 */
export function deepMerge<T extends object, S extends object>(target: T, source: S): T & S {
  const isObject = (obj: any): obj is object =>
    obj !== null && typeof obj === 'object' && !Array.isArray(obj);

  if (!isObject(target) || !isObject(source)) {
    return target as T & S;
  }

  const targetAsRecord = target as Record<string, any>;
  const sourceKeys = Object.keys(source);

  // Fast path: check if we can use shallow merge
  let hasNestedObjects = false;
  for (const key of sourceKeys) {
    const sourceValue = (source as any)[key];
    if (isObject(sourceValue) && !Array.isArray(sourceValue)) {
      hasNestedObjects = true;
      break;
    }
  }

  // Use Object.assign for simple shallow merges (faster)
  if (!hasNestedObjects) {
    return Object.assign(target, source);
  }

  // Deep merge for complex objects
  for (const key of sourceKeys) {
    const sourceValue = (source as any)[key];
    const targetValue = targetAsRecord[key];

    if (Array.isArray(sourceValue)) {
      // Optimize: avoid unnecessary slice if arrays are identical
      targetAsRecord[key] = sourceValue;
    } else if (isObject(sourceValue) && isObject(targetValue)) {
      targetAsRecord[key] = deepMerge(targetValue, sourceValue);
    } else if (sourceValue !== undefined) {
      targetAsRecord[key] = sourceValue;
    }
  }

  return target as T & S;
}
