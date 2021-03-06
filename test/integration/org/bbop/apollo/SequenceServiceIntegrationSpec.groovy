package org.bbop.apollo

import grails.converters.JSON
import grails.test.spock.IntegrationSpec
import org.bbop.apollo.gwt.shared.FeatureStringEnum
import org.codehaus.groovy.grails.web.json.JSONObject
import spock.lang.Ignore

class SequenceServiceIntegrationSpec extends IntegrationSpec {
    
    def requestHandlingService
    def sequenceService
    
    def setup() {
        Organism organism = new Organism(
                directory: "/tmp"
                ,commonName: "sampleAnimal"
        ).save(flush: true)

        Sequence sequence = new Sequence(
                length: 1405242
                ,refSeqFile: "adsf"
                ,seqChunkPrefix: "Group1.10-"
                ,seqChunkSize: 20000
                ,start: 0
                ,end: 1405242
                ,organism: organism
                // from (honeybee f78/c6f/0c
                ,sequenceDirectory: "test/integration/resources/sequences/honeybee-Group1.10/"
                ,name: "Group1.10"
        ).save(flush: true)
    }

    def cleanup() {
    }

    void "add a simple gene model to get its sequence and a valid GFF3"() {
        
        given: "a simple gene model with 1 mRNA, 1 exon and 1 CDS"
        String jsonString = "{ \"track\": \"Annotations-Group1.10\", \"features\": [{\"location\":{\"fmin\":1248797,\"fmax\":1249052,\"strand\":-1},\"type\":{\"cv\":{\"name\":\"sequence\"},\"name\":\"mRNA\"},\"name\":\"GB40722-RA\",\"children\":[{\"location\":{\"fmin\":1248797,\"fmax\":1249052,\"strand\":-1},\"type\":{\"cv\":{\"name\":\"sequence\"},\"name\":\"exon\"}},{\"location\":{\"fmin\":1248797,\"fmax\":1249052,\"strand\":-1},\"type\":{\"cv\":{\"name\":\"sequence\"},\"name\":\"CDS\"}}]}], \"operation\": \"add_transcript\" }"
        JSONObject jsonObject = JSON.parse(jsonString) as JSONObject
        
        when: "the gene model is added"
        JSONObject returnObject = requestHandlingService.addTranscript(jsonObject)

        then: "we should see the appropriate model"
        assert Sequence.count == 1
        assert Gene.count == 1
        assert MRNA.count == 1
        assert Exon.count == 1
        assert CDS.count == 1
        assert FeatureLocation.count == 4 + FlankingRegion.count
        assert FeatureRelationship.count == 3

        String getSequenceString = "{\"operation\":\"get_sequence\",\"features\":[{\"uniquename\":\"@UNIQUENAME@\"}],\"track\":\"Annotations-Group1.10\",\"type\":\"@SEQUENCE_TYPE@\"}"
        String uniqueName = MRNA.findByName("GB40722-RA-00001").uniqueName
        JSONObject commandObject = new JSONObject()
        
        when: "A request is sent for the peptide sequence of the mRNA"
        String getPeptideSequenceString = getSequenceString.replaceAll("@UNIQUENAME@", uniqueName)
        getPeptideSequenceString = getPeptideSequenceString.replaceAll("@SEQUENCE_TYPE@", FeatureStringEnum.TYPE_PEPTIDE.value)
        commandObject = JSON.parse(getPeptideSequenceString) as JSONObject
        JSONObject getPeptideSequenceReturnObject = sequenceService.getSequenceForFeatures(commandObject)
        
        then: "we should get back the expected peptide sequence"
        assert getPeptideSequenceReturnObject.residues != null
        String expectedPeptideSequence = "MSSYTFSQKVSTPIPPEKGSFPLDHEGICKRLMIKYMRCLIENNNENTMCRDIIKEYLSCRMDNELMAREEWSNLGFSDEVKET"
        assert getPeptideSequenceReturnObject.residues == expectedPeptideSequence
        
        when: "A request is sent for the CDS sequence of the mRNA"
        String getCDSSequenceString = getSequenceString.replaceAll("@UNIQUENAME@", uniqueName)
        getCDSSequenceString = getCDSSequenceString.replaceAll("@SEQUENCE_TYPE@", FeatureStringEnum.TYPE_CDS.value)
        commandObject = JSON.parse(getCDSSequenceString) as JSONObject
        JSONObject getCDSSequenceReturnObject = sequenceService.getSequenceForFeatures(commandObject)

        then: "we should get back the expected CDS sequence"
        assert getCDSSequenceReturnObject.residues != null
        String expectedCDSSequence = "ATGTCATCGTATACATTTTCGCAAAAAGTGTCTACACCTATACCTCCAGAAAAGGGTAGTTTTCCACTCGACCATGAGGGTATTTGCAAAAGACTTATGATTAAGTATATGCGCTGTTTAATTGAGAATAATAACGAAAACACGATGTGTCGTGATATTATAAAAGAATACCTTTCTTGTCGAATGGATAATGAGCTTATGGCACGAGAAGAATGGTCTAATCTTGGTTTTTCTGACGAGGTCAAGGAGACATAA"
        assert getCDSSequenceReturnObject.residues == expectedCDSSequence

        when: "A request is sent for the CDNA sequence of the mRNA"
        String getCDNASequenceString = getSequenceString.replaceAll("@UNIQUENAME@", uniqueName)
        getCDNASequenceString = getCDNASequenceString.replaceAll("@SEQUENCE_TYPE@", FeatureStringEnum.TYPE_CDNA.value)
        commandObject = JSON.parse(getCDNASequenceString) as JSONObject
        JSONObject getCDNASequenceReturnObject = sequenceService.getSequenceForFeatures(commandObject)

        then: "we should get back the expected CDNA sequence"
        assert getCDNASequenceReturnObject.residues != null
        String expectedCDNASequence = "ATGTCATCGTATACATTTTCGCAAAAAGTGTCTACACCTATACCTCCAGAAAAGGGTAGTTTTCCACTCGACCATGAGGGTATTTGCAAAAGACTTATGATTAAGTATATGCGCTGTTTAATTGAGAATAATAACGAAAACACGATGTGTCGTGATATTATAAAAGAATACCTTTCTTGTCGAATGGATAATGAGCTTATGGCACGAGAAGAATGGTCTAATCTTGGTTTTTCTGACGAGGTCAAGGAGACATAA"
        assert getCDNASequenceReturnObject.residues == expectedCDNASequence
//
        when: "A request is sent for the genomic sequence of the mRNA"
        String getGenomicSequenceString = getSequenceString.replaceAll("@UNIQUENAME@", uniqueName)
        getGenomicSequenceString = getGenomicSequenceString.replaceAll("@SEQUENCE_TYPE@", FeatureStringEnum.TYPE_GENOMIC.value)
        commandObject = JSON.parse(getGenomicSequenceString) as JSONObject
        JSONObject getGenomicSequenceReturnObject = sequenceService.getSequenceForFeatures(commandObject)
//
        then: "we should get back the expected genomic sequence"
        assert getGenomicSequenceReturnObject.residues != null
        String expectedGenomicSequence = "ATGTCATCGTATACATTTTCGCAAAAAGTGTCTACACCTATACCTCCAGAAAAGGGTAGTTTTCCACTCGACCATGAGGGTATTTGCAAAAGACTTATGATTAAGTATATGCGCTGTTTAATTGAGAATAATAACGAAAACACGATGTGTCGTGATATTATAAAAGAATACCTTTCTTGTCGAATGGATAATGAGCTTATGGCACGAGAAGAATGGTCTAATCTTGGTTTTTCTGACGAGGTCAAGGAGACATAA"
        assert getGenomicSequenceReturnObject.residues == expectedGenomicSequence

        when: "A request is sent for the genomic sequence of the mRNA with a flank of 500 bp"
        String getGenomicFlankSequenceString = getSequenceString.replaceAll("@UNIQUENAME@", uniqueName)
        getGenomicFlankSequenceString = getGenomicFlankSequenceString.replaceAll("@SEQUENCE_TYPE@", FeatureStringEnum.TYPE_GENOMIC.value)
        commandObject = JSON.parse(getGenomicFlankSequenceString) as JSONObject
        commandObject.put("flank", 500)
        JSONObject getGenomicFlankSequenceReturnObject = sequenceService.getSequenceForFeatures(commandObject)

        then: "we should get back the expected genomic sequence including the flanking regions"
        assert getGenomicFlankSequenceReturnObject.residues != null
        String expectedGenomicSequenceWithFlank = "TAATAAATTTACTGTGTATTTAATCTGTATTTCTATCTTAAATGTAAGTGCAAAATTTTTTGAAAAAAATTTCATTAATTTTTCTAATGGCAAATGTAAGGACCTCATTTTTTTTATTGACTTATTATTTTTTTAATTAGAATGCAATAGAAATTTTTCTATTTTCATTGTTCAGTAAATAATTATATTTTAAGTTTTCTTTAATTCTATTAAGAATAAAATAAATTCCTGTAAAACTAAAATAAATTATCGAAATTATAATTATTATTTATTTTATATTTAATTTAAATTGGATATCTTTTTTATATTATATAACACGGAGCGTGCGTAAAATGAAGTTAGAAATATCATCGTAAGTGGCGCTAGTATATTTATTATCCCGTAATGGCGGTTATGATTTATCGTGTATTTTGTAATAAGTTTACTTAAAAGAAGTTTATTGAAAATTTTGACTTTTACTTTTCATCGAGTTTTCTGTATGAACGTAATCAGCGGTAGTGATGTCATCGTATACATTTTCGCAAAAAGTGTCTACACCTATACCTCCAGAAAAGGGTAGTTTTCCACTCGACCATGAGGGTATTTGCAAAAGACTTATGATTAAGTATATGCGCTGTTTAATTGAGAATAATAACGAAAACACGATGTGTCGTGATATTATAAAAGAATACCTTTCTTGTCGAATGGATAATGAGCTTATGGCACGAGAAGAATGGTCTAATCTTGGTTTTTCTGACGAGGTCAAGGAGACATAACAGCAGGGATTTATTACTAATCGATTGTCGCAAAGTTGTAGATCACTGTTTTTATCACTTGAATGGTATTTACAAACTTTTTCGCTGGTGATTCTTATTTTATCTTAGATAAGACTTATATGGATTTTAAAGTGAATTTGTAAATTTGTAGCGACATATAACTTATGTACGTATTTTATGAATTATAAATAATCATGCATTTATTCGATATGATTTTTGAATAATGATACAATATAAGTAGAAAATTCTTTTTAACATTCAATCCAATTAAAGTGATCTCAGTGCAGAAAAAGCTTCACAGCAAATATTAAATATTTAAAATAAATTAATAATATAAATATATTCAGTATAGAGATCTAAGGAGAAAAGAAATCATTAATAAAAATACTTTTATTAAATGTAGTTTATTGTTAATTGATGTGATTTAGGTATCTTATATCAGATGGCCTAAAATATTTAATATAGCTATAACTGTAAAAGTTAAAATCATTTCATCTTGA"
        assert getGenomicFlankSequenceReturnObject.residues == expectedGenomicSequenceWithFlank
        
        when: "A request is sent for the GFF3 of a simple gene model"
        String getGff3String = "{\"operation\":\"get_gff3\",\"features\":[{\"uniquename\":\"@UNIQUENAME@\"}],\"track\":\"Annotations-Group1.10\"}"
        getGff3String = getGff3String.replaceAll("@UNIQUENAME@", uniqueName)
        JSONObject inputObject = JSON.parse(getGff3String) as JSONObject
        Sequence refSequence = Sequence.first()
        FeatureLocation.all.each { featureLocation->
            refSequence.addToFeatureLocations(featureLocation)
        }
        File gffFile = File.createTempFile("feature", ".gff3")
        sequenceService.getGff3ForFeature(inputObject, gffFile)

        then: "we should get a proper GFF3 for the feature"
        String gffFileText = gffFile.text
        assert gffFileText.length() > 0
        log.debug gffFileText
    }

