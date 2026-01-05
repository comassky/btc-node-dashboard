import { ref, computed, type Ref } from 'vue';

/**
 * Composable for sortable table functionality
 * @template T - The type of items in the table
 * @template K - The type of keys that can be used for sorting
 */
export function useSortableTable<T extends Record<string, any>, K extends keyof T>(
  items: Ref<T[]>,
  defaultSortKey: K,
  defaultSortOrder: 'asc' | 'desc' = 'asc'
) {
  const sortKey = ref<K>(defaultSortKey);
  const sortOrder = ref<'asc' | 'desc'>(defaultSortOrder);

  function setSort(key: K) {
    if (sortKey.value === key) {
      sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc';
    } else {
      sortKey.value = key;
      sortOrder.value = 'asc';
    }
  }

  function compare<V>(a: V, b: V, order: 'asc' | 'desc'): number {
    if (a == null && b == null) return 0;
    if (a == null) return 1;
    if (b == null) return -1;
    if (typeof a === 'number' && typeof b === 'number') {
      return order === 'asc' ? a - b : b - a;
    }
    return order === 'asc'
      ? String(a).localeCompare(String(b))
      : String(b).localeCompare(String(a));
  }

  const sortedItems = computed(() => {
    const key = sortKey.value;
    const order = sortOrder.value;
    return [...items.value].sort((a, b) => compare(a[key], b[key], order));
  });

  return {
    sortKey,
    sortOrder,
    sortedItems,
    setSort,
  };
}
