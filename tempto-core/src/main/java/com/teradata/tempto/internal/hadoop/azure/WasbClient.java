package com.teradata.tempto.internal.hadoop.azure;

import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageCredentials;
import com.microsoft.azure.storage.StorageCredentialsAccountAndKey;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;
import com.teradata.tempto.hadoop.FileSystemClient;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

public class WasbClient
        implements FileSystemClient
{
    private static final Logger logger = getLogger(WasbClient.class);
    private CloudBlobClient wasbClient;

    public static final String CONF_WASB_ACCESSKEY = "wasb.accesskey";
    public static final String CONF_WASB_ACCOUNTNAME = "wasb.accountname";
    public static final String CONF_WASB_CONTAINERNAME = "wasb.containername";

    private static final String EMPTY_STRING = "";
    private static final String PATH_SEPARATOR = "/";
    private final String container;

    @Inject
    public WasbClient(@Named(CONF_WASB_ACCESSKEY) String accessKey,
            @Named(CONF_WASB_ACCOUNTNAME) String accountName,
            @Named(CONF_WASB_CONTAINERNAME) String containerName)
    {
        checkNotNull(accessKey, "Access key cannot be null");
        checkNotNull(accountName, "Account name cannot be null");
        checkNotNull(containerName, "Container name cannot be null");
        this.wasbClient = createWasbClient(accountName, accessKey);
        this.container = containerName;
    }

    private CloudBlobClient createWasbClient(String accountName, String accessKey)
    {
        Optional<StorageCredentials> credentials = Optional.of(new StorageCredentialsAccountAndKey(accountName, accessKey));
        if (credentials.isPresent()) {
            CloudStorageAccount storageAccount = null;
            try {
                storageAccount = new CloudStorageAccount(credentials.get());
            }
            catch (URISyntaxException e) {
                e.printStackTrace();
            }
            return storageAccount.createCloudBlobClient();
        }
        else {
            throw new RuntimeException("WASB credentials not configured");
        }
    }

    @Override
    public void createDirectory(String path)
    {

    }

    private CloudBlobContainer getContainer()
    {
        try {
            return wasbClient.getContainerReference(container);
        }
        catch (URISyntaxException e) {
            throw Throwables.propagate(e);
        }
        catch (StorageException e) {
            throw Throwables.propagate(e);
        }
    }

    private String ensureTrailingSlashPresent(String path)
    {
        if (!path.endsWith(PATH_SEPARATOR)) {
            path = path + PATH_SEPARATOR;
        }
        return path;
    }

    private String ensureStartSlashNotPresent(String path)
    {
        if (path.startsWith(PATH_SEPARATOR)) {
            path = path.substring(1);
        }
        return path;
    }

    @Override
    public void delete(String path)
    {
        try {
            CloudBlockBlob blob = getContainer().getBlockBlobReference(ensureStartSlashNotPresent(path));
            blob.deleteIfExists();
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
        }
        catch (StorageException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteDirectory(String path)
    {
        try {
            for (ListBlobItem blobItem : getContainer().listBlobs(ensureStartSlashNotPresent(ensureTrailingSlashPresent(path)), true)) {
                if (blobItem instanceof CloudBlockBlob) {
                    ((CloudBlockBlob) blobItem).deleteIfExists();
                }
            }

            // Delete the folder
            delete(path);
        }
        catch (StorageException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean exist(String path)
    {
        try {
            CloudBlockBlob blob = getContainer().getBlockBlobReference(ensureStartSlashNotPresent(path));
            return blob.exists();
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
        }
        catch (StorageException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void saveFile(String path, InputStream input, long byteLength)
    {
        try {
            CloudBlockBlob blob = getContainer().getBlockBlobReference(ensureStartSlashNotPresent(path));
            blob.upload(input, byteLength);
            logger.debug("Saved file {} to WASB", path);
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
        }
        catch (StorageException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveFile(String path, RepeatableContentProducer repeatableContentProducer)
    {
        try {
            InputStream inputStream = repeatableContentProducer.getInputStream();
            byte[] contentBytes = IOUtils.toByteArray(inputStream);
            saveFile(path, repeatableContentProducer.getInputStream(), contentBytes.length);
        }
        catch (IOException e) {
            throw new RuntimeException("Could not save file " + path + " in WASB", e);
        }
    }

    @Override
    public void loadFile(String path, OutputStream outputStream)
    {
        try {
            InputStream inputStream = getContainer().getBlockBlobReference(ensureStartSlashNotPresent(path)).openInputStream();
            IOUtils.copy(inputStream, outputStream);
        }
        catch (IOException e) {
            throw new RuntimeException("Could not read file " + path + " in WASB", e);
        }
        catch (StorageException e) {
            e.printStackTrace();
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    // Not needed for WASB
    @Override
    public long getLength(String path) { return 0; }

    // Not needed for WASB
    @Override
    public String getOwner(String path) { return EMPTY_STRING; }

    // Not supported in WASB
    @Override
    public void setXAttr(String path, String key, String value) {}

    // Not supported in WASB
    @Override
    public void removeXAttr(String path, String key) {}

    // Not supported in WASB
    @Override
    public Optional<String> getXAttr(String path, String key) { return Optional.empty(); }
}