    void "add a gene model with UTRs to get its sequence and a valid GFF3"() {
        
        given: "a gene with a 5' UTR and a 3' UTR"
        String jsonString = "{ \"track\": \"Annotations-Group1.10\", \"features\": [{\"location\":{\"fmin\":694293,\"fmax\":696055,\"strand\":-1},\"type\":{\"cv\":{\"name\":\"sequence\"},\"name\":\"mRNA\"},\"name\":\"GB40749-RA\",\"children\":[{\"location\":{\"fmin\":695943,\"fmax\":696055,\"strand\":-1},\"type\":{\"cv\":{\"name\":\"sequence\"},\"name\":\"exon\"}},{\"location\":{\"fmin\":694293,\"fmax\":694440,\"strand\":-1},\"type\":{\"cv\":{\"name\":\"sequence\"},\"name\":\"exon\"}},{\"location\":{\"fmin\":694293,\"fmax\":694606,\"strand\":-1},\"type\":{\"cv\":{\"name\":\"sequence\"},\"name\":\"exon\"}},{\"location\":{\"fmin\":694918,\"fmax\":695591,\"strand\":-1},\"type\":{\"cv\":{\"name\":\"sequence\"},\"name\":\"exon\"}},{\"location\":{\"fmin\":695882,\"fmax\":696055,\"strand\":-1},\"type\":{\"cv\":{\"name\":\"sequence\"},\"name\":\"exon\"}},{\"location\":{\"fmin\":694440,\"fmax\":695943,\"strand\":-1},\"type\":{\"cv\":{\"name\":\"sequence\"},\"name\":\"CDS\"}}]}], \"operation\": \"add_transcript\" }"
        JSONObject jsonObject = JSON.parse(jsonString) as JSONObject
        
        when: "the gene model is added"
        JSONObject returnObject = requestHandlingService.addTranscript(jsonObject)
        
        then: "we should see the appropriate model"
        assert Sequence.count == 1
        assert Gene.count == 1
        assert MRNA.count == 1
        assert Exon.count == 3
        assert CDS.count == 1
        assert FeatureLocation.count == 6 + FlankingRegion.count
        assert FeatureRelationship.count == 5

        String getSequenceString = "{\"operation\":\"get_sequence\",\"features\":[{\"uniquename\":\"@UNIQUENAME@\"}],\"track\":\"Annotations-Group1.10\",\"type\":\"@SEQUENCE_TYPE@\"}"
        String uniqueName = MRNA.findByName("GB40749-RA-00001").uniqueName
        JSONObject commandObject = new JSONObject()
        
        when: "A request is sent for the peptide sequence of the mRNA"
        String getPeptideSequenceString = getSequenceString.replaceAll("@UNIQUENAME@", uniqueName)
        getPeptideSequenceString = getPeptideSequenceString.replaceAll("@SEQUENCE_TYPE@", FeatureStringEnum.TYPE_PEPTIDE.value)
        commandObject = JSON.parse(getPeptideSequenceString) as JSONObject
        JSONObject getPeptideSequenceReturnObject = sequenceService.getSequenceForFeatures(commandObject)

        then: "we should get back the expected peptide sequence"
        assert getPeptideSequenceReturnObject.residues != null
        String expectedPeptideSequence = "MPRCLIKSMTRYRKTDNSSEVEAELPWTPPSSVDAKRKHQIKDNSTKCNNIWTSSRLPIVTRYTFNKENNIFWNKELNIADVELGSRNFSEIENTIPSTTPNVSVNTNQAMVDTSNEQKVEKVQIPLPSNAKKVEYPVNVSNNEIKVAVNLNRMFDGAENQTTSQTLYIATNKKQIDSQNQYLGGNMKTTGVENPQNWKRNKTMHYCPYCRKSFDRPWVLKGHLRLHTGERPFECPVCHKSFADRSNLRAHQRTRNHHQWQWRCGECFKAFSQRRYLERHCPEACRKYRISQRREQNCS"
        assert getPeptideSequenceReturnObject.residues == expectedPeptideSequence

        when: "A request is sent for the CDS sequence of the mRNA"
        String getCDSSequenceString = getSequenceString.replaceAll("@UNIQUENAME@", uniqueName)
        getCDSSequenceString = getCDSSequenceString.replaceAll("@SEQUENCE_TYPE@", FeatureStringEnum.TYPE_CDS.value)
        commandObject = JSON.parse(getCDSSequenceString) as JSONObject
        JSONObject getCDSSequenceReturnObject = sequenceService.getSequenceForFeatures(commandObject)

        then: "we should get back the expected CDS sequence"
        assert getCDSSequenceReturnObject.residues != null
        String expectedCDSSequence = "ATGCCTCGTTGCTTGATCAAATCGATGACAAGGTATAGGAAGACCGATAATTCTTCCGAAGTAGAGGCAGAATTACCATGGACTCCGCCATCGTCGGTCGACGCGAAGAGAAAACATCAGATTAAAGACAATTCCACGAAATGCAATAATATATGGACCTCCTCGAGATTGCCAATTGTAACACGTTACACGTTCAATAAAGAGAACAACATATTTTGGAACAAGGAGTTGAATATAGCAGACGTGGAATTGGGCTCGAGAAATTTTTCCGAGATTGAGAATACGATACCGTCGACCACTCCGAATGTCTCTGTGAATACCAATCAGGCAATGGTGGACACGAGCAATGAGCAAAAGGTGGAAAAAGTGCAAATACCATTGCCCTCGAACGCGAAAAAAGTAGAGTATCCGGTAAACGTGAGTAACAACGAGATCAAGGTGGCTGTGAATTTGAATAGGATGTTCGATGGGGCTGAGAATCAGACCACCTCGCAGACTTTGTATATTGCCACGAATAAGAAACAGATTGATTCCCAGAATCAATATTTAGGAGGGAATATGAAAACTACGGGGGTGGAGAATCCCCAGAATTGGAAGAGAAATAAAACTATGCATTATTGCCCTTATTGTCGCAAGAGTTTCGATCGTCCATGGGTTTTGAAGGGTCATCTGCGTCTTCACACGGGTGAACGACCTTTTGAATGTCCGGTCTGCCATAAATCCTTTGCCGATCGATCAAATTTACGTGCGCATCAAAGGACTCGGAATCACCATCAATGGCAATGGCGATGCGGGGAATGTTTCAAAGCATTCTCGCAAAGACGATATTTAGAACGACATTGCCCCGAAGCTTGTAGAAAATATCGAATATCTCAAAGGAGGGAACAGAATTGTAGTTAG"
        assert getCDSSequenceReturnObject.residues == expectedCDSSequence

        when: "A request is sent for the CDNA sequence of the mRNA"
        String getCDNASequenceString = getSequenceString.replaceAll("@UNIQUENAME@", uniqueName)
        getCDNASequenceString = getCDNASequenceString.replaceAll("@SEQUENCE_TYPE@", FeatureStringEnum.TYPE_CDNA.value)
        commandObject = JSON.parse(getCDNASequenceString) as JSONObject
        JSONObject getCDNASequenceReturnObject = sequenceService.getSequenceForFeatures(commandObject)

        then: "we should get back the expected CDNA sequence"
        assert getCDNASequenceReturnObject.residues != null
        String expectedCDNASequence = "CAAATGCCTGTCGAACGTGTGACAGTGGTTTGCTCCATCGCTGTTGCAACGGCCAACACTTTATCGTATTTCGTTCTTTTTTTAGCTGTGGCCGTTTCATCGTCGCGAAAATATGCCTCGTTGCTTGATCAAATCGATGACAAGGTATAGGAAGACCGATAATTCTTCCGAAGTAGAGGCAGAATTACCATGGACTCCGCCATCGTCGGTCGACGCGAAGAGAAAACATCAGATTAAAGACAATTCCACGAAATGCAATAATATATGGACCTCCTCGAGATTGCCAATTGTAACACGTTACACGTTCAATAAAGAGAACAACATATTTTGGAACAAGGAGTTGAATATAGCAGACGTGGAATTGGGCTCGAGAAATTTTTCCGAGATTGAGAATACGATACCGTCGACCACTCCGAATGTCTCTGTGAATACCAATCAGGCAATGGTGGACACGAGCAATGAGCAAAAGGTGGAAAAAGTGCAAATACCATTGCCCTCGAACGCGAAAAAAGTAGAGTATCCGGTAAACGTGAGTAACAACGAGATCAAGGTGGCTGTGAATTTGAATAGGATGTTCGATGGGGCTGAGAATCAGACCACCTCGCAGACTTTGTATATTGCCACGAATAAGAAACAGATTGATTCCCAGAATCAATATTTAGGAGGGAATATGAAAACTACGGGGGTGGAGAATCCCCAGAATTGGAAGAGAAATAAAACTATGCATTATTGCCCTTATTGTCGCAAGAGTTTCGATCGTCCATGGGTTTTGAAGGGTCATCTGCGTCTTCACACGGGTGAACGACCTTTTGAATGTCCGGTCTGCCATAAATCCTTTGCCGATCGATCAAATTTACGTGCGCATCAAAGGACTCGGAATCACCATCAATGGCAATGGCGATGCGGGGAATGTTTCAAAGCATTCTCGCAAAGACGATATTTAGAACGACATTGCCCCGAAGCTTGTAGAAAATATCGAATATCTCAAAGGAGGGAACAGAATTGTAGTTAGAAGGCAAATTTTATTTCTTTAGTATAAACATATTTTTATATTGAAATATCTAATGTAATATATTAAATGTATTTCGTTAATTAACACTGTAAAATTTGAATTCGAAATATCACTGTATTGTTATTCTAATATACATATATATATGTG"
        assert getCDNASequenceReturnObject.residues == expectedCDNASequence
//
        when: "A request is sent for the genomic sequence of the mRNA"
        String getGenomicSequenceString = getSequenceString.replaceAll("@UNIQUENAME@", uniqueName)
        getGenomicSequenceString = getGenomicSequenceString.replaceAll("@SEQUENCE_TYPE@", FeatureStringEnum.TYPE_GENOMIC.value)
        commandObject = JSON.parse(getGenomicSequenceString) as JSONObject
        JSONObject getGenomicSequenceReturnObject = sequenceService.getSequenceForFeatures(commandObject)
//
        then: "we should get back the expected genomic sequence"
        assert getGenomicSequenceReturnObject.residues != null
        String expectedGenomicSequence = "CAAATGCCTGTCGAACGTGTGACAGTGGTTTGCTCCATCGCTGTTGCAACGGCCAACACTTTATCGTATTTCGTTCTTTTTTTAGCTGTGGCCGTTTCATCGTCGCGAAAATATGCCTCGTTGCTTGATCAAATCGATGACAAGGTATAGGAAGACCGATAATTCTTCCGAAGGTAAGACAGAATTCTCTTGTGCACACAGTATAGCTACAGTATACTCAGGGGACGACGAGTGAAATTTTGGCGTGGTTATGGAAAAAAAAAAAGTACAACTCGTAAAGTTGTTGGAGTAAATGAGTCCCGTTTTTTCATGGCGAATCGTACGTCTCCTTTCCACTCGACGACACAGTTTTCAATTTCATATAATAAAAGCGAATGTGAAAATACGATGCGTATGATTCGTTCGAAAAAGAAAGGCAAAAAAAAAAAAAAAAAAAAAATGAAATGATTTTCTCTCCTAATTAGTAGAGGCAGAATTACCATGGACTCCGCCATCGTCGGTCGACGCGAAGAGAAAACATCAGATTAAAGACAATTCCACGAAATGCAATAATATATGGACCTCCTCGAGATTGCCAATTGTAACACGTTACACGTTCAATAAAGAGAACAACATATTTTGGAACAAGGAGTTGAATATAGCAGACGTGGAATTGGGCTCGAGAAATTTTTCCGAGATTGAGAATACGATACCGTCGACCACTCCGAATGTCTCTGTGAATACCAATCAGGCAATGGTGGACACGAGCAATGAGCAAAAGGTGGAAAAAGTGCAAATACCATTGCCCTCGAACGCGAAAAAAGTAGAGTATCCGGTAAACGTGAGTAACAACGAGATCAAGGTGGCTGTGAATTTGAATAGGATGTTCGATGGGGCTGAGAATCAGACCACCTCGCAGACTTTGTATATTGCCACGAATAAGAAACAGATTGATTCCCAGAATCAATATTTAGGAGGGAATATGAAAACTACGGGGGTGGAGAATCCCCAGAATTGGAAGAGAAATAAAACTATGCATTATTGCCCTTATTGTCGCAAGAGTTTCGATCGTCCATGGGTTTTGAAGGGTCATCTGCGTCTTCACACGGGTGAACGACCTTTTGAATGTCCGGTCTGCCATAAATCCTTTGCCGATCGGTATATTTCTTCTTTTAGTTTTATATATTTTTTTATATATTATATATATACACGAGTTACGAATAATAACAAAATTTTTTTCGAACCTTGAACGTATGATCAAAATTTCTCATTAAAACATTTGGAACGAAAAATGATAATTAAATATCGTAATCGGATGATTGCAACATATTATATAGTAATACATTATACATACCTATTTTATTTATTTTCTTTAGCAAATTATAAAGTTAATTTTATTGAGTTAATTTTACGATGTTTGAATTAATTAGTGGATACGAGATTATATGGTGTAACGTACATATTTTGTAGATCAAATTTACGTGCGCATCAAAGGACTCGGAATCACCATCAATGGCAATGGCGATGCGGGGAATGTTTCAAAGCATTCTCGCAAAGACGATATTTAGAACGACATTGCCCCGAAGCTTGTAGAAAATATCGAATATCTCAAAGGAGGGAACAGAATTGTAGTTAGAAGGCAAATTTTATTTCTTTAGTATAAACATATTTTTATATTGAAATATCTAATGTAATATATTAAATGTATTTCGTTAATTAACACTGTAAAATTTGAATTCGAAATATCACTGTATTGTTATTCTAATATACATATATATATGTG"
        assert getGenomicSequenceReturnObject.residues == expectedGenomicSequence

        when: "A request is sent for the genomic sequence of the mRNA with a flank of 500 bp"
        String getGenomicFlankSequenceString = getSequenceString.replaceAll("@UNIQUENAME@", uniqueName)
        getGenomicFlankSequenceString = getGenomicFlankSequenceString.replaceAll("@SEQUENCE_TYPE@", FeatureStringEnum.TYPE_GENOMIC.value)
        commandObject = JSON.parse(getGenomicFlankSequenceString) as JSONObject
        commandObject.put("flank", 500)
        JSONObject getGenomicFlankSequenceReturnObject = sequenceService.getSequenceForFeatures(commandObject)

        then: "we should get back the expected genomic sequence including the flanking regions"
        assert getGenomicFlankSequenceReturnObject.residues != null
        String expectedGenomicSequenceWithFlank = "GAAACGTACACAAACCGTTTCGACTGTTTGTACGTTACTCTTGAGTTCAAATTTACTCGACCTTTTCGAAACTAGCTGAACTTGAATATACTACTTTTGTACCTCTTAATAATAATTGATAATTATTATAAAATTAACAATAATTAATTCTATATAAAAAAAAAAAAAAAAAATAATAATATATTCCTCTACTATATTAAATTTATTTTTCAAATTACGTGGTTTCTCATATAAAAAAAAATTATTAATTTTATTAATCATAATTTTATATATATATATATATATATATATATATATATATATATAAAAAATAAAAATATATTCTTGAAATGAATAATTTTAAAAAACGATTCGATAATTCGAAAAATTATCATTTTTCATTTCCTATTATGAATGGGAAAAGAAATCGGCATTAATGGTACAGATAGAAGGTACGCATTTTTTCGCGTTAGCTATGGGTGAATCTTTCGACATTACAGTACTGATGGCACAGAAGGTGACAAATGCCTGTCGAACGTGTGACAGTGGTTTGCTCCATCGCTGTTGCAACGGCCAACACTTTATCGTATTTCGTTCTTTTTTTAGCTGTGGCCGTTTCATCGTCGCGAAAATATGCCTCGTTGCTTGATCAAATCGATGACAAGGTATAGGAAGACCGATAATTCTTCCGAAGGTAAGACAGAATTCTCTTGTGCACACAGTATAGCTACAGTATACTCAGGGGACGACGAGTGAAATTTTGGCGTGGTTATGGAAAAAAAAAAAGTACAACTCGTAAAGTTGTTGGAGTAAATGAGTCCCGTTTTTTCATGGCGAATCGTACGTCTCCTTTCCACTCGACGACACAGTTTTCAATTTCATATAATAAAAGCGAATGTGAAAATACGATGCGTATGATTCGTTCGAAAAAGAAAGGCAAAAAAAAAAAAAAAAAAAAAATGAAATGATTTTCTCTCCTAATTAGTAGAGGCAGAATTACCATGGACTCCGCCATCGTCGGTCGACGCGAAGAGAAAACATCAGATTAAAGACAATTCCACGAAATGCAATAATATATGGACCTCCTCGAGATTGCCAATTGTAACACGTTACACGTTCAATAAAGAGAACAACATATTTTGGAACAAGGAGTTGAATATAGCAGACGTGGAATTGGGCTCGAGAAATTTTTCCGAGATTGAGAATACGATACCGTCGACCACTCCGAATGTCTCTGTGAATACCAATCAGGCAATGGTGGACACGAGCAATGAGCAAAAGGTGGAAAAAGTGCAAATACCATTGCCCTCGAACGCGAAAAAAGTAGAGTATCCGGTAAACGTGAGTAACAACGAGATCAAGGTGGCTGTGAATTTGAATAGGATGTTCGATGGGGCTGAGAATCAGACCACCTCGCAGACTTTGTATATTGCCACGAATAAGAAACAGATTGATTCCCAGAATCAATATTTAGGAGGGAATATGAAAACTACGGGGGTGGAGAATCCCCAGAATTGGAAGAGAAATAAAACTATGCATTATTGCCCTTATTGTCGCAAGAGTTTCGATCGTCCATGGGTTTTGAAGGGTCATCTGCGTCTTCACACGGGTGAACGACCTTTTGAATGTCCGGTCTGCCATAAATCCTTTGCCGATCGGTATATTTCTTCTTTTAGTTTTATATATTTTTTTATATATTATATATATACACGAGTTACGAATAATAACAAAATTTTTTTCGAACCTTGAACGTATGATCAAAATTTCTCATTAAAACATTTGGAACGAAAAATGATAATTAAATATCGTAATCGGATGATTGCAACATATTATATAGTAATACATTATACATACCTATTTTATTTATTTTCTTTAGCAAATTATAAAGTTAATTTTATTGAGTTAATTTTACGATGTTTGAATTAATTAGTGGATACGAGATTATATGGTGTAACGTACATATTTTGTAGATCAAATTTACGTGCGCATCAAAGGACTCGGAATCACCATCAATGGCAATGGCGATGCGGGGAATGTTTCAAAGCATTCTCGCAAAGACGATATTTAGAACGACATTGCCCCGAAGCTTGTAGAAAATATCGAATATCTCAAAGGAGGGAACAGAATTGTAGTTAGAAGGCAAATTTTATTTCTTTAGTATAAACATATTTTTATATTGAAATATCTAATGTAATATATTAAATGTATTTCGTTAATTAACACTGTAAAATTTGAATTCGAAATATCACTGTATTGTTATTCTAATATACATATATATATGTGCACATGTAGTACTATAAAATCGTATAAAATTATATTATAAAATCACTTTCTCATACCTTTTTTTCATACTGTTGATTTTATATTAAATTTGTTGTATTCTAAAATTGCTAAAATTATTGATTTGATATAAATTCATTGTTCTTTCCCATAAATAATAATGGTTCGATTATAATATCGAAAGAAATATTTGAATCATGAAAAGGATAAAACACTGATATAAATTCGAATAGTTTTATGTAATATTTTTGTTCACTAACAATATTTTATATAAAATTAATAAATATTAATTATTCTAGAATTGTTACTCTTTTATGTAATAGAATTATTGAAACAGTTAACATAATAGTAAATACGTTAATATACATTCGAATATATTCGGAATATTCTGAAAATACGCCTTAAATTGCACAATTAAGTGTGGTTATCGATAGAGTGCGCGCAATAATGTAACGGTTTCTGATCTGAGTGTTTTTAATCTTTCATGGCGCATGTCGTGTACG"
        assert getGenomicFlankSequenceReturnObject.residues == expectedGenomicSequenceWithFlank
        
        when: "A request is sent for the GFF3 of a gene with UTRs"
        String getGff3String = "{\"operation\":\"get_gff3\",\"features\":[{\"uniquename\":\"@UNIQUENAME@\"}],\"track\":\"Annotations-Group1.10\"}"
        getGff3String = getGff3String.replaceAll("@UNIQUENAME@", uniqueName)
        JSONObject inputObject = JSON.parse(getGff3String) as JSONObject
        Sequence refSequence = Sequence.first()
        FeatureLocation.all.each { featureLocation->
            refSequence.addToFeatureLocations(featureLocation)
        }
        
        File gffFile = File.createTempFile("feature", ".gff3")
        sequenceService.getGff3ForFeature(inputObject, gffFile)

        then: "we should get a proper GFF3 for the feature"
        String gffFileText = gffFile.text
        assert gffFileText.length() > 0
        log.debug gffFileText
    }
    

