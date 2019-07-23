import com.bencode.BencodeDecoder;
import com.bencode.BencodeValue;
import com.bencode.InvalidBencodingException;
import com.torrent.client.tracker.TrackerConnectionHandler;
import com.torrent.utils.BlockingSet;
import com.torrent.utils.TorrentConstants;
import com.torrent.utils.TorrentContext;
import com.torrent.utils.TorrentThreadPool;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;

public class Torrent extends  Thread{

    private String torrentFileName;
    private TrackerConnectionHandler connectionHandler;
    private Logger LOGGER = Logger.getLogger(Torrent.class.getName());
    private BlockingSet peerAddress;

    public Torrent(String torrentFileName) {
        this.torrentFileName = torrentFileName;
        this.peerAddress = new BlockingSet();
    }

    @Override
    public void run() {
        LOGGER.info("Torrent: start() execution started");
        if(this.torrentFileName != null) {
            try (FileInputStream fin = new FileInputStream(new File(this.torrentFileName))) {
                BencodeDecoder decoder = new BencodeDecoder(fin);
                HashMap<String, BencodeValue> torrentMetaInfo = (HashMap<String, BencodeValue>)decoder.decode().getValue();
                initContext(torrentMetaInfo);
                this.connectionHandler = new TrackerConnectionHandler(TorrentContext.getContext());
                LOGGER.debug("Runnable ::: " + this.connectionHandler);
                getPeerInfoFromTracker();
                String peers= "";
                int count = 0;
                do{
                    peers = peers + this.peerAddress.take();
                    count ++;
                }while(this.peerAddress.size() != 0);
                LOGGER.debug("PEERS: " + peers + " " + count);

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

    private void getPeerInfoFromTracker() {
        TorrentThreadPool.getThreadPool().execute(connectionHandler);
    }

    private void initContext(HashMap<String, BencodeValue> torrentMetaInfo) {
        TorrentContext context = TorrentContext.getContext();
        for(String key: torrentMetaInfo.keySet()) {
            context.addProperty(key, torrentMetaInfo.get(key));
        }
        context.addProperty("fileName", torrentFileName);
        context.addProperty(TorrentConstants.PEER_ADDRESSES, this.peerAddress);
        context.addProperty(TorrentConstants.LEECHERS, 0);
        context.addProperty(TorrentConstants.SEEDERS, 0);
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
