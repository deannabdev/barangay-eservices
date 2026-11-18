package data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Local demo account storage. Passwords and reset codes are stored as salted hashes. */
public final class AccountsStore {
    private static final int ITERATIONS = 120_000;
    private static final int HASH_BITS = 256;
    private static final long RESET_CODE_LIFETIME_MILLIS = 15 * 60 * 1000L;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Path DATABASE_DIRECTORY = Paths.get("database");
    private static final Path ACCOUNTS_STORE = DATABASE_DIRECTORY.resolve("accounts.csv");
    private static final Path RESIDENTS_STORE = DATABASE_DIRECTORY.resolve("residents.csv");
    private static final Path LEGACY_STORE = DATABASE_DIRECTORY.resolve("accounts.properties");
    private static final Path ID_PHOTO_DIRECTORY = DATABASE_DIRECTORY.resolve("id-photos");
    private static final List<String> ACCOUNT_HEADER =
            List.of("ID", "emailOrMobile", "passwordHash");
    private static final List<String> RESIDENT_HEADER = List.of(
            "ID", "firstName", "middleName", "lastName", "suffix", "birthDate", "gender", "civilStatus",
            "houseNumber", "street", "barangay", "city", "email", "mobile", "idType", "idPhoto");
    private static final int ACCOUNT_ID_COLUMN = 0;
    private static final int ACCOUNT_IDENTIFIER_COLUMN = 1;
    private static final int ACCOUNT_PASSWORD_HASH_COLUMN = 2;
    private static final int RESIDENT_ID_COLUMN = 0;
    private static final int RESIDENT_FIRST_NAME_COLUMN = 1;
    private static final int RESIDENT_MIDDLE_NAME_COLUMN = 2;
    private static final int RESIDENT_LAST_NAME_COLUMN = 3;
    private static final int RESIDENT_SUFFIX_COLUMN = 4;
    private static final int RESIDENT_BIRTH_DATE_COLUMN = 5;
    private static final int RESIDENT_GENDER_COLUMN = 6;
    private static final int RESIDENT_CIVIL_STATUS_COLUMN = 7;
    private static final int RESIDENT_HOUSE_NUMBER_COLUMN = 8;
    private static final int RESIDENT_STREET_COLUMN = 9;
    private static final int RESIDENT_BARANGAY_COLUMN = 10;
    private static final int RESIDENT_CITY_COLUMN = 11;
    private static final int RESIDENT_EMAIL_COLUMN = 12;
    private static final int RESIDENT_MOBILE_COLUMN = 13;
    private static final int RESIDENT_ID_TYPE_COLUMN = 14;
    private static final int RESIDENT_ID_PHOTO_COLUMN = 15;
    private static final CsvTable ACCOUNT_TABLE = new CsvTable(ACCOUNT_HEADER);
    private static final CsvTable RESIDENT_TABLE = new CsvTable(RESIDENT_HEADER);
    private static final Map<String, ResetCode> RESET_CODES = new HashMap<>();
    private static String currentAccountId;

    private AccountsStore() {}

    public static synchronized boolean register(RegistrationData profile, String idType, File idPhoto,
                                                char[] password)
            throws IOException, GeneralSecurityException {
        if (profile == null) throw new IllegalArgumentException("Profile is required.");
        ensureAccountStores();
        if (profile.email.trim().isEmpty() || profile.mobile.trim().isEmpty()) {
            throw new IOException("Email address and mobile number are required.");
        }

        List<List<String>> oldAccounts = ACCOUNT_TABLE.load(ACCOUNTS_STORE);
        List<List<String>> oldResidents = RESIDENT_TABLE.load(RESIDENTS_STORE);
        Set<String> identifiers = aliases(profile.email, profile.mobile);
        if (identifiers.isEmpty()) throw new IOException("At least one login identifier is required.");
        for (String identifier : identifiers) {
            if (findAccount(oldAccounts, identifier).isPresent()) return false;
        }
        if (idPhoto == null || !Files.isRegularFile(idPhoto.toPath())) {
            throw new IOException("The selected ID photo could not be found.");
        }

        String accountId = nextResidentId(oldAccounts, oldResidents);
        String extension = photoExtension(idPhoto);
        Files.createDirectories(ID_PHOTO_DIRECTORY);
        Path storedPhoto = ID_PHOTO_DIRECTORY.resolve(accountId + extension);
        byte[] salt = randomBytes(16);
        String passwordHash = encodeCredential(salt, hash(password, salt, ITERATIONS));
        List<List<String>> newAccounts = copyRows(oldAccounts);
        for (String identifier : identifiers) {
            newAccounts.add(accountRow(accountId, identifier, passwordHash));
        }
        List<List<String>> newResidents = copyRows(oldResidents);
        newResidents.add(residentRow(accountId, profile, idType, storedPhoto.toString()));

        try {
            Files.copy(idPhoto.toPath(), storedPhoto);
            saveAccountAndResidentRows(oldAccounts, oldResidents, newAccounts, newResidents);
        } catch (IOException e) {
            try {
                Files.deleteIfExists(storedPhoto);
            } catch (IOException cleanupError) {
                e.addSuppressed(cleanupError);
            }
            throw e;
        }
        return true;
    }

