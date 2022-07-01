package at.emielregis.backend.data.responses;

import at.emielregis.backend.data.dtos.TransientClassId;
import at.emielregis.backend.data.dtos.TransientItem;
import at.emielregis.backend.data.dtos.TransientItemCategory;
import at.emielregis.backend.data.dtos.TransientSticker;
import at.emielregis.backend.data.enums.Exterior;
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
            AtomicReference<String> atomicName = new AtomicReference<>();
            AtomicReference<String> atomicType = new AtomicReference<>();
            AtomicReference<String> atomicClassId = new AtomicReference<>();
            AtomicReference<String> atomicNameTag = new AtomicReference<>();
            AtomicReference<Boolean> atomicTradable = new AtomicReference<>();
            AtomicReference<Exterior> atomicExterior = new AtomicReference<>();
            AtomicReference<List<TransientSticker>> atomicStickers = new AtomicReference<>();
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
                            if (((String) value1).startsWith("Exterior: ")) {
                                atomicExterior.set(Exterior.of(((String) value1).substring(10)));
                            }

                            if (((String) value1).contains("sticker_info")) {
                                List<TransientSticker> stickers = new ArrayList<>();
                                String html = (String) value1;
                                if (html.contains("<br>Sticker:")) {
                                    html = html.substring(html.indexOf("<br>Sticker:")).substring(4);
                                    html = html.substring(0, html.indexOf("</center>"));
                                    Arrays.stream(html.split(",")).forEach(
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
            });

            TransientClassId transientClassId = TransientClassId.of(atomicClassId.get());
            TransientItem item = items.get(transientClassId);

            item.setClassID(atomicClassId.get());

            String name = atomicName.get();
            if (name.startsWith("StatTrak™ ")) {
                name = name.substring(10);
                item.setStatTrak(true);
            }
            if (name.startsWith("★ StatTrak™ ")) {
                name = name.substring(12);
                item.setStatTrak(true);
            }
            if (name.startsWith("Souvenir ")) {
                name = name.substring(9);
                item.setSouvenir(true);
            }
            item.setName(name);

            String nametag = atomicNameTag.get();
            item.setNameTag(nametag);

            Exterior exterior = atomicExterior.get();
            item.setExterior(exterior);

            List<TransientSticker> stickers = atomicStickers.get();
            item.setStickers(stickers);

            boolean tradable = atomicTradable.get();
            item.setTradable(tradable);

            String type = atomicType.get();
            if (type.startsWith("StatTrak™ ")) {
                type = type.substring(10);
                item.setStatTrak(true);
            }
            if (type.startsWith("★ StatTrak™ ")) {
                type = type.substring(12);
                item.setStatTrak(true);
            }
            if (type.startsWith("Souvenir ")) {
                type = type.substring(9);
                item.setSouvenir(true);
            }
            if (types.get(type) != null) {
                item.setCategory(types.get(type));
            } else {
                TransientItemCategory transientItemCategory = TransientItemCategory.builder().name(type).build();
                types.put(type, transientItemCategory);
                item.setCategory(transientItemCategory);
            }

            items.put(transientClassId, item);
        });
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
