package at.emielregis.backend.service;

import at.emielregis.backend.data.entities.items.ItemSet;
import at.emielregis.backend.data.enums.Exterior;
import at.emielregis.backend.repository.ItemSetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.stream.Collectors;

@Component
public record ItemSetService(ItemSetRepository itemSetRepository) {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public List<ItemSet> searchBySubstring(String... searches) {
        LOGGER.info("ItemSetService#searchBySubstring(" + Arrays.toString(searches) + ")");
        List<ItemSet> sets = new ArrayList<>();
        for (String search : searches) {
            sets.addAll(itemSetRepository.search(search));
        }
        return sets.stream().distinct().toList();
    }

    public List<ItemSet> searchByEquality(String... searches) {
        LOGGER.info("ItemSetService#searchByEquality(" + Arrays.toString(searches) + ")");
        List<ItemSet> sets = new ArrayList<>();
        for (String search : searches) {
            sets.add(itemSetRepository.getByName(search));
        }
        return sets.stream().distinct().toList();
    }

    public List<Exterior> getExteriorsForItemSet(ItemSet set) {
        LOGGER.info("ItemSetService#getExteriorsForItemSet(" + set.toString() + ")");
        return itemSetRepository.getExteriorsForSet(set);
    }

    public boolean hasStatTrakForItemSet(ItemSet set) {
        LOGGER.info("ItemSetService#hasStatTrakForItemSet(" + set.toString() + ")");
        return itemSetRepository.hasStatTrakForItemSet(set);
    }

    public boolean hasSouvenirForItemSet(ItemSet set) {
        LOGGER.info("ItemSetService#hasSouvenirForItemSet(" + set.toString() + ")");
        return itemSetRepository.hasSouvenirForItemSet(set);
    }

    public long count() {
        LOGGER.info("ItemSetService#count()");
        return itemSetRepository.count();
    }

    public List<ItemSet> getAllCaseCollections() {
        LOGGER.info("ItemSetService#getAllCaseCollections()");
        return searchByEquality(
            "The Recoil Collection",
            "The Dreams & Nightmares Collection",
            "The Operation Riptide Collection",
            "The Snakebite Collection",
            "The Operation Broken Fang Collection",
            "The Fracture Collection",
            "The Prisma 2 Collection",
            "The Prisma Collection",
            "The CS20 Collection",
            "The Shattered Web Collection",
            "The Danger Zone Collection",
            "The Horizon Collection",
            "The Clutch Collection",
            "The Spectrum 2 Collection",
            "The Spectrum Collection",
            "The Operation Hydra Collection",
            "The Glove Collection",
            "The Gamma 2 Collection",
            "The Gamma Collection",
            "The Chroma Collection",
            "The Chroma 2 Collection",
            "The Chroma 3 Collection",
            "The Wildfire Collection",
            "The Revolver Case Collection",
            "The Shadow Collection",
            "The Falchion Collection",
            "The Vanguard Collection",
            "The eSports 2014 Summer Collection",
            "The Breakout Collection",
            "The Huntsman Collection",
            "The Phoenix Collection",
            "The Arms Deal Collection",
            "The Arms Deal 2 Collection",
            "The Arms Deal 3 Collection",
            "The Winter Offensive Collection",
            "The eSports 2013 Winter Collection",
            "The eSports 2013 Collection",
            "The Bravo Collection")
            .stream().filter(Objects::nonNull).sorted(Comparator.comparing(ItemSet::getName)).collect(Collectors.toList());
    }

    public List<ItemSet> getAllSouvenirCollections() {
        LOGGER.info("ItemSetService#getAllSouvenirCollections()");
        return searchBySubstring("Mirage", "Dust II", "Ancient", "Inferno",
            "Overpass", "Nuke", "Vertigo", "Cache", "Cobblestone", "Train", "Souvenir")
            .stream().sorted(Comparator.comparing(ItemSet::getName))
            .filter(col -> !col.getName().contains("2021 Train")) // this is not a souvenir collection and was in the riptide shop
            .collect(Collectors.toList());
    }