    private static String photoExtension(File photo) throws IOException {
        String name = photo.getName().toLowerCase(Locale.ROOT);
        if (name.endsWith(".jpeg")) return ".jpeg";
        if (name.endsWith(".jpg")) return ".jpg";
        if (name.endsWith(".png")) return ".png";
        throw new IOException("The ID photo must be a JPG, JPEG, or PNG file.");
    }

    public static synchronized boolean authenticate(String identifier, char[] password)
            throws IOException, GeneralSecurityException {
        currentAccountId = null;
        ensureAccountStores();
        if (identifier == null) return false;
        Optional<List<String>> account = ACCOUNT_TABLE.find(
                ACCOUNTS_STORE, ACCOUNT_IDENTIFIER_COLUMN, normalizeIdentifier(identifier));
        if (account.isEmpty()) return false;

        String accountId = account.get().get(ACCOUNT_ID_COLUMN);
        Credential credential = parseCredential(account.get().get(ACCOUNT_PASSWORD_HASH_COLUMN));
        boolean authenticated = MessageDigest.isEqual(
                credential.hash, hash(password, credential.salt, credential.iterations));
        currentAccountId = authenticated ? accountId : null;
        return authenticated;
    }

    public static synchronized AccountProfile getCurrentProfile() throws IOException {
        if (currentAccountId == null) return null;
        ensureAccountStores();
        Optional<List<String>> resident = RESIDENT_TABLE.find(
                RESIDENTS_STORE, RESIDENT_ID_COLUMN, currentAccountId);
        if (resident.isEmpty()) {
            throw new IOException("The signed-in account profile could not be found.");
        }
        List<String> row = resident.get();
        return new AccountProfile(
                row.get(RESIDENT_FIRST_NAME_COLUMN),
                row.get(RESIDENT_MIDDLE_NAME_COLUMN),
                row.get(RESIDENT_LAST_NAME_COLUMN),
                row.get(RESIDENT_SUFFIX_COLUMN),
                row.get(RESIDENT_BIRTH_DATE_COLUMN),
                row.get(RESIDENT_GENDER_COLUMN),
                row.get(RESIDENT_CIVIL_STATUS_COLUMN),
                row.get(RESIDENT_HOUSE_NUMBER_COLUMN),
                row.get(RESIDENT_STREET_COLUMN),
                row.get(RESIDENT_BARANGAY_COLUMN),
                row.get(RESIDENT_CITY_COLUMN),
                row.get(RESIDENT_EMAIL_COLUMN),
                row.get(RESIDENT_MOBILE_COLUMN));
    }

