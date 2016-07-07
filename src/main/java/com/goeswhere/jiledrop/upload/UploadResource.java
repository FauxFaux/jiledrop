package com.goeswhere.jiledrop.upload;

import com.goeswhere.jiledrop.app.JileDropConfiguration;
import com.goeswhere.jiledrop.app.Storage;
import com.goeswhere.jiledrop.types.FileId;
import com.goeswhere.jiledrop.types.Target;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;

@Path("/upload/{target:" + Target.VALID_TARGET_REGEX + "}/")
public class UploadResource {
    private static final Logger logger = LoggerFactory.getLogger(UploadResource.class);

    private final Storage storage;

    public UploadResource(@PathParam("target") Target target, JileDropConfiguration config) {
        this.storage = new Storage(target, config.getStorageDirectory());
    }

    @GET
    @Path("chunked")
    @Produces(MediaType.APPLICATION_JSON)
    public Response chunkStatus(
            @NotNull @QueryParam("fileId") FileId fileId,
            @NotNull @QueryParam("resumableChunkNumber") int chunkNumber) {
        if (storage.partComplete(fileId, chunkNumber)) {
            return Response.ok().build();
        }

        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("chunked")
    public boolean storeChunk(
            @NotNull @FormDataParam("fileId") FileId fileId,
            @NotNull @FormDataParam("resumableChunkNumber") int chunkNumber,
            @NotNull @FormDataParam("resumableTotalChunks") int totalChunks,
            @NotNull @FormDataParam("file") InputStream data) throws IOException {
        return storage.storePart(fileId, chunkNumber, data, chunkNumber == totalChunks);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("complete")
    public UploadSuccess complete(
            @QueryParam("fileId") FileId fileId,
            @QueryParam("total") int total,
            @NotEmpty String name
    ) throws IOException {
        return new UploadSuccess(storage.combine(fileId, name, total));
    }
}
