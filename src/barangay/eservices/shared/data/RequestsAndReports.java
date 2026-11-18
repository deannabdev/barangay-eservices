package data;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

/** Data records for document request and report submissions stored in the local database. */
public final class RequestsAndReports {
    private static final Path REQUESTS_STORE = Paths.get("database", "requests.csv");
    private static final Path REPORTS_STORE = Paths.get("database", "reports.csv");
    private static final Path LEGACY_STORE = Paths.get("database", "submissions.properties");
    private static final List<String> REQUESTS_HEADER =
            List.of("ID", "date", "requester", "category", "details", "status");
    private static final List<String> REPORTS_HEADER =
            List.of("ID", "date", "reporter", "category", "details", "status");
    private static final String REPORT_METADATA_MARKER = "\n\n[RNR1:";
    private static final CsvTable REQUESTS_TABLE = new CsvTable(REQUESTS_HEADER);
    private static final CsvTable REPORTS_TABLE = new CsvTable(REPORTS_HEADER);

    private RequestsAndReports() {}

    enum SubmissionType {
        DOCUMENT_REQUEST("Document Request"),
        REPORT("Report");

        private final String label;

        SubmissionType(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public enum SubmissionStatus {
        PENDING("Pending"),
        IN_PROGRESS("In Progress"),
        COMPLETED("Completed"),
        REJECTED("Rejected");

        private final String label;

        SubmissionStatus(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    /** Stores a record in the CSV table that matches its kind. */
    public static synchronized void save(SubmissionRecord record) throws IOException {
        ensureStores();
        if ("REPORT".equals(record.kind)) {
            REPORTS_TABLE.append(REPORTS_STORE, reportRow(record));
        } else if ("DOCUMENT".equals(record.kind) || "DOCUMENT_REQUEST".equals(record.kind)) {
            REQUESTS_TABLE.append(REQUESTS_STORE, requestRow(record));
        } else {
            throw new IllegalArgumentException("Unknown submission kind: " + record.kind);
        }
    }

    /** Loads records for a submission kind. Accepts REPORT, DOCUMENT, or DOCUMENT_REQUEST. */
    public static synchronized List<SubmissionRecord> load(String kind) throws IOException {
        ensureStores();
        if ("REPORT".equals(kind)) {
            List<SubmissionRecord> records = new ArrayList<>();
            for (List<String> row : REPORTS_TABLE.load(REPORTS_STORE)) records.add(reportRecord(row));
            return records;
        }
        if ("DOCUMENT".equals(kind) || "DOCUMENT_REQUEST".equals(kind)) {
            List<SubmissionRecord> records = new ArrayList<>();
            for (List<String> row : REQUESTS_TABLE.load(REQUESTS_STORE)) records.add(requestRecord(row));
            return records;
        }
        throw new IllegalArgumentException("Unknown submission kind: " + kind);
    }

    /** A submission record for a document request or report. */
    public static final class SubmissionRecord {
        public final String kind;           // "REPORT" or "DOCUMENT"
        public final String title;
        public final String reference;
        public final SubmissionStatus status;
        public final LocalDateTime submittedAt;
        public final String submittedBy;
        public final String category;       // nullable for documents
        public final String location;       // nullable for documents
        public final String details;        // nullable for documents
        public final String photoPath;      // nullable for documents

        public SubmissionRecord(String kind, String title, String reference, SubmissionStatus status,
                                LocalDateTime submittedAt, String submittedBy, String category,
                                String location, String details, String photoPath) {
            this.kind = kind;
            this.title = title;
            this.reference = reference;
            this.status = status;
            this.submittedAt = submittedAt;
            this.submittedBy = submittedBy;
            this.category = category;
            this.location = location;
            this.details = details;
            this.photoPath = photoPath;
        }

        public String getReference() {
            return reference;
        }

        public SubmissionStatus getStatus() {
            return status;
        }

        public long getSubmittedAt() {
            return submittedAt.toInstant(ZoneOffset.UTC).toEpochMilli();
        }

        public String getSubmittedBy() {
            return submittedBy;
        }

        public String getSubmittedAtFormatted() {
            return submittedAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a"));
        }
    }

    private static void ensureStores() throws IOException {
        boolean requestsExist = Files.exists(REQUESTS_STORE);
        boolean reportsExist = Files.exists(REPORTS_STORE);
        if (!requestsExist && !reportsExist && Files.exists(LEGACY_STORE)) {
            migrateLegacyStore();
        } else if (requestsExist != reportsExist) {
            throw new IOException("Submission storage migration is incomplete; restore both CSV files before continuing.");
        }
        REQUESTS_TABLE.load(REQUESTS_STORE);
        REPORTS_TABLE.load(REPORTS_STORE);
    }

    private static void migrateLegacyStore() throws IOException {
        Properties legacy = new Properties();
        try (InputStream input = Files.newInputStream(LEGACY_STORE)) {
            legacy.load(input);
        }
        List<List<String>> requestRows = new ArrayList<>();
        List<List<String>> reportRows = new ArrayList<>();
        Set<String> references = new TreeSet<>();
        for (String key : legacy.stringPropertyNames()) {
            if (key.startsWith("submission.") && key.endsWith(".kind")) {
                references.add(key.substring("submission.".length(), key.length() - ".kind".length()));
            }
        }

        for (String reference : references) {
            String prefix = "submission." + reference + ".";
            String kind = legacy.getProperty(prefix + "kind");
            SubmissionRecord record = new SubmissionRecord(
                    kind,
                    legacy.getProperty(prefix + "title", ""),
                    legacy.getProperty(prefix + "reference", reference),
                    parseStatus(legacy.getProperty(prefix + "status", "PENDING")),
                    parseDate(legacy.getProperty(prefix + "submittedAt")),
                    emptyToNull(legacy.getProperty(prefix + "submittedBy", "")),
                    emptyToNull(legacy.getProperty(prefix + "category", "")),
                    emptyToNull(legacy.getProperty(prefix + "location", "")),
                    emptyToNull(legacy.getProperty(prefix + "details", "")),
                    emptyToNull(legacy.getProperty(prefix + "photoPath", "")));
            if ("REPORT".equals(kind)) reportRows.add(reportRow(record));
            else if ("DOCUMENT".equals(kind) || "DOCUMENT_REQUEST".equals(kind)) requestRows.add(requestRow(record));
            else throw new IOException("Unknown submission kind in legacy storage: " + kind);
        }

        try {
            REQUESTS_TABLE.save(REQUESTS_STORE, requestRows);
            REPORTS_TABLE.save(REPORTS_STORE, reportRows);
        } catch (IOException e) {
            try {
                Files.deleteIfExists(REQUESTS_STORE);
                Files.deleteIfExists(REPORTS_STORE);
            } catch (IOException cleanupError) {
                e.addSuppressed(cleanupError);
            }
            throw e;
        }
    }

    private static List<String> requestRow(SubmissionRecord record) {
        String details = record.details == null ? nullToEmpty(record.location) : record.details;
        return List.of(record.reference, record.submittedAt.toString(), nullToEmpty(record.submittedBy),
                nullToEmpty(record.title), details, record.status.name());
    }

    private static List<String> reportRow(SubmissionRecord record) {
        String details = packReportDetails(record.location, record.details, record.photoPath);
        return List.of(record.reference, record.submittedAt.toString(), nullToEmpty(record.submittedBy),
                nullToEmpty(record.category == null ? record.title : record.category), details, record.status.name());
    }

    private static SubmissionRecord requestRecord(List<String> row) throws IOException {
        return new SubmissionRecord("DOCUMENT", row.get(3), row.get(0), parseStatus(row.get(5)),
                parseDate(row.get(1)), emptyToNull(row.get(2)), null, emptyToNull(row.get(4)), null, null);
    }

    private static SubmissionRecord reportRecord(List<String> row) throws IOException {
        ReportDetails details = unpackReportDetails(row.get(4));
        return new SubmissionRecord("REPORT", row.get(3), row.get(0), parseStatus(row.get(5)),
                parseDate(row.get(1)), emptyToNull(row.get(2)), row.get(3), details.location,
                details.details, details.photoPath);
    }

    private static String packReportDetails(String location, String details, String photoPath) {
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        return nullToEmpty(details) + REPORT_METADATA_MARKER
                + encoder.encodeToString(nullToEmpty(location).getBytes(StandardCharsets.UTF_8)) + ":"
                + encoder.encodeToString(nullToEmpty(photoPath).getBytes(StandardCharsets.UTF_8)) + "]";
    }

    private static ReportDetails unpackReportDetails(String value) throws IOException {
        int marker = value.lastIndexOf(REPORT_METADATA_MARKER);
        if (marker < 0 || !value.endsWith("]")) return new ReportDetails("", value, "");
        String[] parts = value.substring(marker + REPORT_METADATA_MARKER.length(), value.length() - 1).split(":", -1);
        if (parts.length != 2) throw new IOException("Invalid report details metadata.");
        try {
            Base64.Decoder decoder = Base64.getUrlDecoder();
            String location = new String(decoder.decode(parts[0]), StandardCharsets.UTF_8);
            String photoPath = new String(decoder.decode(parts[1]), StandardCharsets.UTF_8);
            return new ReportDetails(location, value.substring(0, marker), photoPath);
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid report details metadata.", e);
        }
    }

    private static SubmissionStatus parseStatus(String value) throws IOException {
        try {
            return SubmissionStatus.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid submission status in storage: " + value, e);
        }
    }

    private static LocalDateTime parseDate(String value) throws IOException {
        if (value == null || value.isEmpty()) throw new IOException("Submission date is missing from storage.");
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException e) {
            throw new IOException("Invalid submission date in storage.", e);
        }
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String emptyToNull(String value) {
        return value == null || value.isEmpty() ? null : value;
    }

    private static final class ReportDetails {
        private final String location;
        private final String details;
        private final String photoPath;

        private ReportDetails(String location, String details, String photoPath) {
            this.location = location;
            this.details = details;
            this.photoPath = photoPath;
        }
    }

    /** Generates a unique submission reference (e.g. BS-A1B2C3D4). */
    public static String generateReference() {
        return "BS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