    void "Add 2 genes and get their GFF3"() {
        
        given: "a simple gene model with 1 mRNA, 1 exon and 1 CDS"
        String jsonString1 = "{ \"track\": \"Annotations-Group1.10\", \"features\": [{\"location\":{\"fmin\":1248797,\"fmax\":1249052,\"strand\":-1},\"type\":{\"cv\":{\"name\":\"sequence\"},\"name\":\"mRNA\"},\"name\":\"GB40722-RA\",\"children\":[{\"location\":{\"fmin\":1248797,\"fmax\":1249052,\"strand\":-1},\"type\":{\"cv\":{\"name\":\"sequence\"},\"name\":\"exon\"}},{\"location\":{\"fmin\":1248797,\"fmax\":1249052,\"strand\":-1},\"type\":{\"cv\":{\"name\":\"sequence\"},\"name\":\"CDS\"}}]}], \"operation\": \"add_transcript\" }"
        JSONObject jsonObject1 = JSON.parse(jsonString1) as JSONObject

        String jsonString2 = "{ \"track\": \"Annotations-Group1.10\", \"features\": [{\"location\":{\"fmin\":729928,\"fmax\":730304,\"strand\":1},\"type\":{\"cv\":{\"name\":\"sequence\"},\"name\":\"mRNA\"},\"name\":\"GB40827-RA\",\"children\":[{\"location\":{\"fmin\":729928,\"fmax\":730010,\"strand\":1},\"type\":{\"cv\":{\"name\":\"sequence\"},\"name\":\"exon\"}},{\"location\":{\"fmin\":730296,\"fmax\":730304,\"strand\":1},\"type\":{\"cv\":{\"name\":\"sequence\"},\"name\":\"exon\"}},{\"location\":{\"fmin\":729928,\"fmax\":730304,\"strand\":1},\"type\":{\"cv\":{\"name\":\"sequence\"},\"name\":\"CDS\"}}]}], \"operation\": \"add_transcript\" }"
        JSONObject jsonObject2 = JSON.parse(jsonString2) as JSONObject

        when: "the gene model is added"
        JSONObject returnObject1 = requestHandlingService.addTranscript(jsonObject1)
        JSONObject returnObject2 = requestHandlingService.addTranscript(jsonObject2)
        
        then: "we should see the appropriate model"
        assert Gene.count == 2
        assert MRNA.count == 2

        when: "A request is sent for the GFF3 of a list of genes"
        String uniqueName1 = MRNA.findByName("GB40722-RA-00001").uniqueName
        String uniqueName2 = MRNA.findByName("GB40827-RA-00001").uniqueName
        JSONObject commandObject = new JSONObject()
        String getGff3String = "{\"operation\":\"get_gff3\",\"features\":[{\"uniquename\":\"@UNIQUENAME1@\"}, {\"uniquename\":\"@UNIQUENAME2@\"}],\"track\":\"Annotations-Group1.10\"}"
        getGff3String = getGff3String.replaceAll("@UNIQUENAME1@", uniqueName1)
        getGff3String = getGff3String.replaceAll("@UNIQUENAME2@", uniqueName2)
        JSONObject inputObject = JSON.parse(getGff3String) as JSONObject
        Sequence refSequence = Sequence.first()
        FeatureLocation.all.each { featureLocation->
            refSequence.addToFeatureLocations(featureLocation)
        }
        File gffFile = File.createTempFile("feature", ".gff3");
        sequenceService.getGff3ForFeature(inputObject, gffFile)
        
        then: "we should get a proper GFF3 for each feature"
        String gffFileText = gffFile.text
        assert gffFileText.length() > 0
        log.debug gffFileText
    }
}