    public List<ItemSet> getAllStickerCollections() {
        LOGGER.info("ItemSetService#getAllStickerCollections()");
        return searchByEquality(
            "Antwerp 2022 Contenders Stickers",
            "Antwerp 2022 Legends Stickers",
            "Antwerp 2022 Challengers Stickers",
            "Antwerp 2022 Player Autographs",
            "Stockholm 2021 Challengers Stickers",
            "Stockholm 2021 Contenders Stickers",
            "Stockholm 2021 Legends Stickers",
            "Stockholm 2021 Player Autographs",
            "2020 RMR Challengers",
            "2020 RMR Contenders",
            "2020 RMR Legends",
            "Berlin 2019 Legends",
            "Berlin 2019 Minor Challengers",
            "Berlin 2019 Player Autographs",
            "Berlin 2019 Returning Challengers",
            "Katowice 2019 Legends",
            "Katowice 2019 Minor Challengers",
            "Katowice 2019 Player Autographs",
            "Katowice 2019 Returning Challengers",
            "London 2018 Legends",
            "London 2018 Minor Challengers",
            "London 2018 Player Autographs",
            "London 2018 Returning Challengers",
            "Boston 2018 Player Autographs",
            "Boston 2018 Minor Challengers",
            "Boston 2018 Legends",
            "Boston 2018 Returning Challengers",
            "Krakow 2017 Legends",
            "Krakow 2017 Challengers",
            "Krakow 2017 Player Autographs",
            "Atlanta 2017 Player Autographs",
            "Atlanta 2017 Legends",
            "Atlanta 2017 Challengers",
            "Cologne 2016 Player Autographs",
            "Cologne 2016 Legends",
            "Cologne 2016 Challengers",
            "MLG Columbus 2016 Player Autographs",
            "MLG Columbus 2016 Legends",
            "MLG Columbus 2016 Challengers",
            "DreamHack Cluj-Napoca 2015 Player Autographs",
            "DreamHack Cluj-Napoca 2015 Challengers",
            "DreamHack Cluj-Napoca 2015 Legends",
            "ESL One Cologne 2015 Legends",
            "ESL One Cologne 2015 Player Autographs",
            "ESL One Cologne 2015 Challengers",
            "ESL One Katowice 2015 Challengers",
            "ESL One Katowice 2015 Legends",
            "DreamHack 2014 Challengers",
            "DreamHack 2014 Legends",
            "ESL One Cologne 2014 Legends",
            "ESL One Cologne 2014 Challengers",
            "EMS Katowice 2014 Challengers",
            "EMS Katowice 2014 Legends",
            "The Boardroom Sticker Capsule",
            "Operation Riptide Sticker Collection",
            "Riptide Surf Shop Sticker Collection",
            "2021 Community Sticker Capsule",
            "Poorly Drawn Capsule",
            "Broken Fang Sticker Collection",
            "Recoil Sticker Collection",
            "Warhammer 40,000 Sticker Capsule",
            "Half-Life: Alyx Sticker Capsule",
            "Halo Capsule",
            "Shattered Web Sticker Collection",
            "CS20 Sticker Capsule",
            "Chicken Capsule",
            "Feral Predators Capsule",
            "Skill Groups Capsule",
            "Community Capsule 2018",
            "Perfect World Sticker Capsule 1",
            "Perfect World Sticker Capsule 2",
            "Bestiary Capsule",
            "Sugarface Capsule",
            "Team Roles Capsule",
            "Slid3 Capsule",
            "Pinups Capsule",
            "Enfu Sticker Capsule",
            "Sticker Capsule",
            "Sticker Capsule 2",
            "10 Year Birthday Sticker Capsule"
        ).stream().filter(Objects::nonNull).toList();
    }

    public List<ItemSet> getAllPatchCollections() {
        LOGGER.info("ItemSetService#getAllPatchCollections()");
        return searchByEquality(
            "Stockholm 2021 Challengers Patches",
            "Stockholm 2021 Legends Patches",
            "Stockholm 2021 Contenders Patches",
            "Metal Skill Group Patch Collection",
            "Operation Riptide Patch Collection",
            "CS:GO Patch Pack",
            "Half-Life: Alyx Patch Pack"
        ).stream().filter(Objects::nonNull).toList();
    }
}
