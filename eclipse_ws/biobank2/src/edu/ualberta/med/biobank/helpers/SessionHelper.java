package edu.ualberta.med.biobank.helpers;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.text.MessageFormat;
import java.util.List;

import org.acegisecurity.providers.rcp.RemoteAuthenticationException;
import org.eclipse.core.runtime.Platform;
import org.springframework.remoting.RemoteAccessException;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import edu.ualberta.med.biobank.BiobankPlugin;
import edu.ualberta.med.biobank.client.util.ServiceConnection;
import edu.ualberta.med.biobank.client.util.TrustStore;
import edu.ualberta.med.biobank.client.util.TrustStore.Cert;
import edu.ualberta.med.biobank.common.wrappers.UserWrapper;
import edu.ualberta.med.biobank.gui.common.BgcLogger;
import edu.ualberta.med.biobank.gui.common.BgcPlugin;
import edu.ualberta.med.biobank.server.applicationservice.BiobankApplicationService;
import edu.ualberta.med.biobank.server.applicationservice.exceptions.ClientVersionInvalidException;
import edu.ualberta.med.biobank.server.applicationservice.exceptions.ServerVersionInvalidException;
import edu.ualberta.med.biobank.server.applicationservice.exceptions.ServerVersionNewerException;
import edu.ualberta.med.biobank.server.applicationservice.exceptions.ServerVersionOlderException;
import gov.nih.nci.system.applicationservice.ApplicationException;

public class SessionHelper implements Runnable {
    private static final I18n i18n = I18nFactory.getI18n(SessionHelper.class);

    private static BgcLogger logger = BgcLogger.getLogger(SessionHelper.class
        .getName());

    private String serverUrl;

    private String userName;

    private final String password;

    private BiobankApplicationService appService;

    private UserWrapper user;

    @SuppressWarnings("nls")
    private static final String DOWNLOAD_URL =
        "http://aicml-med.cs.ualberta.ca/CBSR/latest.html";

    @SuppressWarnings("nls")
    private static final String DEFAULT_TEST_USER = "testuser";

    @SuppressWarnings("nls")
    private static final String DEFAULT_TEST_USER_PWD = "test";

    @SuppressWarnings("nls")
    private static final String SECURE_CONNECTION_URI = "https://";

    @SuppressWarnings("nls")
    private static final String NON_SECURE_CONNECTION_URI = "http://";

    @SuppressWarnings("nls")
    private static final String BIOBANK_URL = "/biobank";

    public SessionHelper(String server, boolean secureConnection,
        String userName, String password) {
        if (secureConnection) {
            this.serverUrl = SECURE_CONNECTION_URI;
        } else {
            this.serverUrl = NON_SECURE_CONNECTION_URI;
        }
        this.serverUrl += server + BIOBANK_URL;
        this.userName = userName;
        this.password = password;

        appService = null;
    }

    private static final String UNTRUSTED_CERT_MESSAGE =
        "The authenticity of host ''{0}'' can''t be established."
            + "\nSHA1 fingerprint is {1}"
            + "\nMD5 fingerprint is {2}"
            + "\nAre you sure you want to continue?"
            + "\n(Choosing yes will trust this certificate as "
            + "long as this program is open)";

    private void checkCertificates(String serverUrl)
        throws KeyManagementException, NoSuchAlgorithmException,
        KeyStoreException, UnknownHostException, IOException,
        CertificateException {
        TrustStore ts = TrustStore.getInstance();
        List<Cert> untrustedCerts = ts.getUntrustedCerts(serverUrl);

        if (!untrustedCerts.isEmpty()) {
            MessageDigest sha1 = MessageDigest.getInstance("SHA1");
            MessageDigest md5 = MessageDigest.getInstance("MD5");

            for (Cert untrustedCert : untrustedCerts) {
                byte[] encoded = untrustedCert.getCertificate().getEncoded();
                String host = untrustedCert.getUrl().getHost();
                String sha1Hash = toHexString(sha1.digest(encoded));
                String md5Hash = toHexString(md5.digest(encoded));

                String message = MessageFormat.format(
                    UNTRUSTED_CERT_MESSAGE, host, sha1Hash, md5Hash);

                boolean trustCert =
                    BgcPlugin.openConfirm("Untrusted SSL Certificate", message);

                if (trustCert) {
                    untrustedCert.trust();
                } else {
                    throw new RuntimeException("Untrusted SSL certificate.");
                }
            }
        }
    }

