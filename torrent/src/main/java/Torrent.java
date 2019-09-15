import com.bencode.BencodeDecoder;
import com.bencode.BencodeValue;
import com.bencode.InvalidBencodingException;
import com.torrent.client.peer.PeerConnectionHandler;
import com.torrent.client.tracker.TrackerConnectionHandler;
import com.torrent.utils.BlockingSet;
import com.torrent.utils.TorrentContext;
import com.torrent.utils.TorrentThreadPool;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.concurrent.Future;

public class Torrent extends Thread{

    private String torrentFileName;
    private TrackerConnectionHandler connectionHandler;
    private PeerConnectionHandler peerConnectionHandler;
    private Logger LOGGER = Logger.getLogger(Torrent.class.getName());
    private BlockingSet<InetSocketAddress> peerAddress;

    public Torrent(String torrentFileName) {
        this.torrentFileName = torrentFileName;
        this.peerAddress = new BlockingSet<>();
    }

    @Override
    public void run() {
        LOGGER.info("Torrent: start() execution started");
        if(this.torrentFileName != null) {
            try (FileInputStream fin = new FileInputStream(new File(this.torrentFileName))) {
                BencodeDecoder decoder = new BencodeDecoder(fin);

                // the torrent file has bencoded dictionary structure
                HashMap<String, BencodeValue> torrentMetaData = (HashMap<String, BencodeValue>)decoder.decode().getValue();
                initContext(torrentMetaData);
                this.connectionHandler = new TrackerConnectionHandler(TorrentContext.getContext());
                LOGGER.debug("Runnable ::: " + this.connectionHandler);
                Future<?> isTrackerComplete = getPeerInfoFromTracker();
                this.peerConnectionHandler = new PeerConnectionHandler(TorrentContext.getContext(), isTrackerComplete);
                connectAndStartDownloadFromPeers();
            } catch (FileNotFoundException e) {
                LOGGER.error("File with given name does not exist");
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (InvalidBencodingException e) {
                LOGGER.error("Error while decoding torrent file: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            LOGGER.info("No torrent file given");
        }
        LOGGER.info("Torrent: start() execution end");
    }

    private void connectAndStartDownloadFromPeers() {
        this.peerConnectionHandler.connectAndStartDownloadFromPeers();
    }

    private Future<?> getPeerInfoFromTracker() {
        return TorrentThreadPool.getThreadPool().submit(connectionHandler);
    }

    private void initContext(HashMap<String, BencodeValue> torrentMetaData) {
        TorrentContext context = TorrentContext.getContext();
        for(String key: torrentMetaData.keySet()) {
            context.addProperty(key, torrentMetaData.get(key));
        }
        context.addProperty("fileName", torrentFileName);
    }

    public static void main(String[] args) {
        TorrentThreadPool threadPool = TorrentThreadPool.getThreadPool();
        //Torrent torrent = new Torrent("C:\\Users\\Deepayan\\Downloads\\Fantastic Beasts and Where to Find Them (2016) [BluRay] [720p] [YTS.AM].torrent");
        //Torrent t1 = new Torrent("C:\\Users\\Deepayan\\Downloads\\Big-Buck-Bunny.torrent");
        //Torrent t2 = new Torrent("C:\\Users\\Deepayan\\Downloads\\Avatar (2009) [BluRay] [720p] [YTS.AM].torrent");
        Torrent t3 = new Torrent("C:\\Users\\Deepayan\\Downloads\\Captain Phillips (2013) [BluRay] [720p] [YTS.AM].torrent");
        //Torrent t4 = new Torrent("C:\\Users\\Deepayan\\Downloads\\Dunkirk (2017) [BluRay] [720p] [YTS.AM].torrent");

        //threadPool.execute(torrent);
        //threadPool.execute(t1);
        //threadPool.execute(t2);
        threadPool.execute(t3);
        //threadPool.execute(t4);
    }
}
