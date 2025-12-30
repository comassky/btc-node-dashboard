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
    obj !== null && typeof obj === 'object' && !Array.isArray(obj);

  if (!isObject(target) || !isObject(source)) {
    return target as T & S;
  }

  const targetAsRecord = target as Record<string, any>;

  for (const key of Object.keys(source)) {
    const sourceValue = (source as any)[key];
    const targetValue = targetAsRecord[key];

    if (Array.isArray(sourceValue) && Array.isArray(targetValue)) {
      // Option: replace arrays (could be changed to concat or deep merge if needed)
      targetAsRecord[key] = sourceValue.slice();
    } else if (isObject(sourceValue) && isObject(targetValue)) {
      targetAsRecord[key] = deepMerge(targetValue, sourceValue);
    } else if (sourceValue !== undefined) {
      targetAsRecord[key] = sourceValue;
    }
  }

  return target as T & S;
}