    private static final char[] HEXDIGITS = "0123456789abcdef".toCharArray();

    private static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 3);
        for (int b : bytes) {
            b &= 0xff;
            sb.append(HEXDIGITS[b >> 4]);
            sb.append(HEXDIGITS[b & 15]);
            sb.append(':');
        }
        return sb.toString();
    }

    @SuppressWarnings("nls")
    @Override
    public void run() {
        try {
            checkCertificates(serverUrl);

            if (userName.length() == 0) {
                if (BiobankPlugin.getDefault().isDebugging()) {
                    userName = DEFAULT_TEST_USER;
                    appService = ServiceConnection.getAppService(serverUrl,
                        userName, DEFAULT_TEST_USER_PWD);
                } else {
                    appService = ServiceConnection.getAppService(serverUrl);
                }
            } else {
                appService = ServiceConnection.getAppService(serverUrl,
                    userName, password);
            }
            String clientVersion = Platform.getProduct().getDefiningBundle()
                .getVersion().toString();
            logger.debug("Check client version:"
                + clientVersion);
            appService.checkVersion(clientVersion);
            user = UserWrapper.getUser(appService, userName);
        } catch (ApplicationException exp) {
            if (exp instanceof ServerVersionInvalidException) {
                BgcPlugin
                    .openError(
                        // dialog title.
                        i18n.tr("Server Version Error"),
                        // dialog message.
                        i18n.tr("The server you are connecting to does not have a version. Cannot authenticate."),
                        exp);
            } else if (exp instanceof ServerVersionNewerException) {
                if (BgcPlugin.openConfirm(
                    // dialog title.
                    i18n.tr("Server Version Error"),
                    // dialog message. {0} is an exception message.
                    i18n.tr(
                        "{0} Would you like to download the latest version?",
                        exp.getMessage()))) {
                    try {
                        Desktop.getDesktop().browse(new URI(DOWNLOAD_URL));
                    } catch (Exception e1) {
                        // ignore
                    }
                    logger.error(exp.getMessage(), exp);
                }
            } else if (exp instanceof ServerVersionOlderException) {
                BgcPlugin.openError(
                    // dialog title.
                    i18n.tr("Server Version Error"),
                    exp.getMessage(), exp);
            } else if (exp instanceof ClientVersionInvalidException) {
                BgcPlugin
                    .openError(
                        // dialog title.
                        i18n.tr("Client Version Error"),
                        // dialog message.
                        i18n.tr("Cannot connect to this server because the Java Client version is invalid."),
                        exp);
            } else if (exp.getCause() != null
                && exp.getCause() instanceof RemoteAuthenticationException) {
                BgcPlugin
                    .openAsyncError(
                        // dialog title.
                        i18n.tr("Login Failed"),
                        // dialog message.
                        i18n.tr("Bad credentials. Warning: You will be locked out after 3 failed login attempts."),
                        exp);
            } else if (exp.getCause() != null
                && exp.getCause() instanceof RemoteAccessException) {
                BgcPlugin.openAsyncError(
                    // dialog title.
                    i18n.tr("Login Failed"),
                    // dialog message.
                    i18n.tr("Error contacting server."), exp);
            }
        } catch (Exception exp) {
            BgcPlugin.openAsyncError(
                // dialog title.
                i18n.tr("Login Failed"),
                exp);
        }
    }

    public BiobankApplicationService getAppService() {
        return appService;
    }

    public UserWrapper getUser() {
        return user;
    }
}
