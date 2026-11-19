package tn.reclamationapp.model;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NestedMapAdapter extends XmlAdapter<NestedMapAdapter.AdaptedNestedMapList, Map<String, Map<String, Long>>> {

    public static class AdaptedNestedMap {
        public String key;
        public List<MapAdapter.AdaptedMap> values = new ArrayList<>();
    }

    public static class AdaptedNestedMapList {
        public List<AdaptedNestedMap> entries = new ArrayList<>();
    }

    @Override
    public Map<String, Map<String, Long>> unmarshal(AdaptedNestedMapList adaptedNestedMapList) {
        Map<String, Map<String, Long>> map = new HashMap<>();
        for (AdaptedNestedMap entry : adaptedNestedMapList.entries) {
            Map<String, Long> nestedMap = new HashMap<>();
            for (MapAdapter.AdaptedMap adaptedEntry : entry.values) {
                nestedMap.put(adaptedEntry.key, adaptedEntry.value);
            }
            map.put(entry.key, nestedMap);
        }
        return map;
    }

    @Override
    public AdaptedNestedMapList marshal(Map<String, Map<String, Long>> map) {
        AdaptedNestedMapList adaptedNestedMapList = new AdaptedNestedMapList();
        for (Map.Entry<String, Map<String, Long>> entry : map.entrySet()) {
            AdaptedNestedMap adaptedEntry = new AdaptedNestedMap();
            adaptedEntry.key = entry.getKey();
            adaptedEntry.values = new ArrayList<>();
            for (Map.Entry<String, Long> subEntry : entry.getValue().entrySet()) {
                MapAdapter.AdaptedMap adaptedSubEntry = new MapAdapter.AdaptedMap();
                adaptedSubEntry.key = subEntry.getKey();
                adaptedSubEntry.value = subEntry.getValue();
                adaptedEntry.values.add(adaptedSubEntry);
            }
            adaptedNestedMapList.entries.add(adaptedEntry);
        }
        return adaptedNestedMapList;
    }
}
