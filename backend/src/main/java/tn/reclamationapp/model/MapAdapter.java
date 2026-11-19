package tn.reclamationapp.model;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapAdapter extends XmlAdapter<MapAdapter.AdaptedMapList, Map<String, Long>> {

    public static class AdaptedMap {
        public String key;
        public Long value;
    }

    public static class AdaptedMapList {
        public List<AdaptedMap> entries = new ArrayList<>();
    }

    @Override
    public Map<String, Long> unmarshal(AdaptedMapList adaptedMapList) throws Exception {
        Map<String, Long> map = new HashMap<>();
        for (AdaptedMap entry : adaptedMapList.entries) {
            map.put(entry.key, entry.value);
        }
        return map;
    }

    @Override
    public AdaptedMapList marshal(Map<String, Long> map) throws Exception {
        AdaptedMapList adaptedMapList = new AdaptedMapList();
        for (Map.Entry<String, Long> entry : map.entrySet()) {
            AdaptedMap adaptedEntry = new AdaptedMap();
            adaptedEntry.key = entry.getKey();
            adaptedEntry.value = entry.getValue();
            adaptedMapList.entries.add(adaptedEntry);
        }
        return adaptedMapList;
    }
}
