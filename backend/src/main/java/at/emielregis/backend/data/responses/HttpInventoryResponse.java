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

/**
 * Represents the response from an HTTP request to retrieve an inventory.
 * Parses the JSON response and extracts item information.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HttpInventoryResponse {

    /**
     * Identifier for unique item types based on classId and instanceId.
     */
    private record UniqueItemTypeIdentifier(String classId, String instanceId) {
    }

    /**
     * List of sticker names that include commas, used for parsing stickers correctly.
     */
    private static final List<String[]> stickerNamesWithCommas = List.of(
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

    /**
     * Map to store item collections, keyed by their unique identifiers.
     */
    private final HashMap<UniqueItemTypeIdentifier, ItemCollection> items = new HashMap<>();

    /**
     * Indicates if the request was successful.
     */
    @JsonProperty("success")
    private Integer success;

    /**
     * Indicates if there are more items to load in the inventory.
     */
    @JsonProperty("more_items")
    private Integer hasMoreItems;

    /**
     * The last asset ID returned in the response, used for pagination.
     */
    @Getter
    @JsonProperty("last_assetid")
    private String lastAssetId;

    /**
     * Processes the 'assets' section of the JSON response to count the number of each unique item.
     * Groups items by their classId and instanceId.
     *
     * @param assets The list of asset maps from the JSON response.
     */
    @JsonProperty("assets")
    private void unpackAssets(List<Map<String, Object>> assets) {
        assets.forEach(map -> {
            AtomicReference<String> currentClassId = new AtomicReference<>();
            AtomicReference<String> currentInstanceId = new AtomicReference<>();

            // Extract classid and instanceid from the asset map
            map.forEach((key, value) -> {
                if (key.equals("classid")) {
                    currentClassId.set((String) value);
                } else if (key.equals("instanceid")) {
                    currentInstanceId.set((String) value);
                }
            });

            // Ensure that both classid and instanceid are present
            if (currentClassId.get() == null || currentInstanceId.get() == null) {
                throw new IllegalStateException("ClassId or CurrentInstanceId is null in response: " + assets);
            }

            // Create a unique identifier and update the item count
            UniqueItemTypeIdentifier identifier = new UniqueItemTypeIdentifier(currentClassId.get(), currentInstanceId.get());
            if (items.containsKey(identifier)) {
                items.put(identifier, ItemCollection.builder().amount(items.get(identifier).getAmount() + 1).build());
            } else {
                items.put(identifier, ItemCollection.builder().amount(1).build());
            }
        });
    }

    /**
     * Processes the 'descriptions' section of the JSON response to extract detailed item information.
     * Populates the item collections with item types and additional properties.
     *
     * @param descriptions The list of description maps from the JSON response.
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
            final AtomicReference<Charm> atomicCharm = new AtomicReference<>();

            map.forEach((key, value) -> {
                switch (key) {
                    case "name" -> atomicName.set((String) value);
                    case "classid" -> atomicClassId.set((String) value);
                    case "instanceid" -> atomicInstanceId.set((String) value);
                    case "type" -> atomicCategory.set((String) value);
                    case "market_hash_name" -> atomicMarketHashName.set((String) value);
                    case "descriptions" -> {
                        List<Map<String, Object>> maps = (List<Map<String, Object>>) value;
                        maps.forEach(map1 -> {
                            String map1Name = (String) map1.get("name");
                            String map1Value = (String) map1.get("value");

                            if (map1Name == null || map1Value == null) {
                                return;
                            }

                            if (map1Name.equals("sticker_info")) {
                                // Parse sticker information
                                String valueString = map1Value;
                                if (valueString.contains("<br>Sticker:")) {
                                    int amountOfStickers = valueString.split("<img").length - 1;

                                    valueString = valueString.substring(valueString.indexOf("<br>Sticker:") + "<br>Sticker:".length()).trim();
                                    valueString = valueString.substring(0, valueString.indexOf("</center>")).trim();
                                    String[] split = valueString.split(",");
                                    List<Sticker> stickers = new ArrayList<>();
                                    for (int i = 0; i < split.length; i++) {
                                        String stickerName = split[i].trim().replaceAll(" {2,}", " ");

                                        // Handle stickers that include commas in their names
                                        for (String[] stickerNameWithCommas : stickerNamesWithCommas) {
                                            if (stickerName.equals(stickerNameWithCommas[0])) {

                                                int remainingCurrentStickerLength = stickerNameWithCommas.length - 1;
                                                int remainingPossibleLength = split.length - i - 1;

                                                if (remainingPossibleLength < remainingCurrentStickerLength) {
                                                    continue;
                                                }

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

                                                stickerName = String.join(",", stickerNameWithCommas);
                                                i += remainingCurrentStickerLength;
                                            }
                                        }

                                        stickers.add(Sticker.builder().name(stickerName).stickerType(StickerType.ofName(stickerName)).build());
                                    }

                                    if (stickers.size() != amountOfStickers) {
                                        throw new IllegalStateException("Amount of stickers does not match for string: " + map1Value);
                                    }

                                    atomicStickers.set(stickers);
                                }
                            } else if (map1Name.equals("keychain_info")) {
                                // Parse charm information
                                String valueString = map1Value;
                                if (valueString.contains("<br>Charm:")) {
                                    valueString = valueString.substring(valueString.indexOf("<br>Charm:") + "<br>Charm:".length()).trim();
                                    valueString = valueString.substring(0, valueString.indexOf("</center>")).trim();
                                    Charm charm = Charm.builder().name(valueString).build();
                                    atomicCharm.set(charm);
                                }
                            } else if (map1Value.startsWith("Name Tag: ''")) {
                                // Extract name tag
                                atomicNameTag.set(map1Value.substring(12, map1Value.length() - 2));
                            }
                        });
                    }
                    case "tags" -> {
                        // Parse item tags to extract item set, rarity, exterior, and quality (StatTrak or Souvenir)
                        List<Map<String, Object>> maps = (List<Map<String, Object>>) value;
                        maps.forEach(map1 -> {
                            String category = (String) map1.get("category");
                            switch (category) {
                                case "ItemSet", "StickerCapsule", "PatchCapsule", "SprayCapsule", "KeychainCapsule" ->
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

            // Retrieve the item from the items map using the unique identifier
            UniqueItemTypeIdentifier identifier = new UniqueItemTypeIdentifier(atomicClassId.get(), atomicInstanceId.get());
            ItemCollection item = items.get(identifier);

            // Build the ItemType object with parsed data
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

            // Ensure item is not both StatTrak and Souvenir
            if (atomicSouvenir.get() && atomicStatTrak.get()) {
                throw new IllegalStateException("Item can't be both Souvenir and StatTrak at the same time!");
            }

            itemTypeBuilder.specialItemType(SpecialItemType.fromBooleans(atomicStatTrak.get(), atomicSouvenir.get()));

            // Set the ItemType and other properties in the ItemCollection
            item.setItemType(itemTypeBuilder.build());
            item.setNameTag(atomicNameTag.get());
            item.setStickers(atomicStickers.get());
            item.setCharm(atomicCharm.get());

            // Update the items map
            items.put(identifier, item);
        });
    }

    /**
     * Removes the 'StatTrak™' and 'Souvenir' prefixes from item names.
     *
     * @param string The original item name.
     * @return The pruned item name without prefixes.
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
     * Returns the list of item collections parsed from the response.
     *
     * @return The list of ItemCollection objects.
     */
    public List<ItemCollection> getItemCollections() {
        return items.values().stream().toList();
    }

    /**
     * Checks if there are more items to load from the inventory.
     *
     * @return true if there are more items, false otherwise.
     */
    public boolean hasMoreItems() {
        return hasMoreItems != null && hasMoreItems == 1;
    }

    /**
     * Checks if the request was successful.
     *
     * @return true if successful, false otherwise.
     */
    public boolean successful() {
        return success != null && success == 1;
    }
}
