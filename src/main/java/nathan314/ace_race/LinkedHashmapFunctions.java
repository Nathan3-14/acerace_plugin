package nathan314.ace_race;

import java.util.LinkedHashMap;

public class LinkedHashmapFunctions {
    public static <K, V> int indexOfKey(LinkedHashMap<K, V> map, K lookfor_key) {
        int index = 0;
        for (K key : map.keySet()) {
            if (key.equals(lookfor_key)) return index;
            index++;
        }
        return -1;
    }

    public static <K, V> K getKeyAtIndex(LinkedHashMap<K, V> map, int index) {
        int i = 0;
        for (K key : map.keySet()) {
            if (i == index) return key;
            i++;
        }
        return null;
    }

    public static <K, V> V getValueAtIndex(LinkedHashMap<K, V> map, int index) {
        K key = getKeyAtIndex(map, index);
        if (key == null) {
            return null;
        }
        return map.get(key);
    }
}
