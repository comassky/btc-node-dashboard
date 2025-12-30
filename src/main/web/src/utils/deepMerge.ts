/**
 * Deeply merges a source object into a target object.
 * This function is designed to work with Vue's reactive objects.
 *
 * @param target The target object to merge into.
 * @param source The source object to merge from.
 * @returns The merged target object.
 */
export function deepMerge<T extends object, S extends object>(target: T, source: S): T & S {
  const isObject = (obj: any): obj is object =>
    obj && typeof obj === 'object' && !Array.isArray(obj);

  if (!isObject(target) || !isObject(source)) {
    // This case should not happen based on type constraints, but as a runtime safeguard.
    return target as T & S;
  }

  // Treat target as an indexable record for dynamic key assignment
  const targetAsRecord = target as Record<string, any>;

  for (const key in source) {
    if (Object.prototype.hasOwnProperty.call(source, key)) {
      const sourceValue = (source as any)[key];
      const targetValue = targetAsRecord[key];

      if (isObject(sourceValue) && isObject(targetValue)) {
        // Recurse for nested objects
        targetAsRecord[key] = deepMerge(targetValue, sourceValue);
      } else if (sourceValue !== undefined) {
        // Assign primitive values, arrays, or if target property is not an object
        targetAsRecord[key] = sourceValue;
      }
    }
  }

  return target as T & S;
}
