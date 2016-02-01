package circuit.architecture;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.TimeUnit;

import circuit.exceptions.InvalidPlatformException;

public class ArchitectureCacher {

    private String circuitName;
    private File netFile, architectureFile;

    private long netTime, architectureTime;
    private String netPath, architecturePath;
    private boolean useVprTiming;

    private final File cacheFolder = new File("data");

    public ArchitectureCacher(String circuitName, File netFile, File architectureFile, boolean useVprTiming) {
        this.netFile = netFile;
        this.architectureFile = architectureFile;

        this.netPath = netFile.getAbsolutePath();
        this.architecturePath = architectureFile.getAbsolutePath();

        this.netTime = this.getFileTime(this.netFile);
        this.architectureTime = this.getFileTime(this.architectureFile);

        this.useVprTiming = useVprTiming;

        this.circuitName = circuitName;

        this.cacheFolder.mkdirs();
    }

    public boolean store(Architecture architecture) {
        try {
            this.storeThrowing(architecture);
            return true;

        } catch(IOException error) {
            return false;
        }
    }

    private void storeThrowing(Architecture architecture) throws IOException {
        File cacheFile = this.getCachedCircuitFile();
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(cacheFile));


        out.writeObject(this.netPath);
        out.writeObject(this.architecturePath);
        out.writeObject(this.netTime);
        out.writeObject(this.architectureTime);
        out.writeObject(this.useVprTiming);

        out.writeObject(architecture);

        out.close();
    }

    public Architecture loadIfCached() {
        ObjectInputStream in = this.getInputStreamIfCached();

        if(in == null) {
            return null;
        }

        try {
            Architecture architecture = (Architecture) in.readObject();
            in.close();
            return architecture;

        } catch(ClassNotFoundException | IOException e) {
            return null;
        }
    }



    private ObjectInputStream getInputStreamIfCached() {

        // Open the cache file
        File cacheFile = this.getCachedCircuitFile();

        ObjectInputStream in;
        try {
            in = new ObjectInputStream(new FileInputStream(cacheFile));
        } catch(IOException error) {
            return null;
        }

        // Try to read the file time and cache time
        if(this.upToDate(in)) {
            return in;
        } else {
            return null;
        }
    }

    private boolean upToDate(ObjectInputStream in) {
        long cacheNetTime, cacheArchitectureTime;
        String cacheNetPath, cacheArchitecturePath;
        boolean cacheUseVprTiming;

        try {
            cacheNetPath = (String) in.readObject();
            cacheArchitecturePath = (String) in.readObject();

            cacheNetTime = (Long) in.readObject();
            cacheArchitectureTime = (Long) in.readObject();

            cacheUseVprTiming = (Boolean) in.readObject();

        } catch(ClassNotFoundException | IOException error) {
            return false;
        }

        return (
                this.netPath.equals(cacheNetPath)
                && this.architecturePath.equals(cacheArchitecturePath)
                && this.netTime == cacheNetTime
                && this.architectureTime == cacheArchitectureTime
                && this.useVprTiming == cacheUseVprTiming);
    }


    private long getFileTime(File file) {
        try {
            return this.getFileTimeThrowing(file);

        } catch(IOException | InvalidPlatformException error) {
            return -1;
        }
    }
    private long getFileTimeThrowing(File file) throws IOException, InvalidPlatformException {
        Path filePath = file.toPath();
        boolean isUnix = Files.getFileStore(filePath).supportsFileAttributeView("unix");

        if(isUnix) {
            FileTime fileTime = (FileTime) Files.getAttribute(filePath, "unix:ctime");
            return fileTime.to(TimeUnit.SECONDS);

        } else {
            throw new InvalidPlatformException("Only Unix is supported");
        }
    }


    private File getCachedCircuitFile() {
        return new File(this.cacheFolder, this.circuitName + ".ser");
    }
}
