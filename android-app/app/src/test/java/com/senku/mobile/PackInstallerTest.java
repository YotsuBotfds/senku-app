package com.senku.mobile;

import org.junit.Test;

import java.io.File;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public final class PackInstallerTest {
    @Test
    public void sha256HexForTestMatchesKnownDigest() throws Exception {
        File tempFile = File.createTempFile("pack-installer", ".txt");
        try {
            byte[] content = "senku".getBytes(StandardCharsets.UTF_8);
            Files.write(tempFile.toPath(), content);

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] expectedBytes = digest.digest(content);
            StringBuilder expected = new StringBuilder(expectedBytes.length * 2);
            for (byte b : expectedBytes) {
                expected.append(String.format("%02x", b));
            }

            assertEquals(expected.toString(), PackInstaller.sha256HexForTest(tempFile));
        } finally {
            tempFile.delete();
        }
    }

    @Test
    public void missingRequiredPackTablesAcceptsFts4AsLexicalFallback() {
        assertEquals(
            "",
            PackInstaller.missingRequiredPackTablesForTest(Set.of("guides", "guide_related", "lexical_chunks_fts4"))
        );
    }

    @Test
    public void missingRequiredPackTablesDoesNotRequireOptionalAnswerCardTables() {
        assertEquals(
            "",
            PackInstaller.missingRequiredPackTablesForTest(Set.of("guides", "guide_related", "lexical_chunks_fts"))
        );
    }

    @Test
    public void missingRequiredPackTablesIgnoresFutureOptionalTables() {
        assertEquals(
            "",
            PackInstaller.missingRequiredPackTablesForTest(Set.of(
                "guides",
                "guide_related",
                "lexical_chunks_fts",
                "answer_cards",
                "answer_card_clauses",
                "answer_card_sources",
                "answer_card_tags",
                "retrieval_metadata_v2"
            ))
        );
    }

    @Test
    public void missingRequiredPackTablesRequiresAtLeastOneLexicalTable() {
        assertEquals(
            "lexical_chunks_fts or lexical_chunks_fts4",
            PackInstaller.missingRequiredPackTablesForTest(Set.of("guides", "guide_related"))
        );
    }
}