    public static synchronized void updateCurrentProfile(RegistrationData profile) throws IOException {
        if (currentAccountId == null) {
            throw new IOException("There is no signed-in account to update.");
        }
        if (profile == null || profile.email.trim().isEmpty() || profile.mobile.trim().isEmpty()) {
            throw new IOException("Email address and mobile number are required.");
        }
        ensureAccountStores();

        List<List<String>> oldAccounts = ACCOUNT_TABLE.load(ACCOUNTS_STORE);
        List<List<String>> oldResidents = RESIDENT_TABLE.load(RESIDENTS_STORE);
        List<String> currentResident = findResident(oldResidents, currentAccountId)
                .orElseThrow(() -> new IOException("The signed-in account profile could not be found."));

        Set<String> identifiers = aliases(profile.email, profile.mobile);
        for (String identifier : identifiers) {
            Optional<List<String>> owner = findAccount(oldAccounts, identifier);
            if (owner.isPresent() && !currentAccountId.equals(owner.get().get(ACCOUNT_ID_COLUMN))) {
                throw new IOException("That email address or mobile number is already linked to another account.");
            }
        }

        String passwordHash = null;
        List<List<String>> newAccounts = new ArrayList<>();
        for (List<String> row : oldAccounts) {
            if (currentAccountId.equals(row.get(ACCOUNT_ID_COLUMN))) {
                if (passwordHash == null) passwordHash = row.get(ACCOUNT_PASSWORD_HASH_COLUMN);
            } else {
                newAccounts.add(new ArrayList<>(row));
            }
        }
        if (passwordHash == null) throw new IOException("The signed-in account login data could not be found.");
        for (String identifier : identifiers) newAccounts.add(accountRow(currentAccountId, identifier, passwordHash));

        List<List<String>> newResidents = copyRows(oldResidents);
        int residentIndex = newResidents.indexOf(currentResident);
        newResidents.set(residentIndex, residentRow(currentAccountId, profile,
                currentResident.get(RESIDENT_ID_TYPE_COLUMN), currentResident.get(RESIDENT_ID_PHOTO_COLUMN)));
        saveAccountAndResidentRows(oldAccounts, oldResidents, newAccounts, newResidents);
    }

    public static synchronized void clearCurrentAccount() {
        currentAccountId = null;
    }

    static synchronized String getCurrentAccountId() {
        return currentAccountId;
    }

    /** Returns a six-digit demo code, or null when the account does not exist. */
    public static synchronized String createResetCode(String identifier)
            throws IOException, GeneralSecurityException {
        ensureAccountStores();
        if (identifier == null) return null;
        Optional<List<String>> account = ACCOUNT_TABLE.find(
                ACCOUNTS_STORE, ACCOUNT_IDENTIFIER_COLUMN, normalizeIdentifier(identifier));
        if (account.isEmpty()) return null;

        String code = String.format(Locale.ROOT, "%06d", RANDOM.nextInt(1_000_000));
        byte[] salt = randomBytes(16);
        RESET_CODES.put(account.get().get(ACCOUNT_ID_COLUMN),
                new ResetCode(salt, hashString(code, salt), System.currentTimeMillis() + RESET_CODE_LIFETIME_MILLIS));
        return code;
    }

    public static synchronized boolean resetPassword(String identifier, String code, char[] newPassword)
            throws IOException, GeneralSecurityException {
        ensureAccountStores();
        if (identifier == null) return false;
        Optional<List<String>> account = ACCOUNT_TABLE.find(
                ACCOUNTS_STORE, ACCOUNT_IDENTIFIER_COLUMN, normalizeIdentifier(identifier));
        if (account.isEmpty()) return false;

        String accountId = account.get().get(ACCOUNT_ID_COLUMN);
        ResetCode resetCode = RESET_CODES.get(accountId);
        if (resetCode == null) return false;
        if (System.currentTimeMillis() > resetCode.expiresAt) {
            RESET_CODES.remove(accountId);
            return false;
        }
        if (!MessageDigest.isEqual(resetCode.hash, hashString(code, resetCode.salt))) return false;

        List<List<String>> accounts = ACCOUNT_TABLE.load(ACCOUNTS_STORE);
        byte[] salt = randomBytes(16);
        String passwordHash = encodeCredential(salt, hash(newPassword, salt, ITERATIONS));
        boolean updated = false;
        for (int i = 0; i < accounts.size(); i++) {
            List<String> row = accounts.get(i);
            if (accountId.equals(row.get(ACCOUNT_ID_COLUMN))) {
                accounts.set(i, accountRow(accountId, row.get(ACCOUNT_IDENTIFIER_COLUMN), passwordHash));
                updated = true;
            }
        }
        if (!updated) throw new IOException("The account login data could not be found.");
        ACCOUNT_TABLE.save(ACCOUNTS_STORE, accounts);
        RESET_CODES.remove(accountId);
        return true;
    }

