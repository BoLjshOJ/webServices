import com.mongodb.MongoClient;
import com.mongodb.MongoWriteException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Iterator;
import java.util.Random;

@Path("links")
public class LinkResource {

    private static final MongoCollection<Document> LINK_COLLECTION;

    static {
        final MongoClient mongo = new MongoClient("localhost", 27017);
        final MongoDatabase db = mongo.getDatabase("testBase");
        LINK_COLLECTION = db.getCollection("links");
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("{id}")
    public Response getUrlById(final @PathParam("id") String id) {
        if (id == null || id.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        final FindIterable<Document> resultIterable = LINK_COLLECTION.find(new Document("id", id));
        final Iterator<Document> resultIterator = resultIterable.iterator();
        if (!resultIterator.hasNext()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        final String url = resultIterator.next().getString("url");
        if (url == null || url == "") {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(url).build();
    }
        @PUT
        @Consumes(MediaType.TEXT_PLAIN)
        @Produces(MediaType.TEXT_PLAIN)
        public Response shortUrl ( final String url){
            int attempt = 0;
            while (attempt < 5) {
                final String id = getRandomId();
                final Document newShortDoc = new Document("id", id);
                newShortDoc.put("url", url);
                try {
                    LINK_COLLECTION.insertOne(newShortDoc);
                    return Response.ok(id).build();
                } catch (MongoWriteException e) {

                }
                attempt++;
            }
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }


        private static String getRandomId () {
            String possibleCharacters = "QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm1234567890";
            StringBuilder iBuilder = new StringBuilder();
            Random rnd = new Random();
            while (iBuilder.length() < 5) {
                int index = (int) (rnd.nextFloat() * possibleCharacters.length());
                iBuilder.append(possibleCharacters.charAt(index));
            }
            return iBuilder.toString();
        }
    }
