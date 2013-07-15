/**
 * WRML - Web Resource Modeling Language
 *  __     __   ______   __    __   __
 * /\ \  _ \ \ /\  == \ /\ "-./  \ /\ \
 * \ \ \/ ".\ \\ \  __< \ \ \-./\ \\ \ \____
 *  \ \__/".~\_\\ \_\ \_\\ \_\ \ \_\\ \_____\
 *   \/_/   \/_/ \/_/ /_/ \/_/  \/_/ \/_____/
 *
 * http://www.wrml.org
 *
 * Copyright (C) 2011 - 2013 Mark Masse <mark@wrml.org> (OSS project WRML.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wrml.runtime.service.file;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wrml.model.Abstract;
import org.wrml.model.Filed;
import org.wrml.model.Model;
import org.wrml.model.format.Format;
import org.wrml.model.rest.Api;
import org.wrml.model.schema.Schema;
import org.wrml.runtime.Context;
import org.wrml.runtime.Dimensions;
import org.wrml.runtime.Keys;
import org.wrml.runtime.format.ModelFormattingException;
import org.wrml.runtime.format.ModelWriteOptions;
import org.wrml.runtime.format.ModelWriterException;
import org.wrml.runtime.format.SystemFormat;
import org.wrml.runtime.schema.SchemaLoader;
import org.wrml.runtime.service.AbstractService;
import org.wrml.runtime.service.Service;
import org.wrml.runtime.service.ServiceConfiguration;
import org.wrml.runtime.service.ServiceException;
import org.wrml.util.UniqueName;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * <p>
 * The {@link FileSystem} as a WRML {@link Service}.
 * </p>
 * <p/>
 * <h2>Overview</h2>
 * <p/>
 * <p>
 * This {@link Service} stores {@link Model} data in {@link File}s and, to support cross-{@link Schema} key
 * lookups, a {@link Model}'s {@link Keys} are stored as <b>Symbolic Link</b> files (aka "Alias" files) using
 * {@link Path}s created with the {@link Files#createSymbolicLink(Path, Path, java.nio.file.attribute.FileAttribute...)}
 * method. As a result of this design, each {@link Model} instance has its <i>state</i> stored in one single "data"
 * file, which is "linked to" from Symbolic Links (one per key in {@link Keys}) that are used as indices for
 * {@link Model} data look-ups.
 * </p>
 * <p/>
 * <h2>Organizational Structure</h2>
 * <p/>
 * <p>
 * The example directory structure below outlines the elements that are fundamental to the implementation of this
 * {@link Service}.
 * <p/>
 * <pre>
 *
 *  / (rootDirectory)
 *   |
 *   | # NOTE: Schema-based directories follow for each distinct model type that is (or was) managed by this service.
 *   |
 *   +---- /com
 *   |     |
 *   |     |
 *   |     +---- /example
 *   |           |
 *   |           |
 *   |           +---- /shape
 *   |                 |
 *   |                 | # NOTE: Each subdirectory below represents the local name portion of a specific Schema's UniqueName.
 *   |                 |
 *   |                 |
 *   |                 +---- /Circle
 *   |                 |     |
 *   |                 |     |
 *   |                 |     +---- /data
 *   |                 |           # NOTE: This directory contains the Circle model data files.
 *   |                 |
 *   |                 |
 *   |                 +---- /Shape
 *   |                 |     |
 *   |                 |     |
 *   |                 |     +---- /keys
 *   |                 |           # NOTE: This directory contains the model key symbolic links for Circle, Square, and Triangle models.
 *   |                 |
 *   |                 |
 *   |                 +---- /Square
 *   |                 |     |
 *   |                 |     |
 *   |                 |     +---- /data
 *   |                 |           # NOTE: This directory contains the Square model data files.
 *   |                 |
 *   |                 |
 *   |                 +---- /Triangle
 *   |                       |
 *   |                       |
 *   |                       +---- /data
 *   |                             # NOTE: This directory contains the Triangle model data files.
 *   |
 *   ----- /edu
 *   ----- /gov
 *   ----- /org
 *   ...
 *
 * </pre>
 * <p/>
 * </pre>
 * </p>
 * <p/>
 * <h3>Root Directory</h3>
 * <p/>
 * <p>
 * At the highest level of this {@link Service}'s file system structure is the "root" directory, which is specified by
 * the {@link FileSystemService#ROOT_DIRECTORY_SETTING_NAME} configuration value.
 * </p>
 * <p/>
 * <h3>Schema-based Directories</h3>
 * <p/>
 * <p>
 * Directly underneath the root directory, {@link Model}s are separated into directories based upon their {@link Schema}
 * , or more specifically, based upon their {@link Schema}'s {@link UniqueName}, which is converted into a directory
 * {@link Path}.
 * </p>
 * <p/>
 * <p>
 * For example, if the {@link Service} contains any {@link Api}s, then it will have a directory {@link Path} like this:
 * <code>(rootDirectory)/org/wrml/model/rest/Api</code> to store them.
 * </p>
 * <p/>
 * <h3><i>data</i> Directory</h3>
 * <p/>
 * <p>
 * Within each (non-{@link Abstract}) {@link Schema}-based directory, a "data" subdirectory may be found. The data
 * directory contains "model state" files, one per {@link Model} named with {@link UUID}s. The format of the stored
 * model data files depends on the {@link Format} that has been configured.
 * </p>
 * <p/>
 * <p>
 * Continuing the previous example, an {@link Api} might be stored in:
 * </p>
 * <p/>
 * <p>
 * <code>(rootDirectory)/org/wrml/model/rest/Api/data/9600a5d0-c75e-41bd-b361-d22e3b55b7b4.json</code>
 * </p>
 * <p/>
 * <h3><i>keys</i> Directory</h3>
 * <p/>
 * <p>
 * Any {@link Schema} with a declared key slot may have a subdirectory, named "keys", within its associated
 * directory. If present, the keys directory contains Symbolic Links that "reference" some associated data file
 * (described above).
 * </p>
 * <p/>
 * <p>
 * If a {@link Schema} declares a key slot with a value that naturally lends itself to representation using a
 * directory/file {@link Path}, then this Service will organize the Symbolic Links to reflect the nature of their key
 * values. Examples of such key slot value types are {@link URI}s and {@link UniqueName}s, the inherent
 * path-orientation of these types logically <i>maps</i> to a directory/file {@link Path} layout.
 * </p>
 * <p/>
 * <p>
 * This design accomplishes two goals. First it optimizes model look-ups by using the key alias {@link Path}s as indices
 * into the data directories. Second it provides human readable/browse-able access to the stored model data by aliasing
 * the UUID files with more "meaningful" key value-based alias names.
 * </p>
 * <p/>
 * <p>
 * Finishing the example, the {@link Api} stored in the {@link File}:
 * </p>
 * <p/>
 * <p>
 * <code>(rootDirectory)/org/wrml/model/rest/Api/data/9600a5d0-c75e-41bd-b361-d22e3b55b7b4.json</code>
 * </p>
 * <p/>
 * <p>
 * Is "keyed" from the Symbolic Link with the {@link Path}:
 * </p>
 * <p/>
 * <p>
 * <code>(rootDirectory)/org/wrml/model/rest/Api/keys/com/example/ShapeApi.json</code>
 * </p>
 * <p/>
 * <p>
 * This example demonstrates this service's representation of the alias associated with the {@link Api}'s key value, its
 * {@link UniqueName} of "com/example/ShapeApi", as a directory/file {@link Path} <code>com/example/ShapeApi.json</code>
 * .
 * </p>
 *
 * @see Filed
 * @see Service
 * @see Path
 * @see File
 * @see Files
 * @see FileSystem
 * @see Keys
 */