    public static synchronized boolean changeCurrentPassword(char[] currentPassword, char[] newPassword)
            throws IOException, GeneralSecurityException {
        if (currentAccountId == null) {
            throw new IOException("There is no signed-in account to update.");
        }
        if (currentPassword == null || newPassword == null) {
            throw new IllegalArgumentException("Current and new passwords are required.");
        }
        if (newPassword.length < 8) {
            throw new IOException("Your new password must be at least 8 characters.");
        }
        if (Arrays.equals(currentPassword, newPassword)) {
            throw new IOException("Your new password must be different from the current one.");
        }

        ensureAccountStores();
        List<List<String>> accounts = ACCOUNT_TABLE.load(ACCOUNTS_STORE);
        String storedHash = null;
        for (List<String> row : accounts) {
            if (currentAccountId.equals(row.get(ACCOUNT_ID_COLUMN))) {
                storedHash = row.get(ACCOUNT_PASSWORD_HASH_COLUMN);
                break;
            }
        }
        if (storedHash == null) throw new IOException("The signed-in account login data could not be found.");

        Credential credential = parseCredential(storedHash);
        if (!MessageDigest.isEqual(credential.hash, hash(currentPassword, credential.salt, credential.iterations))) {
            return false;
        }

        byte[] salt = randomBytes(16);
        String passwordHash = encodeCredential(salt, hash(newPassword, salt, ITERATIONS));
        for (int i = 0; i < accounts.size(); i++) {
            List<String> row = accounts.get(i);
            if (currentAccountId.equals(row.get(ACCOUNT_ID_COLUMN))) {
                accounts.set(i, accountRow(currentAccountId, row.get(ACCOUNT_IDENTIFIER_COLUMN), passwordHash));
            }
        }
        ACCOUNT_TABLE.save(ACCOUNTS_STORE, accounts);
        RESET_CODES.remove(currentAccountId);
        return true;
    }

    public static synchronized void deleteCurrentAccount() throws IOException {
        if (currentAccountId == null) {
            throw new IOException("There is no signed-in account to delete.");
        }
        ensureAccountStores();

        List<List<String>> oldAccounts = ACCOUNT_TABLE.load(ACCOUNTS_STORE);
        List<List<String>> oldResidents = RESIDENT_TABLE.load(RESIDENTS_STORE);
        List<String> resident = findResident(oldResidents, currentAccountId)
                .orElseThrow(() -> new IOException("The signed-in account profile could not be found."));
        String photoPathValue = resident.get(RESIDENT_ID_PHOTO_COLUMN);
        Path photoPath = null;
        if (!photoPathValue.isEmpty()) {
            photoPath = Paths.get(photoPathValue).toAbsolutePath().normalize();
            Path photoDirectory = ID_PHOTO_DIRECTORY.toAbsolutePath().normalize();
            if (!photoDirectory.equals(photoPath.getParent())) {
                throw new IOException("The stored ID photo path is outside the account photo directory.");
            }
        }

        List<List<String>> newAccounts = new ArrayList<>();
        for (List<String> row : oldAccounts) {
            if (!currentAccountId.equals(row.get(ACCOUNT_ID_COLUMN))) newAccounts.add(new ArrayList<>(row));
        }
        if (newAccounts.size() == oldAccounts.size()) {
            throw new IOException("The signed-in account login data could not be found.");
        }
        List<List<String>> newResidents = new ArrayList<>();
        for (List<String> row : oldResidents) {
            if (!currentAccountId.equals(row.get(RESIDENT_ID_COLUMN))) newResidents.add(new ArrayList<>(row));
        }

        saveAccountAndResidentRows(oldAccounts, oldResidents, newAccounts, newResidents);
        try {
            if (photoPath != null) Files.deleteIfExists(photoPath);
        } catch (IOException photoError) {
            try {
                saveAccountAndResidentRows(newAccounts, newResidents, oldAccounts, oldResidents);
            } catch (IOException rollbackError) {
                photoError.addSuppressed(rollbackError);
                throw new IOException("Could not remove the ID photo or restore the account records.", photoError);
            }
            throw new IOException("Could not remove the ID photo; account deletion was rolled back.", photoError);
        }
        RESET_CODES.remove(currentAccountId);
        currentAccountId = null;
    }

    public static synchronized RequestsAndReports.SubmissionRecord addDocumentSubmission(
            String title, String name, String contact) throws IOException {
        RequestsAndReports.SubmissionRecord record = new RequestsAndReports.SubmissionRecord(
                "DOCUMENT", title, RequestsAndReports.generateReference(),
                RequestsAndReports.SubmissionStatus.PENDING, LocalDateTime.now(), name,
                null, contact, null, null);
        RequestsAndReports.save(record);
        return record;
    }

