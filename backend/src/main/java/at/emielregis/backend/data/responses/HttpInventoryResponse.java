package at.emielregis.backend.data.responses;

import at.emielregis.backend.data.entities.items.*;
import at.emielregis.backend.data.enums.Exterior;
import at.emielregis.backend.data.enums.Rarity;
import at.emielregis.backend.data.enums.SpecialItemType;
import at.emielregis.backend.data.enums.StickerType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HttpInventoryResponse {

    private record UniqueItemTypeIdentifier(String classId, String instanceId) {

    }

    private static final List<String[]> stickerNamesWithCommas =
        List.of(
            new String[]{"Don't Worry", " I'm Pro"},
            new String[]{"Hi", " My Game Is"},
            new String[]{"Rock", " Paper", " Scissors (Foil)"},
            new String[]{"Run CT", " Run"},
            new String[]{"Run T", " Run"},
            new String[]{"Twistzz (Gold", " Champion) | Antwerp 2022"},
            new String[]{"Twistzz (Holo", " Champion) | Antwerp 2022"},
            new String[]{"Twistzz (Glitter", " Champion) | Antwerp 2022"},
            new String[]{"ropz (Gold", " Champion) | Antwerp 2022"},
            new String[]{"ropz (Holo", " Champion) | Antwerp 2022"},
            new String[]{"ropz (Glitter", " Champion) | Antwerp 2022"},
            new String[]{"rain (Gold", " Champion) | Antwerp 2022"},
            new String[]{"rain (Holo", " Champion) | Antwerp 2022"},
            new String[]{"rain (Glitter", " Champion) | Antwerp 2022"},
            new String[]{"broky (Gold", " Champion) | Antwerp 2022"},
            new String[]{"broky (Holo", " Champion) | Antwerp 2022"},
            new String[]{"broky (Glitter", " Champion) | Antwerp 2022"},
            new String[]{"karrigan (Gold", " Champion) | Antwerp 2022"},
            new String[]{"karrigan (Holo", " Champion) | Antwerp 2022"},
            new String[]{"karrigan (Glitter", " Champion) | Antwerp 2022"},
            new String[]{"Jame (Gold", " Champion) | Rio 2022"},
            new String[]{"Jame (Holo", " Champion) | Rio 2022"},
            new String[]{"Jame (Glitter", " Champion) | Rio 2022"},
            new String[]{"fame (Gold", " Champion) | Rio 2022"},
            new String[]{"fame (Holo", " Champion) | Rio 2022"},
            new String[]{"fame (Glitter", " Champion) | Rio 2022"},
            new String[]{"FL1T (Gold", " Champion) | Rio 2022"},
            new String[]{"FL1T (Holo", " Champion) | Rio 2022"},
            new String[]{"FL1T (Glitter", " Champion) | Rio 2022"},
            new String[]{"qikert (Gold", " Champion) | Rio 2022"},
            new String[]{"qikert (Holo", " Champion) | Rio 2022"},
            new String[]{"qikert (Glitter", " Champion) | Rio 2022"},
            new String[]{"n0rb3r7 (Gold", " Champion) | Rio 2022"},
            new String[]{"n0rb3r7 (Holo", " Champion) | Rio 2022"},
            new String[]{"n0rb3r7 (Glitter", " Champion) | Rio 2022"},
            new String[]{"ZywOo (Gold", " Champion) | Paris 2023"},
            new String[]{"ZywOo (Holo", " Champion) | Paris 2023"},
            new String[]{"ZywOo (Glitter", " Champion) | Paris 2023"},
            new String[]{"dupreeh (Gold", " Champion) | Paris 2023"},
            new String[]{"dupreeh (Holo", " Champion) | Paris 2023"},
            new String[]{"dupreeh (Glitter", " Champion) | Paris 2023"},
            new String[]{"apEX (Gold", " Champion) | Paris 2023"},
            new String[]{"apEX (Holo", " Champion) | Paris 2023"},
            new String[]{"apEX (Glitter", " Champion) | Paris 2023"},
            new String[]{"Magisk (Gold", " Champion) | Paris 2023"},
            new String[]{"Magisk (Holo", " Champion) | Paris 2023"},
            new String[]{"Magisk (Glitter", " Champion) | Paris 2023"},
            new String[]{"Spinx (Gold", " Champion) | Paris 2023"},
            new String[]{"Spinx (Holo", " Champion) | Paris 2023"},
            new String[]{"Spinx (Glitter", " Champion) | Paris 2023"},
            new String[]{"b1t (Gold", " Champion) | Copenhagen 2024"},
            new String[]{"b1t (Holo", " Champion) | Copenhagen 2024"},
            new String[]{"b1t (Glitter", " Champion) | Copenhagen 2024"},
            new String[]{"w0nderful (Gold", " Champion) | Copenhagen 2024"},
            new String[]{"w0nderful (Holo", " Champion) | Copenhagen 2024"},
            new String[]{"w0nderful (Glitter", " Champion) | Copenhagen 2024"},
            new String[]{"jL (Gold", " Champion) | Copenhagen 2024"},
            new String[]{"jL (Holo", " Champion) | Copenhagen 2024"},
            new String[]{"jL (Glitter", " Champion) | Copenhagen 2024"},
            new String[]{"Aleksib (Gold", " Champion) | Copenhagen 2024"},
            new String[]{"Aleksib (Holo", " Champion) | Copenhagen 2024"},
            new String[]{"Aleksib (Glitter", " Champion) | Copenhagen 2024"},
            new String[]{"iM (Gold", " Champion) | Copenhagen 2024"},
            new String[]{"iM (Holo", " Champion) | Copenhagen 2024"},
            new String[]{"iM (Glitter", " Champion) | Copenhagen 2024"}
        );

    private final HashMap<UniqueItemTypeIdentifier, ItemCollection> items = new HashMap<>();

    @JsonProperty("success")
    private Integer success;

    @JsonProperty("more_items")
    private Integer hasMoreItems;

    @Getter
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
        assets.forEach(map -> {
            AtomicReference<String> currentClassId = new AtomicReference<>();
            AtomicReference<String> currentInstanceId = new AtomicReference<>();

            map.forEach((key, value) -> {
                if (key.equals("classid")) {
                    currentClassId.set((String) value);
                } else if (key.equals("instanceid")) {
                    currentInstanceId.set((String) value);
                }
            });

            // this should never happen unless there is a mistake in the api
            if (currentClassId.get() == null || currentInstanceId.get() == null) {
                throw new IllegalStateException("ClassId or CurrentInstanceId is null in response: " + assets);
            }

            UniqueItemTypeIdentifier identifier = new UniqueItemTypeIdentifier(currentClassId.get(), currentInstanceId.get());
            if (items.containsKey(identifier)) {
                items.put(identifier, ItemCollection.builder().amount(items.get(identifier).getAmount() + 1).build());
            } else {
                items.put(identifier, ItemCollection.builder().amount(1).build());
            }
        });
    }

    /**
     * Unpacks the detailed descriptions of all the items in the JSON response and creates TransientItem instances
     * for each unique class/instance id.
     *
     * @param descriptions The map of descriptions from the JSON response.
     */
    @JsonProperty("descriptions")
    private void unpackNested(List<Map<String, Object>> descriptions) {
        descriptions.forEach(map -> {
            final AtomicReference<String> atomicName = new AtomicReference<>();
            final AtomicReference<String> atomicCategory = new AtomicReference<>();
            final AtomicReference<String> atomicClassId = new AtomicReference<>();
            final AtomicReference<String> atomicInstanceId = new AtomicReference<>();
            final AtomicReference<String> atomicNameTag = new AtomicReference<>();
            final AtomicReference<Boolean> atomicStatTrak = new AtomicReference<>(false);
            final AtomicReference<Boolean> atomicSouvenir = new AtomicReference<>(false);
            final AtomicReference<Exterior> atomicExterior = new AtomicReference<>();
            final AtomicReference<String> atomicItemSet = new AtomicReference<>();
            final AtomicReference<Rarity> atomicRarity = new AtomicReference<>();
            final AtomicReference<List<Sticker>> atomicStickers = new AtomicReference<>();
            final AtomicReference<String> atomicMarketHashName = new AtomicReference<>();
            map.forEach((key, value) -> {
                switch (key) {
                    case "name" -> atomicName.set((String) value);
                    case "classid" -> atomicClassId.set((String) value);
                    case "instanceid" -> atomicInstanceId.set((String) value);
                    case "type" -> atomicCategory.set((String) value);
                    case "fraudwarnings" -> {
                        String nameTag = ((List<String>) value).get(0);
                        atomicNameTag.set(nameTag.substring(12, nameTag.length() - 2));
                    }
                    case "market_hash_name" -> atomicMarketHashName.set((String) value);
                    case "descriptions" -> {
                        List<Map<String, Object>> maps = (List<Map<String, Object>>) value;
                        maps.forEach(map1 -> map1.forEach((key1, value1) -> {
                            if (key1.equals("value")) {
                                String value2 = (String) value1;
                                if (value2.contains("sticker_info")) {
                                    int amountOfStickers = value2.split("<img").length - 1;

                                    // the sticker data is only sent as html for displaying it so I have to manually
                                    // extract the sticker names from the html
                                    List<Sticker> stickers = new ArrayList<>();
                                    if (value2.contains("<br>Sticker:")) {
                                        value2 = value2.substring(value2.indexOf("<br>Sticker:")).substring(12);
                                        value2 = value2.substring(0, value2.indexOf("</center>"));
                                        String[] split = value2.split(",");
                                        for (int i = 0; i < split.length; i++) {
                                            String stickerName = split[i];
                                            stickerName = stickerName.trim().replaceAll(" {2,}", " "); // some stickers return doubled spaces for no reason

                                            // some stickers include "," character
                                            for (String[] stickerNameWithCommas : stickerNamesWithCommas) {
                                                if (stickerName.equals(stickerNameWithCommas[0])) {

                                                    // the remaining length of the current stickerNameWithCommas that is checked
                                                    int remainingCurrentStickerLength = stickerNameWithCommas.length - 1;

                                                    // the remaining length in the sticker name returned by the api
                                                    int remainingPossibleLength = split.length - i - 1;

                                                    // if there is less length in the sticker names returned by the api
                                                    // than in the current stickerNameWithCommas it can't be this specific sticker
                                                    if (remainingPossibleLength < remainingCurrentStickerLength) {
                                                        continue;
                                                    }

                                                    // we check all the next sections and compare whether the names are the same
                                                    // if they aren't -> the sticker is not correct
                                                    boolean correctSticker = true;
                                                    for (int j = i + 1; j < i + 1 + remainingCurrentStickerLength; j++) {
                                                        if (!split[j].equals(stickerNameWithCommas[j - i])) {
                                                            correctSticker = false;
                                                            break;
                                                        }
                                                    }

                                                    if (!correctSticker) {
                                                        continue;
                                                    }

                                                    // in this case the sticker names match. We join the name and return it.
                                                    stickerName = String.join(",", stickerNameWithCommas);
                                                    // remove all the next parts of the same name so they aren't stored twice
                                                    i += remainingCurrentStickerLength;
                                                }
                                            }

                                            stickers.add(Sticker.builder().name(stickerName).stickerType(StickerType.ofName(stickerName)).build());
                                        }

                                        if (stickers.size() != amountOfStickers) {
                                            throw new IllegalStateException("Amount of stickers does not match for string: " + value1);
                                        }

                                        atomicStickers.set(stickers);
                                    }
                                }
                            }
                        }));
                    }
                    case "tags" -> {
                        List<Map<String, Object>> maps = (List<Map<String, Object>>) value;
                        maps.forEach(map1 -> {
                            String category = (String) map1.get("category");
                            switch (category) {
                                case "ItemSet", "StickerCapsule", "PatchCapsule", "SprayCapsule" ->
                                    atomicItemSet.set((String) map1.get("localized_tag_name"));
                                case "Rarity" -> atomicRarity.set(Rarity.of((String) map1.get("localized_tag_name")));
                                case "Exterior" ->
                                    atomicExterior.set(Exterior.of((String) map1.get("localized_tag_name")));
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
                }
            });

            // The item is created in the unpackAssets() method
            UniqueItemTypeIdentifier identifier = new UniqueItemTypeIdentifier(atomicClassId.get(), atomicInstanceId.get());
            ItemCollection item = items.get(identifier);

            // Create the Item Type of the item collection
            ItemType.ItemTypeBuilder itemTypeBuilder = ItemType.builder();
            itemTypeBuilder.itemName(ItemName.builder().name(prune(atomicName.get())).build());
            itemTypeBuilder.exterior(atomicExterior.get());
            itemTypeBuilder.rarity(atomicRarity.get());
            itemTypeBuilder.marketHashName(atomicMarketHashName.get());
            String type = prune(atomicCategory.get());
            ItemCategory itemCategory = ItemCategory.builder().name(type).build();
            itemTypeBuilder.category(itemCategory);

            if (atomicItemSet.get() != null) {
                itemTypeBuilder.itemSet(ItemSet.builder().name(atomicItemSet.get()).build());
            }

            // this can't happen, but better to check it
            if (atomicSouvenir.get() && atomicStatTrak.get()) {
                throw new IllegalStateException("Item can't be both Souvenir and StatTrak at the same time!");
            }

            itemTypeBuilder.specialItemType(SpecialItemType.fromBooleans(atomicStatTrak.get(), atomicSouvenir.get()));

            item.setItemType(itemTypeBuilder.build());

            item.setNameTag(atomicNameTag.get());

            item.setStickers(atomicStickers.get());

            items.put(identifier, item);
        });
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
            return string.substring(0, 2) + string.substring(12);
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
    public List<ItemCollection> getItemCollections() {
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

    public boolean successful() {
        return success != null && success == 1;
    }
}
