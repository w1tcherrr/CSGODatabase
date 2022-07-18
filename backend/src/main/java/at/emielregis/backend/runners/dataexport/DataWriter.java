package at.emielregis.backend.runners.dataexport;

import at.emielregis.backend.runners.dataexport.writers.CaseWriter;
import at.emielregis.backend.runners.dataexport.writers.CombinedDataWriter;
import at.emielregis.backend.runners.dataexport.writers.MajorWriter;
import at.emielregis.backend.runners.dataexport.writers.MiscellaneousDataWriter;
import at.emielregis.backend.runners.dataexport.writers.PatchWriter;
import at.emielregis.backend.runners.dataexport.writers.SouvenirWriter;
import at.emielregis.backend.runners.dataexport.writers.StickerWriter;
import at.emielregis.backend.runners.dataexport.writers.StorageUnitWriter;
import org.springframework.stereotype.Component;

@Component
public record DataWriter(CombinedDataWriter combinedDataWriter,
                         CaseWriter caseWriter,
                         MajorWriter majorWriter,
                         MiscellaneousDataWriter miscellaneousWriter,
                         PatchWriter patchWriter,
                         SouvenirWriter souvenirWriter,
                         StickerWriter stickerWriter,
                         StorageUnitWriter storageUnitWriter) {
    public void write() {
        combinedDataWriter.writeWorkbook("Combined_Data.xlsx");
        caseWriter.writeWorkbook("Cases.xlsx");
        majorWriter.writeWorkbook("Majors.xlsx");
        miscellaneousWriter.writeWorkbook("Miscellaneous_Data.xlsx");
        patchWriter.writeWorkbook("Patch_Collections.xlsx");
        souvenirWriter.writeWorkbook("Souvenir_Collections.xlsx");
        stickerWriter.writeWorkbook("Sticker_Collections.xlsx");
        storageUnitWriter.writeWorkbook("Storage_Units.xlsx");
    }
}