    public static synchronized RequestsAndReports.SubmissionRecord addReportSubmission(
            String category, String location, String details, File photo) throws IOException {
        AccountProfile reporterProfile = getCurrentProfile();
        String reporter = reporterProfile == null ? "" : fullName(reporterProfile);
        String photoPath = photo == null ? null : photo.getPath();
        RequestsAndReports.SubmissionRecord record = new RequestsAndReports.SubmissionRecord(
                "REPORT", category, RequestsAndReports.generateReference(),
                RequestsAndReports.SubmissionStatus.PENDING, LocalDateTime.now(), reporter,
                category, location, details, photoPath);
        RequestsAndReports.save(record);
        return record;
    }

    public static synchronized List<RequestsAndReports.SubmissionRecord> getCurrentSubmissions(String type)
            throws IOException {
        return RequestsAndReports.load(type);
    }

    private static void ensureAccountStores() throws IOException {
        boolean accountsExist = Files.exists(ACCOUNTS_STORE);
        boolean residentsExist = Files.exists(RESIDENTS_STORE);
        if (!accountsExist && !residentsExist && Files.exists(LEGACY_STORE)) {
            migrateLegacyAccounts();
        } else if (accountsExist != residentsExist) {
            throw new IOException("Account storage migration is incomplete; restore both CSV files before continuing.");
        }
        ACCOUNT_TABLE.load(ACCOUNTS_STORE);
        RESIDENT_TABLE.load(RESIDENTS_STORE);
    }

    private static void migrateLegacyAccounts() throws IOException {
        Properties legacy = new Properties();
        try (InputStream input = Files.newInputStream(LEGACY_STORE)) {
            legacy.load(input);
        }

        List<String> legacyIds = new ArrayList<>();
        String suffix = ".passwordHash";
        for (String key : legacy.stringPropertyNames()) {
            if (key.startsWith("account.") && key.endsWith(suffix)) {
                legacyIds.add(key.substring("account.".length(), key.length() - suffix.length()));
            }
        }
        legacyIds.sort(String::compareTo);

        List<List<String>> accounts = new ArrayList<>();
        List<List<String>> residents = new ArrayList<>();
        int nextId = 1;
        for (String legacyId : legacyIds) {
            String prefix = "account." + legacyId + ".";
            String saltValue = legacy.getProperty(prefix + "salt");
            String hashValue = legacy.getProperty(prefix + "passwordHash");
            if (saltValue == null || hashValue == null) {
                throw new IOException("Legacy account is missing its password hash data.");
            }
            String passwordHash = encodeCredential(decode(saltValue), decode(hashValue));
            RegistrationData profile = new RegistrationData();
            profile.firstName = legacy.getProperty(prefix + "profile.firstName", "");
            profile.middleName = legacy.getProperty(prefix + "profile.middleName", "");
            profile.lastName = legacy.getProperty(prefix + "profile.lastName", "");
            profile.suffix = legacy.getProperty(prefix + "profile.suffix", "");
            profile.birthDate = legacy.getProperty(prefix + "profile.birthDate", "");
            profile.gender = legacy.getProperty(prefix + "profile.gender", "");
            profile.civilStatus = legacy.getProperty(prefix + "profile.civilStatus", "");
            profile.houseNumber = legacy.getProperty(prefix + "profile.houseNumber", "");
            profile.street = legacy.getProperty(prefix + "profile.street", "");
            profile.barangay = legacy.getProperty(prefix + "profile.barangay", "");
            profile.city = legacy.getProperty(prefix + "profile.city", "");
            profile.email = legacy.getProperty(prefix + "profile.email", "");
            profile.mobile = legacy.getProperty(prefix + "profile.mobile", "");

            Set<String> identifiers = aliases(profile.email, profile.mobile);
            if (identifiers.isEmpty()) throw new IOException("Legacy account has no email or mobile identifier.");
            String residentId = String.format(Locale.ROOT, "R%03d", nextId++);
            for (String identifier : identifiers) accounts.add(accountRow(residentId, identifier, passwordHash));
            residents.add(residentRow(residentId, profile,
                    legacy.getProperty(prefix + "profile.idType", ""),
                    legacy.getProperty(prefix + "profile.idPhoto", "")));
        }

        try {
            ACCOUNT_TABLE.save(ACCOUNTS_STORE, accounts);
            RESIDENT_TABLE.save(RESIDENTS_STORE, residents);
        } catch (IOException e) {
            try {
                Files.deleteIfExists(ACCOUNTS_STORE);
                Files.deleteIfExists(RESIDENTS_STORE);
            } catch (IOException cleanupError) {
                e.addSuppressed(cleanupError);
            }
            throw e;
        }
    }

