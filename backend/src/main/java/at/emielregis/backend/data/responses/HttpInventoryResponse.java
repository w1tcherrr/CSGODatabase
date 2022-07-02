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
                        }
                    }));
                }
                if (key.equals("tags")) {
                    List<Map<String, Object>> maps = (List<Map<String, Object>>) value;
                    maps.forEach(map1 -> {
                        String category = (String) map1.get("category");
                        switch (category) {
                            case "ItemSet" -> atomicItemSet.set((String) map1.get("localized_tag_name"));
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
            if (atomicItemSet.get() != null) {
                item.setItemSet(TransientItemSet.builder().name(atomicItemSet.get()).build());
            }
            item.setRarity(atomicRarity.get());

            items.put(transientClassId, item);
        });
    }

    private void parseName(AtomicReference<String> atomicName, TransientItem item) {
        String name = atomicName.get();
        name = prune(name);
        item.setName(name);
    }

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

    private String prune(String type) {
        if (type.startsWith("StatTrak™ ")) {
            return type.substring(10);
        }
        if (type.startsWith("★ StatTrak™ ")) {
            return type.substring(12);
        }
        if (type.startsWith("Souvenir ")) {
            return type.substring(9);
        }
        return type;
    }

    public List<TransientItem> getInventory() {
        return items.values().stream().toList();
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
