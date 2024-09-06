package at.emielregis.backend.runners.dataexport;

import at.emielregis.backend.runners.dataexport.writers.*;
import org.springframework.stereotype.Component;

@Component
public record DataWriter(CombinedDataWriter combinedDataWriter,
                         CaseWriter caseWriter,
                         MajorWriter majorWriter,
                         MiscellaneousDataWriter miscellaneousWriter,
                         PatchWriter patchWriter,
                         SouvenirWriter souvenirWriter,
                         StickerWriter stickerWriter,
                         PriceWriter priceWriter,
                         GraffitiWriter graffitiWriter,
                         NameTagWriter nameTagWriter) {
    public void write() {
        miscellaneousWriter.writeWorkbook("Miscellaneous_Data.xlsx");
        priceWriter.writeWorkbook("Price_Analysis.xlsx");
        combinedDataWriter.writeWorkbook("Combined_Data.xlsx");
        caseWriter.writeWorkbook("Cases.xlsx");
        majorWriter.writeWorkbook("Majors.xlsx");
        patchWriter.writeWorkbook("Patch_Collections.xlsx");
        souvenirWriter.writeWorkbook("Souvenir_Collections.xlsx");
        stickerWriter.writeWorkbook("Sticker_Collections.xlsx");
        graffitiWriter.writeWorkbook("Graffiti_Collections.xlsx");
        nameTagWriter.writeWorkbook("Name_Tags.xlsx");
    }
}