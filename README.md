# SDM4 importer til LPR

## Igang
LPR-importeren bruger sin egen SQL-baserede persisteringslogik.

Læs stamdata dokumentation der ligger i SDM-Core projektet inden dette projekt bygges.

Se https://github.com/trifork/sdm4-core/tree/sdm-core-4.3/doc.

For at køre integrationstests, kræves en opsætning som beskrevet i guide til udviklere

Klon repo med ```git clone https://github.com/trifork/sdm4-lprimporter.git```.

## Konfiguration
Der er følgende importer-specifikke konfigurations-properties

*  ``spooler.lprimporter.batchsize``
  angiver hvor mange ydelser, der indsættes pr database-commit
  Default-værdi: 1000
