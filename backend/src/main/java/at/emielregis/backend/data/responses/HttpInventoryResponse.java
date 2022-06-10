package at.emielregis.backend.data.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HttpInventoryResponse {
    // CLASS-ID, AMOUNT
    private final HashMap<String, Integer> inventoryByIds = new HashMap<>();
    // CLASS-ID, NAME
    private final HashMap<String, String> descriptionsByClassIds = new HashMap<>();
    // NAME, TYPE
    private final HashMap<String, String> typeByClassIds = new HashMap<>();

    @JsonProperty("success")
    private Integer success;

    @JsonProperty("more_items")
    private Integer hasMoreItems;

    @JsonProperty("last_assetid")
    private String lastAssetId;

    @JsonProperty("descriptions")
    private void unpackNested(List<Map<String, Object>> assets) {
        assets.forEach(map -> {
            AtomicReference<String> name = new AtomicReference<>();
            AtomicReference<String> type = new AtomicReference<>();
            AtomicReference<String> classId = new AtomicReference<>();
            map.forEach((key, value) -> {
                if (key.equals("name")) {
                    name.set((String) value);
                }
                if (key.equals("classid")) {
                    classId.set((String) value);
                }
                if (key.equals("type")) {
                    type.set((String) value);
                }
            });
            descriptionsByClassIds.put(classId.get(), name.get());
            typeByClassIds.put(name.get(), type.get());
        });
    }

    @JsonProperty("assets")
    private void unpackAssets(List<Map<String, Object>> assets) {
        assets.forEach(map -> map.forEach((key, value) -> {
            if (key.equals("classid")) {
                String itemName = (String) value;
                if (inventoryByIds.containsKey(itemName)) {
                    inventoryByIds.put(itemName, inventoryByIds.get(itemName) + 1);
                } else {
                    inventoryByIds.put(itemName, 1);
                }
            }
        })
        );
    }

    public Map<String, Integer> getInventory() {
        Map<String, Integer> inventory = new HashMap<>();

        inventoryByIds.forEach((key, value) -> {
                String name = descriptionsByClassIds.get(key);
                if (inventory.containsKey(name)) {
                    inventory.put(name, inventory.get(name) + value);
                } else {
                    inventory.put(name, value);
                }
            }
        );

        return inventory;
    }

    public Map<String, String> getTypes() {
        return typeByClassIds;
    }

    public boolean hasMoreItems() {
        return hasMoreItems != null && hasMoreItems == 1;
    }

    public String getLastAssetId() {
        return lastAssetId;
    }

    public boolean successful() {
        return success != null && success == 1;
    }
}