public final class FileSystemService extends AbstractService
{

    public static final String ROOT_DIRECTORY_SETTING_NAME = "rootDirectory";


    private static final Logger LOG = LoggerFactory.getLogger(FileSystemService.class);

    private static final String STATIC_PATH_SEGMENT_DATA = "data";

    private static final String STATIC_PATH_SEGMENT_KEYS = "keys";


    private Path _RootDirectoryPath;

    private String _FileExtension;

    private URI _FileFormatUri;

    public static void writeModelFile(final Model model, final Path modelFilePath, final URI fileFormatUri,
                                      final ModelWriteOptions writeOptions) throws IOException, ModelWriterException
    {

        final Context context = model.getContext();
        OutputStream out = null;
        try
        {
            Files.createDirectories(modelFilePath.getParent());
            Files.deleteIfExists(modelFilePath);
            Files.createFile(modelFilePath);
            out = Files.newOutputStream(modelFilePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        }
        catch (final IOException e)
        {
            IOUtils.closeQuietly(out);
            throw e;
        }

        try
        {
            context.writeModel(out, model, writeOptions, fileFormatUri);
        }
        catch (final ModelWriterException e)
        {
            IOUtils.closeQuietly(out);
            throw e;
        }

        IOUtils.closeQuietly(out);
    }

    @Override
    public void delete(final Keys keys, final Dimensions dimensions)
    {

        if (keys == null)
        {
            final ServiceException e = new ServiceException("The keys cannot be null.", null, this);
            LOG.error(e.getMessage(), e);
            throw e;
        }


        final File file = getDataFile(keys);

        if (file == null)
        {
            return;
        }

        // TODO: Need to delete the key symlink files too?

        FileUtils.deleteQuietly(file);
    }

    @Override
    public Model get(final Keys keys, final Dimensions dimensions)
    {

        if (keys == null)
        {
            final ServiceException e = new ServiceException("The keys cannot be null.", null, this);
            LOG.error(e.getMessage(), e);
            throw e;
        }

        if (dimensions == null)
        {
            final ServiceException e = new ServiceException("The dimensions cannot be null.", null, this);
            LOG.error(e.getMessage(), e);
            throw e;
        }

        final File file = getDataFile(keys);

        if (file == null)
        {
            return null;
        }

        InputStream in;
        try
        {
            in = FileUtils.openInputStream(file);
        }
        catch (final Exception e)
        {
            throw new ServiceException("Failed to open stream content.", e, this);
        }

        final Context context = getContext();
        final Model model;
        try
        {
            model = context.readModel(in, keys, dimensions, _FileFormatUri);
        }
        catch (final ModelFormattingException e)
        {
            throw new ServiceException("Failed to read model.", e, this);
        }
        finally
        {
            IOUtils.closeQuietly(in);
        }

        if (model instanceof Filed)
        {
            ((Filed) model).setFile(file);
        }

        return model;

    }

    public URI getFileFormatUri()
    {

        return _FileFormatUri;
    }

    @Override
    public Model save(final Model model)
    {

        if (model == null)
        {
            final ServiceException e = new ServiceException("The model to save cannot be null.", null, this);
            LOG.error(e.getMessage(), e);
            throw e;
        }

        final Context context = model.getContext();
        final Keys keys = model.getKeys();

        if (keys == null)
        {
            final ServiceException e = new ServiceException("The model must have keys in order to be saved.", null,
                    this);
            LOG.error(e.getMessage(), e);
            throw e;
        }

        final Set<URI> keyedSchemaUris = keys.getKeyedSchemaUris();
        if (keyedSchemaUris == null || keyedSchemaUris.isEmpty())
        {
            final ServiceException e = new ServiceException(
                    "The model must have one or more key values in order to be saved.", null, this);
            LOG.error(e.getMessage(), e);
            throw e;
        }

        final int keyCount = keyedSchemaUris.size();
        final Set<Path> keyLinkPaths = new LinkedHashSet<Path>(keyCount);

        // TODO Perhaps this flag should be overridable via config?
        //final URI documentSchemaUri = context.getSchemaLoader().getDocumentSchemaUri();

        UUID managedDataFileHandle = null;

        final URI filedSchemaUri = getFiledSchemaUri(context);

        for (final URI keyedSchemaUri : keyedSchemaUris)
        {

            if (keyedSchemaUri.equals(filedSchemaUri))
            {
                continue;
            }

            final Object keyValue = keys.getValue(keyedSchemaUri);
            final Path keyLinkPath = getKeyLinkPath(keyedSchemaUri, keyValue);
            if (keyLinkPath == null)
            {
                continue;
            }

            if (managedDataFileHandle == null && Files.exists(keyLinkPath))
            {
                // This model has been saved here (managed) before.
                // Get the name of the file associated with the (existing) model's data (so we can overwrite it).
                final String dataFileHandleString = keyLinkPath.getFileName().toString();
                try
                {
                    managedDataFileHandle = UUID.fromString(dataFileHandleString);
                }
                catch (final Exception e)
                {
                    managedDataFileHandle = null;
                }

            }

            // Add the key link path
            keyLinkPaths.add(keyLinkPath);

        }

        Path dataFilePath = null;
        if (model instanceof Filed)
        {
            final Filed filed = (Filed) model;
            final File file = filed.getFile();
            if (file != null)
            {
                dataFilePath = file.toPath();
            }

        }

        if (dataFilePath == null)
        {
            if (managedDataFileHandle == null)
            {
                managedDataFileHandle = UUID.randomUUID();
            }

            dataFilePath = getManagedDataFilePath(model.getSchemaUri(), managedDataFileHandle.toString());
        }

        // TODO: All of the writes should probably be synchronized somehow, yes?.

        // Write the model data to a "data" file
        writeDataFile(model, dataFilePath);

        for (final Path keyLinkPath : keyLinkPaths)
        {
            // Write each key as a symlink "key" that references the model's data file
            writeKeyLink(keyLinkPath, dataFilePath);
        }

        return get(keys, model.getDimensions());
    }

    @Override
    protected void initFromConfiguration(final ServiceConfiguration config)
    {

        if (config == null)
        {
            final ServiceException e = new ServiceException("The config cannot be null.", null, this);
            LOG.error(e.getMessage(), e);
            throw e;
        }

        final Map<String, String> settings = config.getSettings();
        if (settings == null)
        {
            final ServiceException e = new ServiceException("The config settings cannot be null.", null, this);
            LOG.error(e.getMessage(), e);
            throw e;
        }

        final String rootDirectoryPath = settings.get(ROOT_DIRECTORY_SETTING_NAME);

        if (rootDirectoryPath == null)
        {
            final ServiceException e = new ServiceException("The root directory config parameter is required.", null,
                    this);
            LOG.error(e.getMessage(), e);
            throw e;
        }

        File givenPath = FileUtils.getFile(rootDirectoryPath);
        if (!givenPath.exists())
        {
            final File cwd = new File(".");
            final String cwdPath = cwd.getAbsolutePath();
            final ServiceException e = new ServiceException("The root directory given does not exist. "
                    + rootDirectoryPath + ", current working directory is " + cwdPath, null, this);
            LOG.error(e.getMessage(), e);
            throw e;
        }
        // Make this reference absolute
        givenPath = givenPath.getAbsoluteFile();
        _RootDirectoryPath = givenPath.toPath();

        // TODO: Make this configurable
        _FileFormatUri = SystemFormat.json.getFormatUri();
        _FileExtension = "." + SystemFormat.json.getFileExtension();

    }

    private Path findExistingKeyLinkPath(final Keys keys)
    {

        final Set<URI> keyedSchemaUris = keys.getKeyedSchemaUris();
        for (final URI keyedSchemaUri : keyedSchemaUris)
        {
            final Object keyValue = keys.getValue(keyedSchemaUri);
            final Path keyLinkPath = getKeyLinkPath(keyedSchemaUri, keyValue);

            LOG.debug("Key link from schema {} with value {} is {}", new Object[]{keyedSchemaUri, keyValue,
                    keyLinkPath});

            if (keyLinkPath != null)
            {
                if (Files.exists(keyLinkPath) && !Files.isDirectory(keyLinkPath))
                {
                    return keyLinkPath;
                }
            }
        }

        return null;
    }

    private File getDataFile(final Keys keys)
    {

        final Context context = getContext();
        final Set<URI> keyedSchemaUris = keys.getKeyedSchemaUris();
        final URI filedSchemaUri = getFiledSchemaUri(context);
        if (keyedSchemaUris.contains(filedSchemaUri))
        {
            final File dataFile = keys.getValue(filedSchemaUri);
            if (dataFile != null)
            {
                return dataFile;
            }
        }

        final Path keyLinkPath = findExistingKeyLinkPath(keys);
        if (keyLinkPath == null)
        {
            LOG.debug("A key link was NOT found for keys:\n{}", keys);
            return null;
        }

        LOG.debug("A key link \"{}\" was found for keys:\n{}", keyLinkPath, keys);

        if (!Files.isSymbolicLink(keyLinkPath))
        {
            final File keyLinkPathFile = keyLinkPath.toFile();
            return keyLinkPathFile;
        }

        if (Files.isSymbolicLink(keyLinkPath))
        {
            // Resolve the key symlink to the model's data file.
            try
            {
                final Path dataFilePath = keyLinkPath.toRealPath();
                return dataFilePath.toFile();
            }
            catch (final IOException e)
            {

                LOG.error(e.getMessage(), e);
                throw new ServiceException("Unable to dereference the key symlink (I/O problem: " + e.getMessage()
                        + ").", e, this);

            }
        }

        return null;

    }

    private URI getFiledSchemaUri(final Context context)
    {

        return context.getSchemaLoader().getTypeUri(Filed.class);
    }

    private String getFileExtension()
    {

        return _FileExtension;
    }

    private Path getKeyLinkPath(final URI keyedSchemaUri, final Object keyValue)
    {

        final Path rootDirectoryPath = getRootDirectoryPath();
        Path path = rootDirectoryPath.resolve(StringUtils.stripStart(keyedSchemaUri.getPath(), "/"));
        path = path.resolve(STATIC_PATH_SEGMENT_KEYS);

        String keyValueString = null;

        if (keyValue instanceof URI)
        {
            final URI uri = (URI) keyValue;
            final String host = uri.getHost();
            if (host == null || host.trim().isEmpty())
            {
                return null;
            }

            path = path.resolve(host);
            final int port = (uri.getPort() == -1) ? 80 : uri.getPort();
            path = path.resolve(String.valueOf(port));
            keyValueString = StringUtils.stripStart(uri.getPath(), "/");
        }
        else
        {
            final Context context = getContext();
            keyValueString = context.getSyntaxLoader().formatSyntaxValue(keyValue);
        }

        if (keyValueString == null || keyValueString.equals("null"))
        {
            return null;
        }

        if (keyValueString.trim().isEmpty() || keyValueString.endsWith("/"))
        {
            keyValueString = "index";
        }

        if (!keyValueString.endsWith(getFileExtension()))
        {
            keyValueString += getFileExtension();
        }

        path = path.resolve(keyValueString);
        return path.normalize();
    }

    private Path getManagedDataFilePath(final URI schemaUri, final String dataFileHandle)
    {

        final Path rootDirectoryPath = getRootDirectoryPath();
        Path dataFilePath = rootDirectoryPath.resolve(StringUtils.stripStart(schemaUri.getPath(), "/"));
        dataFilePath = dataFilePath.resolve(STATIC_PATH_SEGMENT_DATA);
        dataFilePath = dataFilePath.resolve(dataFileHandle + getFileExtension());
        return dataFilePath;
    }

    private Path getRootDirectoryPath()
    {

        return _RootDirectoryPath;
    }

    private void writeDataFile(final Model model, final Path dataFilePath)
    {

        final ModelWriteOptions writeOptions = new ModelWriteOptions();
        writeOptions.setPrettyPrint(true);
        final Set<URI> excludedSchemaUris = new HashSet<>(1);
        final SchemaLoader schemaLoader = model.getContext().getSchemaLoader();

        final URI filedSchemaUri = schemaLoader.getTypeUri(Filed.class);
        excludedSchemaUris.add(filedSchemaUri);

        // TODO: Verify that the model has other (non-Filed) keys before excluding the Document URI
        // excludedSchemaUris.add(schemaLoader.getDocumentSchemaUri());

        writeOptions.setExcludedSchemaUris(excludedSchemaUris);

        try
        {
            FileSystemService.writeModelFile(model, dataFilePath, _FileFormatUri, writeOptions);
        }
        catch (final Exception e)
        {
            LOG.error(e.getMessage(), e);
            throw new ServiceException("Failed to write model data file - error: " + e.toString() + " - message: "
                    + e.getMessage(), e, this);
        }

    }

    private void writeKeyLink(final Path keyLinkPath, final Path dataFilePath)
    {

        if (!Files.exists(dataFilePath))
        {
            throw new ServiceException("Attempting to make link to non-existent resource " + dataFilePath, null, this);
        }

        try
        {
            // TODO: Should this block be synchronized?
            /**
             * Changing this to be a relative path since a lot of these files are checked in.
             */
            // Get the parent or path treats the file as a node.
            final Path relPath = keyLinkPath.getParent().relativize(dataFilePath);
            Files.deleteIfExists(keyLinkPath);
            Files.createDirectories(keyLinkPath.getParent());
            Files.createDirectories(dataFilePath.getParent());
            Files.createSymbolicLink(keyLinkPath, relPath);
        }
        catch (final IOException e)
        {
            LOG.error(e.getMessage(), e);
            throw new ServiceException("Failed to write model key link file - error: " + e.toString() + " - message: "
                    + e.getMessage(), e, this);

        }
    }

}