    private static void saveAccountAndResidentRows(List<List<String>> oldAccounts, List<List<String>> oldResidents,
                                                   List<List<String>> newAccounts, List<List<String>> newResidents)
            throws IOException {
        try {
            ACCOUNT_TABLE.save(ACCOUNTS_STORE, newAccounts);
            RESIDENT_TABLE.save(RESIDENTS_STORE, newResidents);
        } catch (IOException e) {
            try {
                ACCOUNT_TABLE.save(ACCOUNTS_STORE, oldAccounts);
            } catch (IOException rollbackError) {
                e.addSuppressed(rollbackError);
            }
            try {
                RESIDENT_TABLE.save(RESIDENTS_STORE, oldResidents);
            } catch (IOException rollbackError) {
                e.addSuppressed(rollbackError);
            }
            throw e;
        }
    }

    private static List<String> residentRow(String id, RegistrationData profile, String idType, String idPhoto) {
        return List.of(id, nullToEmpty(profile.firstName), nullToEmpty(profile.middleName),
                nullToEmpty(profile.lastName), nullToEmpty(profile.suffix), nullToEmpty(profile.birthDate),
                nullToEmpty(profile.gender), nullToEmpty(profile.civilStatus), nullToEmpty(profile.houseNumber),
                nullToEmpty(profile.street), nullToEmpty(profile.barangay), nullToEmpty(profile.city),
                nullToEmpty(profile.email), nullToEmpty(profile.mobile), nullToEmpty(idType), nullToEmpty(idPhoto));
    }

    private static List<String> accountRow(String id, String identifier, String passwordHash) {
        return List.of(id, identifier, passwordHash);
    }

    private static List<List<String>> copyRows(List<List<String>> rows) {
        List<List<String>> copy = new ArrayList<>(rows.size());
        for (List<String> row : rows) copy.add(new ArrayList<>(row));
        return copy;
    }

    private static Optional<List<String>> findAccount(List<List<String>> rows, String identifier) {
        for (List<String> row : rows) {
            if (identifier.equals(row.get(ACCOUNT_IDENTIFIER_COLUMN))) return Optional.of(row);
        }
        return Optional.empty();
    }

    private static Optional<List<String>> findResident(List<List<String>> rows, String id) {
        for (List<String> row : rows) {
            if (id.equals(row.get(RESIDENT_ID_COLUMN))) return Optional.of(row);
        }
        return Optional.empty();
    }

    private static Set<String> aliases(String email, String mobile) {
        Set<String> identifiers = new LinkedHashSet<>();
        String emailIdentifier = normalizeIdentifier(email);
        String mobileIdentifier = normalizeIdentifier(mobile);
        if (!emailIdentifier.isEmpty()) identifiers.add(emailIdentifier);
        if (!mobileIdentifier.isEmpty()) identifiers.add(mobileIdentifier);
        return identifiers;
    }

    private static String normalizeIdentifier(String identifier) {
        String normalized = identifier == null ? "" : identifier.trim();
        if (normalized.indexOf('@') >= 0) {
            normalized = normalized.toLowerCase(Locale.ROOT);
        } else {
            normalized = normalized.replaceAll("[\\s()-]", "");
            if (normalized.startsWith("+63")) normalized = "0" + normalized.substring(3);
        }
        return normalized;
    }

    private static String nextResidentId(List<List<String>> accounts, List<List<String>> residents) {
        int highest = 0;
        for (List<String> row : accounts) highest = Math.max(highest, numericResidentId(row.get(ACCOUNT_ID_COLUMN)));
        for (List<String> row : residents) highest = Math.max(highest, numericResidentId(row.get(RESIDENT_ID_COLUMN)));
        return String.format(Locale.ROOT, "R%03d", highest + 1);
    }

