# Please refer to the user manual for correct commandline arguments settings.

# Jonto.
#java --add-opens java.base/java.lang=ALL-UNNAMED -jar Jonto.jar 0 /Users/jizhou/Desktop/out/
java --add-opens java.base/java.lang=ALL-UNNAMED -jar Jonto.jar oaei_NCI_small_overlapping_fma.owl oaei_FMA_small_overlapping_nci.owl oaei_FMA2NCI_UMLS_mappings_with_flagged_repairs.rdf /Users/jizhou/Desktop/out/

# LogMap.
java --add-opens java.base/java.lang=ALL-UNNAMED -jar logmap-matcher-4.0.jar EVALUATION file:/Users/jizhou/Downloads/LargeBio_dataset_oaei/oaei_NCI_small_overlapping_fma.owl file:/Users/jizhou/Downloads/LargeBio_dataset_oaei/oaei_FMA_small_overlapping_nci.owl /Users/jizhou/Downloads/LargeBio_dataset_oaei/oaei_FMA2NCI_UMLS_mappings_with_flagged_repairs.rdf /Users/jizhou/Desktop/out/ true
