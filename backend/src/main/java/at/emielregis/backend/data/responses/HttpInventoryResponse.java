package at.emielregis.backend.data.responses;

import at.emielregis.backend.data.dtos.TransientClassId;
import at.emielregis.backend.data.dtos.TransientItem;
import at.emielregis.backend.data.dtos.TransientItemCategory;
import at.emielregis.backend.data.dtos.TransientItemSet;
import at.emielregis.backend.data.dtos.TransientSticker;
import at.emielregis.backend.data.enums.Exterior;
import at.emielregis.backend.data.enums.Rarity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HttpInventoryResponse {
    private final HashMap<TransientClassId, TransientItem> items = new HashMap<>();
    private final HashMap<String, TransientItemCategory> types = new HashMap<>();

    @JsonProperty("success")
    private Integer success;

    @JsonProperty("more_items")
    private Integer hasMoreItems;

    @JsonProperty("last_assetid")
    private String lastAssetId;

    /**
     * Unpacks the class ids of the response. A class id describes the class of the item - all items with the same class
     * id have the same base properties such as exterior, collection, name. Here the amount of items of each type is counted
     * so equal items are grouped.
     *
     * @param assets The map of asset fields from the JSON response.
     */
    @JsonProperty("assets")
    private void unpackAssets(List<Map<String, Object>> assets) {
        assets.forEach(map -> map.forEach((key, value) -> {
                if (key.equals("classid")) {
                    String itemName = (String) value;
                    if (items.containsKey(TransientClassId.of(itemName))) {
                        items.put(TransientClassId.of(itemName), items.get((TransientClassId.of(itemName))).increaseAmount());
                    } else {
                        items.put(TransientClassId.of(itemName), TransientItem.builder().amount(1).build());
                    }
                }
            })
        );
    }

    /**
     * Unpacks the detailed descriptions of all the items in the JSON response and creates TransientItem instances
     * for each unique class id.
     *
     * @param descriptions The map of descriptions from the JSON response.
     */
    @JsonProperty("descriptions")
    private void unpackNested(List<Map<String, Object>> descriptions) {
        descriptions.forEach(map -> {
            final AtomicReference<String> atomicName = new AtomicReference<>();
            final AtomicReference<String> atomicType = new AtomicReference<>();
            final AtomicReference<String> atomicClassId = new AtomicReference<>();
            final AtomicReference<String> atomicNameTag = new AtomicReference<>();
            final AtomicReference<Boolean> atomicTradable = new AtomicReference<>();
            final AtomicReference<Boolean> atomicStatTrak = new AtomicReference<>(false);
            final AtomicReference<Boolean> atomicSouvenir = new AtomicReference<>(false);
            final AtomicReference<Exterior> atomicExterior = new AtomicReference<>();
            final AtomicReference<String> atomicItemSet = new AtomicReference<>();
            final AtomicReference<Rarity> atomicRarity = new AtomicReference<>();
            final AtomicReference<Integer> atomicStorageUnitAmount = new AtomicReference<>();
            final AtomicReference<List<TransientSticker>> atomicStickers = new AtomicReference<>();
            map.forEach((key, value) -> {
                if (key.equals("name")) {
                    atomicName.set((String) value);
                }
                if (key.equals("classid")) {
                    atomicClassId.set((String) value);
                }
                if (key.equals("type")) {
                    atomicType.set((String) value);
                }
                if (key.equals("fraudwarnings")) {
                    String nameTag = ((List<String>) value).get(0);
                    atomicNameTag.set(nameTag.substring(12, nameTag.length() - 2));
                }
                if (key.equals("tradable")) {
                    atomicTradable.set(((Integer) value) == 1);
                }
                if (key.equals("descriptions")) {
                    List<Map<String, Object>> maps = (List<Map<String, Object>>) value;
                    maps.forEach(map1 -> map1.forEach((key1, value1) -> {
                        if (key1.equals("value")) {
                            String value2 = (String) value1;
                            if (value2.contains("sticker_info")) {

                                // the sticker data is only sent as html for displaying it so I have to manually
                                // extract the sticker names from the html
                                List<TransientSticker> stickers = new ArrayList<>();
                                if (value2.contains("<br>Sticker:")) {
                                    value2 = value2.substring(value2.indexOf("<br>Sticker:")).substring(4);
                                    value2 = value2.substring(0, value2.indexOf("</center>"));
                                    Arrays.stream(value2.split(",")).forEach(
                                        stickerName -> {
                                            stickerName = stickerName.trim();
                                            stickers.add(TransientSticker.builder().name(stickerName).build());
                                        }
                                    );
                                    atomicStickers.set(stickers);
                                }
                            }
                            if (value2.startsWith("Number of Items: ")) {
                                atomicStorageUnitAmount.set(Integer.parseInt(value2.substring(17).trim()));
                            }
                        }
                    }));
                }
                if (key.equals("tags")) {
                    List<Map<String, Object>> maps = (List<Map<String, Object>>) value;
                    maps.forEach(map1 -> {
                        String category = (String) map1.get("category");
                        switch (category) {
                            case "ItemSet", "StickerCapsule", "PatchCapsule" -> atomicItemSet.set((String) map1.get("localized_tag_name"));
                            case "Rarity" -> atomicRarity.set(Rarity.of((String) map1.get("localized_tag_name")));
                            case "Exterior" -> atomicExterior.set(Exterior.of((String) map1.get("localized_tag_name")));
                            case "Quality" -> {
                                String quality = (String) map1.get("localized_tag_name");
                                if (quality.startsWith("StatTrak")) {
                                    atomicStatTrak.set(true);
                                } else if (quality.startsWith("Souvenir")) {
                                    atomicSouvenir.set(true);
                                }
                            }
                        }
                    });
                }
            });

            // The item is created in the unpackAssets() method
            TransientClassId transientClassId = TransientClassId.of(atomicClassId.get());
            TransientItem item = items.get(transientClassId);

            // Set parameters
            item.setClassID(atomicClassId.get());
            parseName(atomicName, item);
            parseType(atomicType, item);
            item.setNameTag(atomicNameTag.get());
            item.setExterior(atomicExterior.get());
            item.setStickers(atomicStickers.get());
            item.setTradable(atomicTradable.get());
            item.setStatTrak(atomicStatTrak.get());
            item.setSouvenir(atomicSouvenir.get());
            item.setRarity(atomicRarity.get());
            item.setAmountStorageUnit(atomicStorageUnitAmount.get());

            // not all items have an item set
            if (atomicItemSet.get() != null) {
                item.setItemSet(TransientItemSet.builder().name(atomicItemSet.get()).build());
            }


            items.put(transientClassId, item);
        });
    }

    /**
     * Parses the name of the item.
     *
     * @param atomicName The atomic name.
     * @param item The item for which to set the name.
     */
    private void parseName(AtomicReference<String> atomicName, TransientItem item) {
        String name = atomicName.get();
        name = prune(name);
        item.setName(name);
    }

    /**
     * Parses the type of the item.
     *
     * @param atomicType The atomic type.
     * @param item The item for which to set the type.
     */
    private void parseType(AtomicReference<String> atomicType, TransientItem item) {
        String type = atomicType.get();
        type = prune(type);
        if (types.get(type) != null) {
            item.setCategory(types.get(type));
        } else {
            TransientItemCategory transientItemCategory = TransientItemCategory.builder().name(type).build();
            types.put(type, transientItemCategory);
            item.setCategory(transientItemCategory);
        }
    }

    /**
     * Prunes the string of StatTrak and Souvenir prefixes.
     * If there is nothing the prune the original string is returned.
     *
     * @param string The string to be pruned.
     * @return The pruned string.
     */
    private String prune(String string) {
        if (string.startsWith("StatTrak™ ")) {
            return string.substring(10);
        }
        if (string.startsWith("★ StatTrak™ ")) {
            return string.substring(12);
        }
        if (string.startsWith("Souvenir ")) {
            return string.substring(9);
        }
        return string;
    }

    /**
     * Returns the transient items of the inventory requests.
     *
     * @return The transient items.
     */
    public List<TransientItem> getTransientItems() {
        return items.values().stream().toList();
    }

    /**
     * Returns whether there are more items in the users inventory after this request.
     *
     * @return true if there more items, false otherwise.
     */
    public boolean hasMoreItems() {
        return hasMoreItems != null && hasMoreItems == 1;
    }

    /**
     * Returns the asset id of the last item of this request.
     *
     * @return The last asset id if one exists, null otherwise.
     */
    public String getLastAssetId() {
        return lastAssetId;
    }

    public boolean successful() {
        return success != null && success == 1;
    }
}