    private static int numericResidentId(String id) {
        if (id == null || id.length() < 2 || id.charAt(0) != 'R') return 0;
        try {
            return Integer.parseInt(id.substring(1));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String fullName(AccountProfile profile) {
        StringBuilder name = new StringBuilder();
        appendNamePart(name, profile.firstName);
        appendNamePart(name, profile.middleName);
        appendNamePart(name, profile.lastName);
        appendNamePart(name, profile.suffix);
        return name.toString();
    }

    private static void appendNamePart(StringBuilder name, String part) {
        if (part == null || part.trim().isEmpty()) return;
        if (name.length() > 0) name.append(' ');
        name.append(part.trim());
    }

    private static String encodeCredential(byte[] salt, byte[] passwordHash) {
        return encodeCredential(salt, passwordHash, ITERATIONS);
    }

    private static String encodeCredential(byte[] salt, byte[] passwordHash, int iterations) {
        return "pbkdf2-sha256$" + iterations + "$" + encode(salt) + "$" + encode(passwordHash);
    }

    private static Credential parseCredential(String value) throws IOException {
        String[] parts = value.split("\\$", -1);
        if (parts.length != 4 || !"pbkdf2-sha256".equals(parts[0])) {
            throw new IOException("The account password hash has an unsupported format.");
        }
        int iterations;
        try {
            iterations = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            throw new IOException("The account password hash has invalid parameters.", e);
        }
        if (iterations <= 0) throw new IOException("The account password hash has invalid parameters.");
        byte[] salt = decode(parts[2]);
        byte[] expectedHash = decode(parts[3]);
        if (salt.length == 0 || expectedHash.length == 0) {
            throw new IOException("The account password hash is incomplete.");
        }
        return new Credential(salt, expectedHash, iterations);
    }

    private static byte[] hash(char[] value, byte[] salt, int iterations) throws GeneralSecurityException {
        PBEKeySpec spec = new PBEKeySpec(value, salt, iterations, HASH_BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec).getEncoded();
        } finally {
            spec.clearPassword();
        }
    }

    private static byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        RANDOM.nextBytes(bytes);
        return bytes;
    }

    private static String encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    private static byte[] decode(String value) throws IOException {
        if (value == null) throw new IOException("The local account store is incomplete.");
        try {
            return Base64.getDecoder().decode(value);
        } catch (IllegalArgumentException e) {
            throw new IOException("The local account store contains invalid data.", e);
        }
    }

    private static byte[] hashString(String value, byte[] salt) throws GeneralSecurityException {
        char[] characters = value.toCharArray();
        try {
            return hash(characters, salt, ITERATIONS);
        } finally {
            Arrays.fill(characters, '\0');
        }
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static final class Credential {
        private final byte[] salt;
        private final byte[] hash;
        private final int iterations;

        private Credential(byte[] salt, byte[] hash, int iterations) {
            this.salt = salt;
            this.hash = hash;
            this.iterations = iterations;
        }
    }

    private static final class ResetCode {
        private final byte[] salt;
        private final byte[] hash;
        private final long expiresAt;

        private ResetCode(byte[] salt, byte[] hash, long expiresAt) {
            this.salt = salt;
            this.hash = hash;
            this.expiresAt = expiresAt;
        }
    }

    public static final class AccountProfile {
        public final String firstName;
        public final String middleName;
        public final String lastName;
        public final String suffix;
        public final String birthDate;
        public final String gender;
        public final String civilStatus;
        public final String houseNumber;
        public final String street;
        public final String barangay;
        public final String city;
        public final String email;
        public final String mobile;

        AccountProfile(String firstName, String middleName, String lastName, String suffix, String birthDate,
                       String gender, String civilStatus, String houseNumber, String street,
                       String barangay, String city, String email, String mobile) {
            this.firstName = firstName;
            this.middleName = middleName;
            this.lastName = lastName;
            this.suffix = suffix;
            this.birthDate = birthDate;
            this.gender = gender;
            this.civilStatus = civilStatus;
            this.houseNumber = houseNumber;
            this.street = street;
            this.barangay = barangay;
            this.city = city;
            this.email = email;
            this.mobile = mobile;
        }
    }

    public static final class RegistrationData {
        public String firstName = "";
        public String middleName = "";
        public String lastName = "";
        public String suffix = "";
        public String birthDate = "";
        public String gender = "";
        public String civilStatus = "";
        public String houseNumber = "";
        public String street = "";
        public String barangay = "Barangay Central Signal";
        public String city = "Taguig City";
        public String mobile = "";
        public String email = "";

        public RegistrationData() {}
    }
}
