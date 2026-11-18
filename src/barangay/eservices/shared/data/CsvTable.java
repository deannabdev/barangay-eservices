package data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PushbackReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Reads and writes CSV rows under a fixed header. */
public final class CsvTable {
    private final List<String> header;

    public CsvTable(List<String> header) {
        Objects.requireNonNull(header, "header");
        if (header.isEmpty()) throw new IllegalArgumentException("CSV header must not be empty.");
        this.header = List.copyOf(header);
        for (String column : this.header) Objects.requireNonNull(column, "header column");
    }

    /** Loads data rows, creating a header-only file if it does not exist. */
    public synchronized List<List<String>> load(Path file) throws IOException {
        Objects.requireNonNull(file, "file");
        if (!Files.exists(file) || Files.size(file) == 0) {
            save(file, List.of());
            return new ArrayList<>();
        }

        List<List<String>> parsed;
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            parsed = parse(reader);
        }
        if (parsed.isEmpty() || !header.equals(parsed.get(0))) {
            throw new IOException("CSV header does not match the expected columns: " + file);
        }

        List<List<String>> rows = new ArrayList<>(parsed.size() - 1);
        for (int i = 1; i < parsed.size(); i++) {
            List<String> row = parsed.get(i);
            validateRow(row);
            rows.add(row);
        }
        return rows;
    }

    /** Replaces all data rows while retaining the configured header. */
    public synchronized void save(Path file, List<? extends List<String>> rows) throws IOException {
        Objects.requireNonNull(file, "file");
        Objects.requireNonNull(rows, "rows");
        Path directory = file.toAbsolutePath().getParent();
        Files.createDirectories(directory);
        Path temporaryFile = Files.createTempFile(directory, "csv-", ".tmp");
        try {
            try (BufferedWriter writer = Files.newBufferedWriter(temporaryFile, StandardCharsets.UTF_8)) {
                writeRow(writer, header);
                for (List<String> row : rows) {
                    validateRow(row);
                    writeRow(writer, row);
                }
            }
            try {
                Files.move(temporaryFile, file, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporaryFile, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    /** Adds one data row. */
    public synchronized void append(Path file, List<String> row) throws IOException {
        validateRow(row);
        List<List<String>> rows = load(file);
        rows.add(new ArrayList<>(row));
        save(file, rows);
    }

    /** Returns the first row whose value in {@code columnIndex} exactly matches {@code value}. */
    public synchronized Optional<List<String>> find(Path file, int columnIndex, String value) throws IOException {
        if (columnIndex < 0 || columnIndex >= header.size()) {
            throw new IndexOutOfBoundsException("CSV column index is outside the header.");
        }
        for (List<String> row : load(file)) {
            if (Objects.equals(row.get(columnIndex), value)) return Optional.of(row);
        }
        return Optional.empty();
    }

    private void validateRow(List<String> row) {
        Objects.requireNonNull(row, "row");
        if (row.size() != header.size()) {
            throw new IllegalArgumentException("CSV row has " + row.size()
                    + " values; expected " + header.size() + ".");
        }
        for (String value : row) Objects.requireNonNull(value, "CSV value");
    }

    private static void writeRow(BufferedWriter writer, List<String> row) throws IOException {
        for (int i = 0; i < row.size(); i++) {
            if (i > 0) writer.write(',');
            String value = row.get(i);
            boolean quote = value.indexOf(',') >= 0 || value.indexOf('"') >= 0
                    || value.indexOf('\r') >= 0 || value.indexOf('\n') >= 0;
            if (quote) writer.write('"');
            for (int j = 0; j < value.length(); j++) {
                char character = value.charAt(j);
                if (character == '"') writer.write("\"\"");
                else writer.write(character);
            }
            if (quote) writer.write('"');
        }
        writer.write('\n');
    }

    private static List<List<String>> parse(BufferedReader reader) throws IOException {
        PushbackReader input = new PushbackReader(reader, 1);
        List<List<String>> records = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        boolean afterQuote = false;
        boolean rowStarted = false;

        int next;
        while ((next = input.read()) != -1) {
            char character = (char) next;
            if (quoted) {
                if (character == '"') {
                    int following = input.read();
                    if (following == '"') field.append('"');
                    else {
                        quoted = false;
                        afterQuote = true;
                        if (following != -1) input.unread(following);
                    }
                } else {
                    field.append(character);
                }
                rowStarted = true;
                continue;
            }

            if (afterQuote) {
                if (character == ',') {
                    row.add(field.toString());
                    field.setLength(0);
                    afterQuote = false;
                } else if (character == '\r' || character == '\n') {
                    row.add(field.toString());
                    records.add(row);
                    row = new ArrayList<>();
                    field.setLength(0);
                    afterQuote = false;
                    rowStarted = false;
                    consumeLineFeed(input, character);
                } else {
                    throw new IOException("Unexpected character after a quoted CSV value.");
                }
                continue;
            }

            if (character == '"') {
                if (field.length() != 0) throw new IOException("Quote inside an unquoted CSV value.");
                quoted = true;
                rowStarted = true;
            } else if (character == ',') {
                row.add(field.toString());
                field.setLength(0);
                rowStarted = true;
            } else if (character == '\r' || character == '\n') {
                row.add(field.toString());
                records.add(row);
                row = new ArrayList<>();
                field.setLength(0);
                rowStarted = false;
                consumeLineFeed(input, character);
            } else {
                field.append(character);
                rowStarted = true;
            }
        }

        if (quoted) throw new IOException("Unterminated quoted CSV value.");
        if (rowStarted || !row.isEmpty() || field.length() > 0 || afterQuote) {
            row.add(field.toString());
            records.add(row);
        }
        return records;
    }

    private static void consumeLineFeed(PushbackReader input, char character) throws IOException {
        if (character != '\r') return;
        int following = input.read();
        if (following != '\n' && following != -1) input.unread(following);
    }
}
