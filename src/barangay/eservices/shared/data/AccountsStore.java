package barangay.eservices;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;
import java.util.UUID;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Local demo account storage. Passwords and reset codes are stored as salted hashes. */
final class AccountsStore {
    private static final int ITERATIONS = 120_000;
    private static final int HASH_BITS = 256;
    private static final long RESET_CODE_LIFETIME_MILLIS = 15 * 60 * 1000L;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Path STORE = Paths.get("database", "accounts.properties");
    private static String currentAccountId;

    private AccountsStore() {}

    static synchronized boolean register(RegistrationData profile, String idType, File idPhoto, char[] password)
            throws IOException, GeneralSecurityException {
        String emailKey = aliasKey(profile.email);
        String mobileKey = aliasKey(profile.mobile);
        Properties data = load();
        if (data.containsKey(emailKey) || data.containsKey(mobileKey)) return false;
        if (idPhoto == null || !Files.isRegularFile(idPhoto.toPath())) {
            throw new IOException("The selected ID photo could not be found.");
        }

        String accountId = UUID.randomUUID().toString();
        byte[] salt = randomBytes(16);
        String accountPrefix = accountPrefix(accountId);
        data.setProperty(emailKey, accountId);
        data.setProperty(mobileKey, accountId);
        data.setProperty(accountPrefix + "salt", encode(salt));
        data.setProperty(accountPrefix + "passwordHash", encode(hash(password, salt)));
        data.setProperty(accountPrefix + "profile.firstName", profile.firstName);
        data.setProperty(accountPrefix + "profile.middleName", profile.middleName);
        data.setProperty(accountPrefix + "profile.lastName", profile.lastName);
        data.setProperty(accountPrefix + "profile.suffix", profile.suffix);
        data.setProperty(accountPrefix + "profile.birthDate", profile.birthDate);
        data.setProperty(accountPrefix + "profile.gender", profile.gender);
        data.setProperty(accountPrefix + "profile.civilStatus", profile.civilStatus);
        data.setProperty(accountPrefix + "profile.houseNumber", profile.houseNumber);
        data.setProperty(accountPrefix + "profile.street", profile.street);
        data.setProperty(accountPrefix + "profile.barangay", profile.barangay);
        data.setProperty(accountPrefix + "profile.city", profile.city);
        data.setProperty(accountPrefix + "profile.email", profile.email);
        data.setProperty(accountPrefix + "profile.mobile", profile.mobile);
        data.setProperty(accountPrefix + "profile.idType", idType);

        String extension = photoExtension(idPhoto);
        Path photoDirectory = STORE.getParent().resolve("id-photos");
        Files.createDirectories(photoDirectory);
        Path storedPhoto = photoDirectory.resolve(accountId + extension);
        Files.copy(idPhoto.toPath(), storedPhoto);
        data.setProperty(accountPrefix + "profile.idPhoto", storedPhoto.toString());
        try {
            save(data);
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

    static synchronized boolean authenticate(String identifier, char[] password)
            throws IOException, GeneralSecurityException {
        currentAccountId = null;
        Properties data = load();
        String accountId = data.getProperty(aliasKey(identifier));
        if (accountId == null) {
            currentAccountId = null;
            return false;
        }

        String accountPrefix = accountPrefix(accountId);
        byte[] salt = decode(data.getProperty(accountPrefix + "salt"));
        byte[] expected = decode(data.getProperty(accountPrefix + "passwordHash"));
        boolean authenticated = MessageDigest.isEqual(expected, hash(password, salt));
        currentAccountId = authenticated ? accountId : null;
        return authenticated;
    }

    static synchronized AccountProfile getCurrentProfile() throws IOException {
        if (currentAccountId == null) return null;

        Properties data = load();
        String prefix = accountPrefix(currentAccountId) + "profile.";
        if (!data.containsKey(prefix + "firstName")) {
            throw new IOException("The signed-in account profile could not be found.");
        }
        return new AccountProfile(
                data.getProperty(prefix + "firstName", ""),
                data.getProperty(prefix + "middleName", ""),
                data.getProperty(prefix + "lastName", ""),
                data.getProperty(prefix + "suffix", ""),
                data.getProperty(prefix + "birthDate", ""),
                data.getProperty(prefix + "gender", ""),
                data.getProperty(prefix + "civilStatus", ""),
                data.getProperty(prefix + "houseNumber", ""),
                data.getProperty(prefix + "street", ""),
                data.getProperty(prefix + "barangay", ""),
                data.getProperty(prefix + "city", ""),
                data.getProperty(prefix + "email", ""),
                data.getProperty(prefix + "mobile", ""));
    }

    static synchronized void updateCurrentProfile(RegistrationData profile) throws IOException {
        if (currentAccountId == null) {
            throw new IOException("There is no signed-in account to update.");
        }
        if (profile.email.trim().isEmpty() || profile.mobile.trim().isEmpty()) {
            throw new IOException("Email address and mobile number are required.");
        }

        Properties data = load();
        String prefix = accountPrefix(currentAccountId) + "profile.";
        if (!data.containsKey(prefix + "firstName")) {
            throw new IOException("The signed-in account profile could not be found.");
        }

        String emailKey = aliasKey(profile.email);
        String mobileKey = aliasKey(profile.mobile);
        String emailOwner = data.getProperty(emailKey);
        String mobileOwner = data.getProperty(mobileKey);
        if ((emailOwner != null && !currentAccountId.equals(emailOwner))
                || (mobileOwner != null && !currentAccountId.equals(mobileOwner))) {
            throw new IOException("That email address or mobile number is already linked to another account.");
        }

        removeCurrentAlias(data, prefix + "email", currentAccountId);
        removeCurrentAlias(data, prefix + "mobile", currentAccountId);
        data.setProperty(emailKey, currentAccountId);
        data.setProperty(mobileKey, currentAccountId);
        data.setProperty(prefix + "firstName", profile.firstName);
        data.setProperty(prefix + "middleName", profile.middleName);
        data.setProperty(prefix + "lastName", profile.lastName);
        data.setProperty(prefix + "suffix", profile.suffix);
        data.setProperty(prefix + "birthDate", profile.birthDate);
        data.setProperty(prefix + "gender", profile.gender);
        data.setProperty(prefix + "civilStatus", profile.civilStatus);
        data.setProperty(prefix + "houseNumber", profile.houseNumber);
        data.setProperty(prefix + "street", profile.street);
        data.setProperty(prefix + "barangay", profile.barangay);
        data.setProperty(prefix + "city", profile.city);
        data.setProperty(prefix + "email", profile.email);
        data.setProperty(prefix + "mobile", profile.mobile);
        save(data);
    }

    private static void removeCurrentAlias(Properties data, String profileKey, String accountId) {
        String identifier = data.getProperty(profileKey);
        if (identifier == null || identifier.trim().isEmpty()) return;

        String key = aliasKey(identifier);
        if (accountId.equals(data.getProperty(key))) data.remove(key);
    }

    static synchronized void clearCurrentAccount() {
        currentAccountId = null;
    }

    /** Returns a six-digit demo code, or null when the account does not exist. */
    static synchronized String createResetCode(String identifier)
            throws IOException, GeneralSecurityException {
        Properties data = load();
        String accountId = data.getProperty(aliasKey(identifier));
        if (accountId == null) return null;

        String code = String.format(Locale.ROOT, "%06d", RANDOM.nextInt(1_000_000));
        byte[] salt = randomBytes(16);
        String accountPrefix = accountPrefix(accountId);
        data.setProperty(accountPrefix + "resetSalt", encode(salt));
        data.setProperty(accountPrefix + "resetCodeHash", encode(hashString(code, salt)));
        data.setProperty(accountPrefix + "resetExpiresAt",
                Long.toString(System.currentTimeMillis() + RESET_CODE_LIFETIME_MILLIS));
        save(data);
        return code;
    }

    static synchronized boolean resetPassword(String identifier, String code, char[] newPassword)
            throws IOException, GeneralSecurityException {
        Properties data = load();
        String accountId = data.getProperty(aliasKey(identifier));
        if (accountId == null) return false;

        String accountPrefix = accountPrefix(accountId);
        String expiresAtValue = data.getProperty(accountPrefix + "resetExpiresAt");
        String resetSaltValue = data.getProperty(accountPrefix + "resetSalt");
        String resetHashValue = data.getProperty(accountPrefix + "resetCodeHash");
        if (expiresAtValue == null || resetSaltValue == null || resetHashValue == null) return false;

        long expiresAt;
        try {
            expiresAt = Long.parseLong(expiresAtValue);
        } catch (NumberFormatException e) {
            return false;
        }
        if (System.currentTimeMillis() > expiresAt) return false;

        byte[] resetSalt = decode(resetSaltValue);
        byte[] expectedResetHash = decode(resetHashValue);
        if (!MessageDigest.isEqual(expectedResetHash, hashString(code, resetSalt))) return false;

        byte[] passwordSalt = randomBytes(16);
        data.setProperty(accountPrefix + "salt", encode(passwordSalt));
        data.setProperty(accountPrefix + "passwordHash", encode(hash(newPassword, passwordSalt)));
        data.remove(accountPrefix + "resetSalt");
        data.remove(accountPrefix + "resetCodeHash");
        data.remove(accountPrefix + "resetExpiresAt");
        save(data);
        return true;
    }

    private static Properties load() throws IOException {
        Properties data = new Properties();
        if (!Files.exists(STORE)) return data;
        try (InputStream input = Files.newInputStream(STORE)) {
            data.load(input);
        }
        return data;
    }

    private static void save(Properties data) throws IOException {
        Path directory = STORE.getParent();
        Files.createDirectories(directory);
        Path temporaryFile = Files.createTempFile(directory, "accounts-", ".tmp");
        try {
            try (OutputStream output = Files.newOutputStream(temporaryFile)) {
                data.store(output, "Barangay Central Signal local demo accounts");
            }
            try {
                Files.move(temporaryFile, STORE, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporaryFile, STORE, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    private static String aliasKey(String identifier) {
        String normalized = identifier.trim();
        if (normalized.indexOf('@') >= 0) {
            normalized = normalized.toLowerCase(Locale.ROOT);
        } else {
            normalized = normalized.replaceAll("[\\s()-]", "");
            if (normalized.startsWith("+63")) normalized = "0" + normalized.substring(3);
        }
        return "alias." + Base64.getUrlEncoder().withoutPadding()
                .encodeToString(normalized.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private static String accountPrefix(String accountId) {
        return "account." + accountId + ".";
    }

    private static byte[] hash(char[] value, byte[] salt) throws GeneralSecurityException {
        PBEKeySpec spec = new PBEKeySpec(value, salt, ITERATIONS, HASH_BITS);
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
            return hash(characters, salt);
        } finally {
            Arrays.fill(characters, '\0');
        }
    }
}

final class AccountProfile {
    final String firstName;
    final String middleName;
    final String lastName;
    final String suffix;
    final String birthDate;
    final String gender;
    final String civilStatus;
    final String houseNumber;
    final String street;
    final String barangay;
    final String city;
    final String email;
    final String mobile;

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

final class RegistrationData {
    String firstName = "";
    String middleName = "";
    String lastName = "";
    String suffix = "";
    String birthDate = "";
    String gender = "";
    String civilStatus = "";
    String houseNumber = "";
    String street = "";
    String barangay = "Barangay Central Signal";
    String city = "Taguig City";
    String mobile = "";
    String email = "";
}